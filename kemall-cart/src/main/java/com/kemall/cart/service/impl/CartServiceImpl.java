package com.kemall.cart.service.impl;

import com.kemall.cart.constant.CartMqConstant;
import com.kemall.cart.constant.RedisConstant;
import com.kemall.cart.domain.dto.ProductionDTO;
import com.kemall.cart.domain.po.Cart;
import com.kemall.cart.mapper.CartMapper;
import com.kemall.cart.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * <p>
 * 购物车主表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-09-04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {


    private final StringRedisTemplate redisTemplate;

    private final RabbitTemplate rabbitTemplate;

    private final

    @Override
    public void addToCart(Long skuId, Integer quantity) {
        Long userId = UserContext.getUserId();
        String key = RedisConstant.CART_PREFIX + userId;

        redisTemplate.opsForHash().increment(key, skuId, quantity);

        rabbitTemplate.convertAndSend(
                CartMqConstant.EXCHANGE_NAME,
                CartMqConstant.ROUTING_KEY_SYNC,
                ProductionDTO.builder().userId(userId).skuId(skuId).quantity(quantity).build(),
                new CorrelationData(UUID.randomUUID().toString())
        );
    }
}
