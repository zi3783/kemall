package com.kemall.iam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

@Slf4j
@SpringBootApplication
@MapperScan(
        value = {
                "com.kemall.iam.auth.infrastructure.persistence.mapper",
                "com.kemall.iam.user.infrastructure.persistence.mapper"
        },
        annotationClass = Mapper.class
)
@MapperScan(
        value = {
                "com.kemall.iam.permission.mapper",
                "com.kemall.iam.platform.mapper",
                "com.kemall.iam.tenant.mapper",
                "com.kemall.iam.user.mapper"
        },
        markerInterface = BaseMapper.class
)
public class IamApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(IamApplication.class, args);
        log.info("||----------------------||");
        log.info("IamApplication Started");
        log.info("||----------------------||");
        Environment env = context.getEnvironment();
        String port = env.getProperty("local.server.port");
        String ip = "127.0.0.1";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            // ignore
        }
        System.out.println("==========> 应用启动完成，访问地址: http://" + ip + ":" + port + "/doc.html" );
    }
}
