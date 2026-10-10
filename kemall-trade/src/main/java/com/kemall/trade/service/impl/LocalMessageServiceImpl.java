package com.kemall.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.trade.domain.enums.LocalMessageStatusEnum;
import com.kemall.trade.domain.po.LocalMessage;
import com.kemall.trade.mapper.LocalMessageMapper;
import com.kemall.trade.service.ILocalMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private final LocalMessageMapper localMessageMapper;

    @Override
    public void sendMessage() {
        //先查出
        LocalDateTime now = LocalDateTime.now();
        List<LocalMessage> list = lambdaQuery()
                .eq(LocalMessage::getStatus, LocalMessageStatusEnum.PENDING)
                .apply("retry_count < max_retry")
                .lt(LocalMessage::getNextRetryTime, now)
                .last("limit 100")
                .list();
        if(list.isEmpty()){
            return;
        }

        LocalDateTime time = now.plusSeconds(30);
        list.forEach(localMessage -> {localMessage.setNextRetryTime(time);});
        localMessageMapper.batchUpdateNextRetryTimeByIds(list, time);

        for(LocalMessage localMessage : list){
            CorrelationData correlationData = new CorrelationData(localMessage.getMessageId());
            String exchange = localMessage.getExchange();
            String routing = localMessage.getRoutingKey();
            String payload = localMessage.getPayload();
            rabbitTemplate.convertAndSend(exchange, routing, payload, correlationData);
        }
    }
}
