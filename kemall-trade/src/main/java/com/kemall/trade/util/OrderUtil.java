package com.kemall.trade.util;

import com.kemall.trade.constant.RedisConstant;
import com.kemall.trade.enums.OrderTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class OrderUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StringRedisTemplate redisTemplate;

    public String generateOrderNo(OrderTypeEnum type) {
        String date = LocalDateTime.now().format(DATE_FMT);
        String orderNo = type.getType() + date;

        String key = RedisConstant.ORDER_SEQUENCE_PREFIX + date;

        Long seq = redisTemplate.opsForValue().increment(date);
        if(seq != null && seq == 1){
            redisTemplate.expire(key, 2, TimeUnit.DAYS);
        }
        orderNo += seq;

        return orderNo;
    }

}
