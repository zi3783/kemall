package com.kemall.trade;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class TradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradeApplication.class, args);
        log.info("||----------------------||");
        log.info("TradeApplication Started");
        log.info("||----------------------||");
    }
}
