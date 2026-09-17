package com.kemall.inventory.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class TccDeductInventoryThreadPoolConfig {

    @Bean(name = "tccDeductInventoryThreadPool")
    public ThreadPoolExecutor tccDeductInventoryThreadPool() {
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        return new ThreadPoolExecutor(
                corePoolSize,
                corePoolSize * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                new CustomizableThreadFactory("tcc-try-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
