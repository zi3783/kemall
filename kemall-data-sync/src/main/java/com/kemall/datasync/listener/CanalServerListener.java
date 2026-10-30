package com.kemall.datasync.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.datasync.domain.dto.CanalMessage;
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

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "canal_queue")
    public void listen(Message message, Channel channel){
        long tag = message.getMessageProperties().getDeliveryTag();
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            CanalMessage canalMessage = objectMapper.readValue(json, CanalMessage.class);
            System.out.println(canalMessage);
            channel.basicAck(tag, false);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
