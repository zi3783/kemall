package com.kemall.inventory.listener;

import com.kemall.api.constant.InventoryMqConstant;
import com.kemall.inventory.service.IInventoryService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ReleaseInventoryMessageListener {

    private final IInventoryService inventoryService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = InventoryMqConstant.INVENTORY_DEDUCT_QUEUE, durable = "true"),
                    exchange = @Exchange(name = InventoryMqConstant.INVENTORY_DEDUCT_EXCHANGE),
                    key = InventoryMqConstant.INVENTORY_DEDUCT_ROUTING_KEY
            )
    )
    public void releaseInventory(String json, Channel channel, Message message){
        try{
            boolean success = inventoryService.releaseInventory(json);
            long tag = message.getMessageProperties().getDeliveryTag();
            if (success) {
                channel.basicAck(tag, false);
            }else{
                channel.basicNack(tag, false, true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
