package com.kemall.pay.config;

import com.kemall.pay.util.AckBatchUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RabbitConfig {

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

}
