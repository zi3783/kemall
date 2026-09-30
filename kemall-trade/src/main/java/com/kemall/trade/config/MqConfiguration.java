package com.kemall.trade.config;

import com.kemall.api.constant.OrderMqConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfiguration {

    @Bean
    public Queue deadQueue() {
        return new Queue(OrderMqConstant.ORDER_DEAD_QUEUE, true);
    }

    @Bean
    public Exchange deadExchange() {
        return new DirectExchange(OrderMqConstant.ORDER_DEAD_EXCHANGE);
    }

    @Bean
    public Binding deadBinding() {
        return BindingBuilder.bind(deadQueue()).to(deadExchange()).with(OrderMqConstant.ORDER_DEAD_ROUTING_KEY).noargs();
    }

}
