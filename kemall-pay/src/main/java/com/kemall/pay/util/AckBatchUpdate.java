package com.kemall.pay.util;

import com.kemall.pay.mapper.LocalMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
@RequiredArgsConstructor
public class AckBatchUpdate {

    private final LocalMessageMapper localMessageMapper;

    private final Queue<String> queue =  new ConcurrentLinkedQueue<>();

    private static final int BATCH_SIZE = 100;

    private static final int QUEUE_ALERT_THRESHOLD = 10000;

    public void add(String message){
        if(message == null){
            throw  new NullPointerException("message is null");
        }
        log.debug("messageId:"+message);
        queue.add(message);
        if(queue.size() > QUEUE_ALERT_THRESHOLD){
            log.warn("聚合队列积压{}",queue.size());
        }
    }

    @Scheduled(fixedRate = 500)
    public void flush(){
        List<String> list = new ArrayList<>(BATCH_SIZE);
        while( list.size() < BATCH_SIZE && !queue.isEmpty() ){
            String messageId = queue.poll();
            list.add(messageId);
        }
        if(list.isEmpty()){
            return ;
        }

        localMessageMapper.batchUpdateStatusBymMsgIds(list);
    }

}
