package com.kemall.user.config;

import com.kemall.common.utils.JwtUtil;
import com.kemall.common.utils.SnowflakeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class UtilConfiguration {
    @Bean
    public SnowflakeGenerator snowflakeGenerator(){
        return new SnowflakeGenerator(1);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // 强度10（默认），范围4-31，越高越安全但也越慢
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public JwtUtil jwtUtil(){
        return new JwtUtil();
    }
}
