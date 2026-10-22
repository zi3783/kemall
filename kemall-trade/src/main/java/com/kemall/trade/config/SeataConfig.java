package com.kemall.trade.config;

import org.apache.seata.tm.api.TransactionalTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeataConfig {

    @Bean
    public TransactionalTemplate transactionalTemplate() {
        // TransactionalTemplate 通常有一个无参构造函数
        return new TransactionalTemplate();
    }
}