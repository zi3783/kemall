package com.kemall.pay;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

@Slf4j
@SpringBootApplication
@MapperScan("com.kemall.pay.mapper")
public class PayApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PayApplication.class, args);
        log.info("||----------------------||");
        log.info("PayApplication Started");
        log.info("||----------------------||");
        Environment env = context.getEnvironment();
        String port = env.getProperty("local.server.port");
        String ip = "127.0.0.1";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        System.out.println("==========> 应用启动完成，访问地址: http://" + ip + ":" + port);
    }
}
