package com.kemall.cart.listener;

import com.kemall.cart.constant.CartMqConstant;
import com.kemall.cart.domain.dto.ProductionDTO;
import com.kemall.cart.domain.po.Cart;
import com.kemall.cart.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CartMessageListener {

    private final CartMapper cartMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = CartMqConstant.QUEUE_NAME,
                    durable = "true"
            ),
            exchange = @Exchange(
                    name = CartMqConstant.EXCHANGE_NAME,
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
    }
}
