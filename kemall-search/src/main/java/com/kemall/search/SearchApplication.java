package com.kemall.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

@SpringBootApplication
@Slf4j
public class SearchApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SearchApplication.class, args);
        log.info("||----------------------||");
        log.info("SearchApplication Started");
        log.info("||----------------------||");
        Environment env = context.getEnvironment();
        String port = env.getProperty("local.server.port");
        String ip = "127.0.0.1";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            // ignore
        }
        System.out.println("==========> 应用启动完成，访问地址: http://" + ip + ":" + port + "/doc.html");
    }

}
