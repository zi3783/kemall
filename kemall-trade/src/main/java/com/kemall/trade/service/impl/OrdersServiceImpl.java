package com.kemall.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.api.constant.InventoryMqConstant;
import com.kemall.api.dto.OrderNo;
import com.kemall.api.dubbo.ProductionDubboService;
import com.kemall.common.core.exception.BusinessException;
import com.kemall.common.core.utils.bean.result.Result;
import com.kemall.trade.constant.RedisConstant;
import com.kemall.trade.domain.dto.OrderItemRequest;
import com.kemall.trade.domain.dto.OrderRequest;
import com.kemall.trade.domain.enums.LocalMessageStatusEnum;
import com.kemall.trade.domain.po.LocalMessage;
import com.kemall.trade.domain.po.Orders;
import com.kemall.trade.domain.vo.OrderBrief;
import com.kemall.trade.enums.OrderStatusEnum;
import com.kemall.trade.mapper.LocalMessageMapper;
import com.kemall.trade.mapper.OrderItemsMapper;
import com.kemall.trade.mapper.OrdersMapper;
import com.kemall.trade.service.GlobalTransactionManageService;
import com.kemall.trade.service.IOrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-09-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    private final StringRedisTemplate redisTemplate;

    @DubboReference
    private ProductionDubboService productionDubboService;

    private final GlobalTransactionManageService globalTransactionManageService;

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    private final LocalMessageMapper localMessageMapper;

    private final OrderItemsMapper orderItemsMapper;

    @Override
    public Result<OrderBrief> placeOrder(OrderRequest request) {
        //校验幂等
        String idempotencyKey = RedisConstant.REQUEST_LOCK_PREFIX + request.getIdempotencyKey();
        //查询redis校验
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "exist", 5, TimeUnit.MINUTES);
        if(Boolean.FALSE.equals(ok)){
            log.info("请求重复");
            return Result.success();
        }
        //获得商品项目
        List<OrderItemRequest> cartItems = request.getOrderItemList();
        List<Long> skuIds = cartItems.stream().map(OrderItemRequest::getSkuId).toList();
        //查询商品服务获得商品金额
        Map<Long, Long> priceMap = productionDubboService.getSkuPriceByIds(skuIds);
        if(priceMap.size() != skuIds.size()){
            throw new BusinessException("有不存在或下架的商品被选中");
        }
        //todo 计算金额 暂时没有这个业务
        try {
            OrderBrief orderBrief = globalTransactionManageService.deductStorageAndPlaceOrder(cartItems, priceMap, request.getIdempotencyKey());
            //返回结果
            return Result.success(orderBrief);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void confirmOrderPayed(String json) {
        OrderNo obj = null;
        try {
            obj = objectMapper.readValue(json, OrderNo.class);
        } catch (JsonProcessingException e) {
            log.error("json反序列化失败");
            throw new RuntimeException(e);
        }
        String orderNo = obj.getOrderNo();
        boolean b = lambdaUpdate().eq(Orders::getOrderNo, orderNo)
                .set(Orders::getStatus, OrderStatusEnum.WAIT_SHIP)
                .update();
        if(!b){
            throw new BusinessException("业务错误");
        }
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNo) {
        //修改订单状态
        boolean sc = lambdaUpdate().eq(Orders::getOrderNo, orderNo)
                .eq(Orders::getStatus, OrderStatusEnum.WAIT_PAY)
                .set(Orders::getStatus, OrderStatusEnum.CANCELED)
                .update();
        if(!sc){
            log.warn("修改订单状态失败了，{}", orderNo);
            throw new BusinessException("修改订单状态失败了");
        }
        //查找所有的订单项
        List<Map<String, Object>> items = orderItemsMapper.selectByOrderNo(orderNo);

        try {
            String json = objectMapper.writeValueAsString(items);
            //将释放库存的消息保存到数据库
            LocalMessage localMessage = LocalMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .exchange(InventoryMqConstant.INVENTORY_DEDUCT_EXCHANGE)
                    .routingKey(InventoryMqConstant.INVENTORY_DEDUCT_ROUTING_KEY)
                    .status(LocalMessageStatusEnum.PENDING)
                    .payload(json)
                    .build();
            localMessageMapper.insert(localMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("toJson is fail");
        }
    }
}
