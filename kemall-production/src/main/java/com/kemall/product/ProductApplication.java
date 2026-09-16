package com.kemall.product;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetAddress;

@Slf4j
@SpringBootApplication
@MapperScan("com.kemall.product.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
public class ProductApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ProductApplication.class, args);
        log.info("||----------------------||");
        log.info("ProductApplication Started");
        log.info("||----------------------||");
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
