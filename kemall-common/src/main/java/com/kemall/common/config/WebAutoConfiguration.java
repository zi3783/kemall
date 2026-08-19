package com.kemall.common.config;

import com.kemall.common.interceptor.UserContextInterceptor;
import com.kemall.common.properties.AuthProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;


@Configuration
@ConditionalOnWebApplication(type = SERVLET)
@EnableConfigurationProperties({AuthProperties.class})
public class WebAutoConfiguration {

    @Bean
    public UserContextInterceptor userContextInterceptor(AuthProperties authProperties) {
        return new UserContextInterceptor(authProperties);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(UserContextInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor)
                        .addPathPatterns("/**");
            }
        };
    }
}
