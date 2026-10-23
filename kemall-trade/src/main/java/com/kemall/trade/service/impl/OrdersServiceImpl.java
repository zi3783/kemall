package com.kemall.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.api.constant.InventoryMqConstant;
import com.kemall.api.dto.OrderNo;
import com.kemall.api.dto.UserOrderDto;
import com.kemall.api.dubbo.InventoryDubboService;
import com.kemall.api.dubbo.ProductionDubboService;
import com.kemall.common.core.exception.BusinessException;
import com.kemall.common.core.utils.bean.result.Result;
import com.kemall.trade.constant.RabbitMQConstants;
import com.kemall.trade.constant.RedisConstant;
import com.kemall.trade.domain.dto.OrderItemRequest;
import com.kemall.trade.domain.dto.OrderRequest;
import com.kemall.trade.domain.enums.LocalMessageStatusEnum;
import com.kemall.trade.domain.exceptions.OrderException;
import com.kemall.trade.domain.po.LocalMessage;
import com.kemall.trade.domain.po.OrderItems;
import com.kemall.trade.domain.po.Orders;
import com.kemall.trade.domain.vo.OrderBrief;
import com.kemall.trade.enums.OrderStatusEnum;
import com.kemall.trade.enums.OrderTypeEnum;
import com.kemall.trade.mapper.LocalMessageMapper;
import com.kemall.trade.mapper.OrderItemsMapper;
import com.kemall.trade.mapper.OrdersMapper;
import com.kemall.trade.service.GlobalTransactionManageService;
import com.kemall.trade.service.IOrdersService;
import com.kemall.trade.util.OrderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.tm.api.TransactionalExecutor;
import org.apache.seata.tm.api.TransactionalTemplate;
import org.apache.seata.tm.api.transaction.TransactionInfo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
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

    private final TransactionTemplate transactionTemplate;

    private final OrderUtil orderUtil;

    @DubboReference(check = false)
    private InventoryDubboService inventoryDubboService;

    @DubboReference(check = false)
    private ProductionDubboService productionDubboService;

    private final GlobalTransactionManageService globalTransactionManageService;

    private final ObjectMapper objectMapper;

    private final LocalMessageMapper localMessageMapper;

    private final OrderItemsMapper orderItemsMapper;

    private final TransactionalTemplate transactionalTemplate;

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

    @Override
    public void createSeckillOrder(String json) {
        if(json == null) {
            throw new RuntimeException("秒杀订单参数为null");
        }
        try {
            UserOrderDto dto = objectMapper.readValue(json, UserOrderDto.class);
            String idempotencyKey = dto.getMessageId();
            Long skuId = dto.getSkuId();
            Long userId = dto.getUserId();
            String orderNo = orderUtil.generateOrderNo(OrderTypeEnum.SK);
            Long price = productionDubboService.getSkuPriceByIds(List.of(skuId)).get(skuId);
            if(price == null){
                throw new BusinessException("商品不存在或者已下架");
            }
            LocalDateTime expireTime = LocalDateTime.now().plusMinutes(30);

            Orders order = Orders.builder()
                    .orderNo(orderNo)
                    .userId(userId)
                    .idempotencyKey(idempotencyKey)
                    .totalAmount(price)
                    .actualAmount(price)
                    .expireTime(expireTime)
                    .status(OrderStatusEnum.WAIT_PAY)
                    .build();
            LocalMessage localMessage = LocalMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .exchange(RabbitMQConstants.ORDER_DELAY_EXCHANGE)
                    .routingKey(RabbitMQConstants.ORDER_DELAY_ROUTING_KEY)
                    .status(LocalMessageStatusEnum.PENDING)
                    .payload(orderNo)
                    .build();

            transactionalTemplate.execute(new TransactionalExecutor() {
                @Override
                public Object execute() {

                    try {
                        inventoryDubboService.prepareDeductByTcc(skuId, 1, orderNo, true);
                    }catch (Throwable e){
                        log.error("库存扣减失败");
                        throw new OrderException.InventoryDeductException(skuId, e);
                    }
                    transactionTemplate.executeWithoutResult(tx -> {
                        save(order);
                        Long orderId = order.getId();
                        OrderItems orderItem = OrderItems.builder()
                                .orderId(orderId)
                                .skuId(skuId)
                                .skuPrice(price)
                                .productQuantity(1)
                                .build();

                        orderItemsMapper.insert(orderItem);

                        localMessageMapper.insert(localMessage);
                    });
                    return null;
                }

                @Override
                public TransactionInfo getTransactionInfo() {
                    TransactionInfo transactionInfo = new TransactionInfo();
                    transactionInfo.setTimeOut(30000);
                    return transactionInfo;
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json to object is fail",e);
        } catch (Throwable t) {
            if(t instanceof TransactionalExecutor.ExecutionException){
                TransactionalExecutor.ExecutionException te = (TransactionalExecutor.ExecutionException) t;
                Throwable e = te.getOriginalException();
                if(e instanceof OrderException.InventoryDeductException){
                    throw (OrderException.InventoryDeductException) e;
                }
                log.error("全局事务回滚失败: xid:{}", RootContext.getXID());
                throw new RuntimeException("全局事务回滚失败");
            }
        }
//        catch (OrderException.InventoryDeductException e){
//            log.error("捕获到订单异常");
//            throw e;
//        }catch (Exception e) {
//            log.error("实际异常类型={}, cause类型={}",
//                    e.getClass().getName(),
//                    e.getCause() == null ? "null" : e.getCause().getClass().getName(), e);
//            throw new RuntimeException(e);
//        }

    }
}
