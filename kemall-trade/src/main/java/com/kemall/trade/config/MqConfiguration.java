package com.kemall.trade.config;

import com.kemall.api.constant.OrderMqConstant;
import com.kemall.trade.util.AckBatchUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.kemall.trade.constant.RabbitMQConstants.*;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class MqConfiguration {

    private final AckBatchUpdate ackBatchUpdate;


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {

            if (correlationData == null) {
                log.error("correlationData is null");
                throw new RuntimeException("correlationData is null");
            }

            if( !ack ){
                return ;
            }
            ackBatchUpdate.add(correlationData.getId());
        });
        return rabbitTemplate;
    }

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

    /** 延迟队列：消息在此等待30分钟，过期后进入死信交换机 */
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(ORDER_DELAY_QUEUE)
                .ttl(30 * 60 * 1000)  // TTL = 30分钟（毫秒）
                .deadLetterExchange(ORDER_DLX_EXCHANGE)
                .deadLetterRoutingKey(ORDER_CLOSE_ROUTING_KEY)
                .build();
    }

    /** 延迟交换机（普通 direct 交换机） */
    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(ORDER_DELAY_EXCHANGE);
    }

    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderDelayExchange())
                .with(ORDER_DELAY_ROUTING_KEY);
    }

    /** 死信交换机 */
    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE);
    }

    /**消费队列*/
    @Bean
    public Queue orderCloseQueue() {
        return QueueBuilder.durable(ORDER_CLOSE_QUEUE).build();
    }

    @Bean
    public Binding orderCloseBinding() {
        return BindingBuilder.bind(orderCloseQueue())
                .to(orderDlxExchange())
                .with(ORDER_CLOSE_ROUTING_KEY);
    }

}
