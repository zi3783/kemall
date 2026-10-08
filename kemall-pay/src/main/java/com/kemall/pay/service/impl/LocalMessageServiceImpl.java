package com.kemall.pay.service.impl;

import com.kemall.pay.domain.enums.LocalMessageStatusEnum;
import com.kemall.pay.domain.po.LocalMessage;
import com.kemall.pay.mapper.LocalMessageMapper;
import com.kemall.pay.service.ILocalMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 本地消息表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-09-30
 */
@Service
@RequiredArgsConstructor
public class LocalMessageServiceImpl extends ServiceImpl<LocalMessageMapper, LocalMessage> implements ILocalMessageService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage() {
        //先查出
        List<LocalMessage> list = lambdaQuery()
                .eq(LocalMessage::getStatus, LocalMessageStatusEnum.PENDING)
                .apply("retry_count < max_retry")
                .lt(LocalMessage::getNextRetryTime, LocalDateTime.now())
                .last("limit 100")
                .list();

        for(LocalMessage localMessage : list){
            CorrelationData correlationData = new CorrelationData(localMessage.getMessageId());
            String exchange = localMessage.getExchange();
            String routing = localMessage.getRoutingKey();
            String payload = localMessage.getPayload();
            rabbitTemplate.convertAndSend(exchange, routing, payload, correlationData);
        }
    }
}
