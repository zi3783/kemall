package com.kemall.seckill.job;

import com.kemall.seckill.service.ILocalMessageService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LocalMessageSendJobHandler {

    private final ILocalMessageService localMessageService;

    @XxlJob("seckillServiceLocalMessageJobHandler")
    public void sendMessage() {
        localMessageService.sendMessage();
    }
}
