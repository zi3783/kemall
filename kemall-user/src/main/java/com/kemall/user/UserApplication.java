package com.kemall.user;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

@SpringBootApplication
@Slf4j
@MapperScan("com.kemall.user.mapper")
public class UserApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(UserApplication.class, args);
        log.info("||---------------------||");
        log.info("UserApplication Started");
        log.info("||---------------------||");
        Environment env = context.getEnvironment();
        String port = env.getProperty("local.server.port");
        String ip = "127.0.0.1";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            // ignore
        }
        System.out.println("==========> 应用启动完成，访问地址: http://" + ip + ":" + port);
    }
}
