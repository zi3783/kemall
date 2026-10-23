package com.kemall.trade.listener;

import com.kemall.api.constant.OrderMqConstant;
import com.kemall.common.core.exception.BusinessException;
import com.kemall.trade.constant.RabbitMQConstants;
import com.kemall.trade.domain.exceptions.OrderException;
import com.kemall.trade.service.IOrdersService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMessageListener {

    private final IOrdersService ordersService;

    /**
     * 接收支付成功消息，修改订单状态未待发货
     * @param message
     * @param channel
     * @throws IOException
     */
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

        try {
            ordersService.confirmOrderPayed(json);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicNack(tag, false, false);
            throw e;
        }
    }

    /**
     * 接收过期订单消息，修改订单状态
     * @param orderNo
     * @param channel
     * @param message
     */
    @RabbitListener(queues = RabbitMQConstants.ORDER_CLOSE_QUEUE,autoStartup = "false")
    public void reviseExpireOrderStatus(String orderNo, Channel channel, Message message) {
        long tag = message.getMessageProperties().getDeliveryTag();
        try{
            try {
                ordersService.cancelOrder(orderNo);
            }catch (BusinessException e){
                channel.basicAck(tag, false);
            }catch (Exception e){
                channel.basicNack(tag, false, true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = OrderMqConstant.SECKILL_ORDER_QUEUE,
                    durable = "true"
            ),
            exchange = @Exchange(
                    name = OrderMqConstant.SECKILL_ORDER_EXCHANGE
            ),
            key = OrderMqConstant.SECKILL_ORDER_ROUTING_KEY
    ), autoStartup = "false", id = "seckillOrderListener")
    public void createSeckillOrder(Message message,Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String json = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            ordersService.createSeckillOrder(json);
            channel.basicAck(tag, false);
        } catch (OrderException.InventoryDeductException e) {
            channel.basicNack(tag, false, false);
        } catch (DuplicateKeyException e){
            channel.basicAck(tag, false);
        }catch (Exception e) {
            log.error("处理消息失败，messageId={}",
                    message.getMessageProperties().getMessageId(), e);
            channel.basicNack(tag, false, true);
        }
    }
}