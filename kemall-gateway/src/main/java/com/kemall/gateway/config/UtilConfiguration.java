package com.kemall.gateway.config;

import com.kemall.common.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilConfiguration {
    @Bean
    public JwtUtil jwtUtil(){
        return new JwtUtil();
    }
}
