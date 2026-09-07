package com.kemall.cart.config;

import com.kemall.cart.constant.CartMqConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfiguration {
    /**
     * 设置 JSON 消息转换器，这样发送对象时会自动序列化成 JSON，接收时自动反序列化成对象
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 购物车消息交换机
     */
    @Bean
    public DirectExchange cartMessageExchange(){
        return new ExchangeBuilder(CartMqConstant.EXCHANGE_NAME, ExchangeTypes.DIRECT)
                .durable(true)
                .alternate(CartMqConstant.BACKUP_EXCHANGE_NAME)
                .build();
    }

    /**
     * 备份交换机
     */
    @Bean
    public FanoutExchange backupExchange(){
        return ExchangeBuilder.fanoutExchange(CartMqConstant.BACKUP_EXCHANGE_NAME)
                .durable(true)
                .build();
    }

    /**
     * 备份交换机的队列
     */
    @Bean
    public Queue backupQueue(){
        return new Queue(CartMqConstant.BACKUP_QUEUE_NAME, true);
    }

    /**
     * 备份-binding
     * @return
     */
    @Bean
    public Binding backupBinding(){
        return BindingBuilder.bind(backupQueue()).to(backupExchange());
    }
}
