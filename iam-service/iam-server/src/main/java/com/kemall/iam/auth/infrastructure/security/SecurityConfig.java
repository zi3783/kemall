package com.kemall.iam.auth.infrastructure.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * IAM Spring Security 配置。
 * <p>
 * 核心设计（Spring Security 认证链路 + 我们的 JWT 过滤器并存）：
 * <ul>
 *   <li>{@link AccountAuthenticationProvider}：登录时由 {@link AuthenticationManager} 调用，
 *       负责"认证"（查用户 + 密码 + 账户状态），这是 Spring Security 的契约</li>
 *   <li>登录成功后 {@link com.kemall.iam.auth.application.service.AuthAppService}
 *       继续创建 Session + 签发 JWT（业务逻辑，不交给 Spring Security）</li>
 *   <li>{@link JwtAuthenticationFilter}：后续受保护接口走 JWT 校验 + Redis Session 验证</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final AccountAuthenticationProvider accountAuthenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final Environment environment;

    private final List<String> effectiveWhiteList = new ArrayList<>();

    @PostConstruct
    public void init() {
        List<String> configuredWhiteList = Binder.get(environment)
                .bind("iam.security.white-list", List.class)
                .orElseGet(List::of);
        if (configuredWhiteList != null) {
            effectiveWhiteList.addAll(configuredWhiteList);
        }
        jwtAuthenticationFilter.setWhiteList(effectiveWhiteList);
        log.info("[IAM] Security 白名单: {}", effectiveWhiteList);
    }

    /**
     * 注入自定义 AuthenticationManager，包含我们的账号密码 Provider。
     * <p>
     * 也可以通过注入 AuthenticationConfiguration#getAuthenticationManager() 获取，
     * 但那样会额外带上 DaoAuthenticationProvider 等默认 Provider。
     * IAM 只用我们的 AccountAuthenticationProvider，显式注册更干净可控。
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(accountAuthenticationProvider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭默认表单登录/HTTP Basic
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                // CSRF 对无状态 JWT 接口无意义
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态会话（登录走 Spring Security 认证体系，但不写入 HttpSession）
                .sessionManagement(cfg -> cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 静态资源/X-Frame-Options 基础保护
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                // 白名单放行，其余全部需认证
                .authorizeHttpRequests(cfg -> {
                    for (String pattern : effectiveWhiteList) {
                        cfg.requestMatchers(new AntPathRequestMatcher(pattern)).permitAll();
                    }
                    cfg.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    cfg.anyRequest().authenticated();
                })
                // 自定义 401 / 403
                .exceptionHandling(cfg -> cfg
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // 插入 JWT 过滤器，位置在 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
