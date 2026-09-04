package com.kemall.cart.listener;

import com.kemall.cart.constant.CartMqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CartMessageListener {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = CartMqConstant.QUEUE_NAME,
                    durable = "true"
            ),
            exchange = @Exchange(
                    name = CartMqConstant.EXCHANGE_NAME,
                    type = ExchangeTypes.DIRECT,
                    declare = "false"
            ),
            key = CartMqConstant.ROUTING_KEY_SYNC
    ))
    public void handleCartSyncMessage(String message) {
        log.info("收到消息:{}", message);
    }

}
