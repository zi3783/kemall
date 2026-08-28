package com.kemall.product;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@MapperScan("com.kemall.product.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
        log.info("||----------------------||");
        log.info("ProductApplication Started");
        log.info("||----------------------||");
    }
}
