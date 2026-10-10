package com.kemall.pay.job;


import com.kemall.api.dubbo.AccountDubboService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReconcileJobHandler {

    private final AccountDubboService accountDubboService;

    //todo 对账未写
    @XxlJob("verifyAccount")
    public void verifyAccount(){
        //先查出哪些需要对账

        //调用账户服务返回具体的信息
//        accountDubboService.batchQueryDeduct();
    }

}
