package com.kemall.account;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class AccountApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AccountApplication.class, args);
        log.info("||----------------------||");
        log.info("AccountApplication Started");
        log.info("||----------------------||");
        Environment env = context.getEnvironment();
        System.out.println("========== Seata 配置来源检查 ==========");
        System.out.println("Active Profiles: " + Arrays.toString(env.getActiveProfiles()));
        System.out.println("seata.registry.type: " + env.getProperty("seata.registry.type"));
        System.out.println("seata.registry.nacos.server-addr: " + env.getProperty("seata.registry.nacos.server-addr"));
        System.out.println("seata.service.grouplist.DEFAULT: " + env.getProperty("seata.service.grouplist.DEFAULT"));
        System.out.println("=======================================");
    }
}
