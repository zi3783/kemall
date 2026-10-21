package com.kemall.trade.listener.start;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpecificListenerStarter {

    private final RabbitListenerEndpointRegistry registry;

    @EventListener(ApplicationReadyEvent.class)
    public void startSeckillOrderListener() {
        MessageListenerContainer container = registry.getListenerContainer("seckillOrderListener");
        if (container != null && !container.isRunning()) {
            System.out.println("---- 应用已就绪，单独启动秒杀订单监听器...");
            container.start();
        }
    }
}