package com.kemall.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VisitedCountService {

    private final StringRedisTemplate redisTemplate;

    @Async
    public void visitedCountIncrement(String key){
        Long newCount = redisTemplate.opsForValue().increment(key, 1);
        if(newCount != null && newCount.equals(1L)){
            redisTemplate.expire(key, 48, TimeUnit.HOURS);
        }
    }

}
