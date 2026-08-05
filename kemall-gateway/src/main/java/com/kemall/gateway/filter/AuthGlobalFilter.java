package com.kemall.gateway.filter;

import com.kemall.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/users/login",    // 登录放行
            "/users"           // 注册放行
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //取出token
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //todo 白名单放行
        String path = request.getURI().getPath();
        if(WHITE_LIST.contains(path)){
            log.info("白名单放行");
            return chain.filter(exchange);
        }

        HttpHeaders headers = request.getHeaders();
        List<String> tokens = headers.get("token");
        if(tokens==null){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = tokens.get(0);

        //校验token
        Claims claims = jwtUtil.parseToken(token);
        if(claims==null){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        Object userId = claims.get("userId");
        if(userId==null){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        //获取数据userId
        Long uid = (Long) userId;
        ServerHttpRequest newRequire = request.mutate().header("userId", String.valueOf(uid)).build();
        return chain.filter(exchange.mutate().request(newRequire).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
