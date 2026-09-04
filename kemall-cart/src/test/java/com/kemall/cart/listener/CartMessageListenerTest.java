package com.kemall.cart.listener;

import com.kemall.cart.constant.CartMqConstant;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartMessageListenerTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void sendMessage() {
        String messageBody = "hello, rabbitMQ";
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                CartMqConstant.EXCHANGE_NAME,        // exchange（需要定义）
                CartMqConstant.ROUTING_KEY_SYNC,     // routingKey
                messageBody,                         // 消息体
                correlationData                      // 消息确认数据
        );
    }
}