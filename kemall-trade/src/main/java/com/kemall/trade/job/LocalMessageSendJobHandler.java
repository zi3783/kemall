package com.kemall.trade.job;

import com.kemall.trade.service.ILocalMessageService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LocalMessageSendJobHandler {

    private final ILocalMessageService localMessageService;

    @XxlJob("expiredOrderLocalMessageHandler")
    public void sendMessage() {
        localMessageService.sendMessage();
    }
}
