package com.kemall.datasync.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.datasync.service.DataSyncService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CanalServerListener {

    private final DataSyncService dataSyncService;


    @RabbitListener(queues = "canal_queue")
    public void listen(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            dataSyncService.syncElastic(json);
            channel.basicAck(tag, false);
        }catch (IOException e) {
            channel.basicNack(tag, false, true);
            throw new RuntimeException(e);
        }
    }

}
