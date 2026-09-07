package com.kemall.cart.listener;

import com.kemall.cart.constant.CartMqConstant;
import com.kemall.cart.constant.RedisConstant;
import com.kemall.cart.domain.dto.ProductionDTO;
import com.kemall.cart.domain.po.Cart;
import com.kemall.cart.domain.po.CartItem;
import com.kemall.cart.mapper.CartItemMapper;
import com.kemall.cart.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CartMessageListener {

    private final DefaultRedisScript<Long> clearCartScript;

    private final CartMapper cartMapper;

    private final CartItemMapper cartItemMapper;

    private final StringRedisTemplate redisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = CartMqConstant.SYNC_QUEUE_NAME,
                    durable = "true"
            ),
            exchange = @Exchange(
                    name = CartMqConstant.SYNC_EXCHANGE_NAME,
                    declare = "false"
            ),
            key = CartMqConstant.ROUTING_KEY_SYNC
    ))
    public void handleCartSyncMessage(ProductionDTO dto) {
        Cart cart = new Cart()
                .setUserId(dto.getUserId())
                .setTotalPrice(dto.getPrice())
                .setTotalQuantity(dto.getQuantity())
                .setSelectedCount(dto.getSelected());
        cartMapper.insertByUserId(cart);
        CartItem cartItem = new CartItem().setCartId(cart.getId())
                .setQuantity(dto.getQuantity())
                .setSelected(0)
                .setProductId(dto.getProductionId())
                .setSkuId(dto.getSkuId())
                .setIsValid(1);
        cartItemMapper.insertByCartId(cartItem);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = CartMqConstant.CLEAN_QUEUE_NAME,
                    durable = "true"
            ),
            exchange = @Exchange(
                    name = CartMqConstant.CLEAN_EXCHANGE_NAME,
                    declare = "false"
            ),
            key = CartMqConstant.ROUTING_KEY_CLEAN
    ))
    public void handleCartCleanMessage(Long userId) {
        //清空mysql
        Long row = cartMapper.deleteFromCartAndItemByUserId(userId);
        log.info("用户id:{}的购物车删除{}行", userId, row);

        //清空redis
        String key = RedisConstant.CART_PREFIX + userId;
        String s_key = key + ":select";
        redisTemplate.execute(
                clearCartScript,
                List.of(key, s_key)
        );

    }
}
