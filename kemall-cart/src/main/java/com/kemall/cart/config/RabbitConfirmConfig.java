package com.kemall.cart.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfirmConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        
        // 👇 设置 Confirm 回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                // 消息成功到达交换机
                System.out.println("✅ Confirm 确认成功，消息ID：" + correlationData.getId());
            } else {
                // 消息未到达交换机（网络问题、交换机不存在等）
                System.err.println("❌ Confirm 确认失败，消息ID：" + correlationData.getId() + "，原因：" + cause);
                // TODO: 记录失败日志，后续人工补偿或定时重发
            }
        });
        
        return rabbitTemplate;
    }
}