package com.kemall.cart;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@Slf4j
@SpringBootApplication
@MapperScan("com.kemall.cart.mapper")
public class CartApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CartApplication.class, args);
        log.info("||---------------------||");
        log.info("CartApplication started");
        log.info("||---------------------||");
    }
}
