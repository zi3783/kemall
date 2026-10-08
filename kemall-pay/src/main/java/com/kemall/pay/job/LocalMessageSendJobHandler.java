package com.kemall.pay.job;

import com.kemall.pay.service.ILocalMessageService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LocalMessageSendJobHandler {

    private final ILocalMessageService localMessageService;

    @XxlJob("payServiceLocalMessageJobHandler")
    public void sendMessage() {
        localMessageService.sendMessage();
    }

    @XxlJob("111111111")
    public void test(){
        System.out.println("111111111");
    }
}
