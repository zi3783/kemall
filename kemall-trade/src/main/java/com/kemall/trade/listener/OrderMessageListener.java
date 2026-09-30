package com.kemall.trade.listener;

import com.kemall.api.constant.OrderMqConstant;
import com.kemall.trade.service.IOrdersService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OrderMessageListener {

    private final IOrdersService ordersService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = com.kemall.api.constant.OrderMqConstant.ORDER_STATE_QUEUE,
                    durable = "true",
                    arguments = {
                            @Argument(name = "x-dead-letter-exchange", value = OrderMqConstant.ORDER_DEAD_EXCHANGE),
                            @Argument(name = "x-dead-letter-routing-key", value = OrderMqConstant.ORDER_DEAD_ROUTING_KEY)
                    }
            ),
            exchange = @Exchange(
                    name = com.kemall.api.constant.OrderMqConstant.ORDER_STATE_EXCHANGE
            ),
            key = com.kemall.api.constant.OrderMqConstant.ORDER_STATE_ROUTING_KEY
    ))
    public void reviseOrderStatus(
            Message message,
            Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String json = new String(message.getBody(), StandardCharsets.UTF_8);

        try{
            ordersService.confirmOrderPayed(json);
            channel.basicAck(tag, false);
        } catch (Exception e){
            channel.basicNack(tag, false, false);
            throw e;
        }
    }
}