package com.kemall.cart.config;

import com.kemall.api.constant.CartMqConstant;
import org.springframework.amqp.core.*;
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
        return new ExchangeBuilder(CartMqConstant.SYNC_EXCHANGE_NAME, ExchangeTypes.DIRECT)
                .durable(true)
                .alternate(CartMqConstant.SYNC_BACKUP_EXCHANGE)
                .build();
    }

    /**
     * 备份交换机
     */
    @Bean
    public FanoutExchange backupExchange(){
        return ExchangeBuilder.fanoutExchange(CartMqConstant.SYNC_BACKUP_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 备份交换机的队列
     */
    @Bean
    public Queue backupQueue(){
        return new Queue(CartMqConstant.SYNC_BACKUP_QUEUE, true);
    }

    /**
     * 备份-binding
     * @return
     */
    @Bean
    public Binding backupBinding(){
        return BindingBuilder.bind(backupQueue()).to(backupExchange());
    }


    @Bean
    public DirectExchange cleanExchange() {
        return ExchangeBuilder
                .directExchange(CartMqConstant.CLEAN_EXCHANGE_NAME)
                .alternate(CartMqConstant.CLEAN_BACKUP_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public FanoutExchange cleanBackupExchange() {
        return new FanoutExchange(CartMqConstant.CLEAN_BACKUP_EXCHANGE, true, false);
    }

    @Bean
    public Queue cleanBackupQueue() {
        return QueueBuilder.durable(CartMqConstant.CLEAN_BACKUP_QUEUE).build();
    }

    @Bean
    public Binding cleanBackupBinding() {
        return BindingBuilder
                .bind(cleanBackupQueue())
                .to(cleanBackupExchange());
    }
}
