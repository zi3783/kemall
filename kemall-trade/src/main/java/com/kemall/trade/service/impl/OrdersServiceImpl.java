package com.kemall.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.api.constant.CartMqConstant;
import com.kemall.api.dubbo.ProductionDubboService;
import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.UserContext;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.trade.constant.RedisConstant;
import com.kemall.trade.domain.dto.OrderItemRequest;
import com.kemall.trade.domain.dto.OrderRequest;
import com.kemall.trade.domain.po.Orders;
import com.kemall.trade.domain.vo.OrderBrief;
import com.kemall.trade.mapper.OrdersMapper;
import com.kemall.trade.service.GlobalTransactionManageService;
import com.kemall.trade.service.IOrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
    private final ProductionDubboService productionDubboService;

    private final GlobalTransactionManageService globalTransactionManageService;

    private final RabbitTemplate rabbitTemplate;

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
        //todo 计算金额 无
        try {
            OrderBrief orderBrief = globalTransactionManageService.deductStorageAndPlaceOrder(cartItems, priceMap, idempotencyKey);
            //异步mq解耦调用清空购物车
            rabbitTemplate.convertAndSend(
                    CartMqConstant.CLEAN_EXCHANGE_NAME,
                    CartMqConstant.ROUTING_KEY_CLEAN,
                    UserContext.getUserId(),
                    new CorrelationData(UUID.randomUUID().toString())
            );
            //返回结果
            return Result.success(orderBrief);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
