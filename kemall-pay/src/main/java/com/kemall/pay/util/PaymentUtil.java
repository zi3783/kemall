package com.kemall.pay.util;

import com.kemall.pay.constant.RedisConstant;
import com.kemall.pay.domain.enums.PaymentChannelEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PaymentUtil {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StringRedisTemplate redisTemplate;

    public String generateOrderNo(PaymentChannelEnum channel) {
        String date = LocalDateTime.now().format(DATE_FMT);
        String paymentNo = date + channel;

        String key = RedisConstant.PAYMENT_SEQUENCE_PREFIX + date;

        Long seq = redisTemplate.opsForValue().increment(key);
        if(seq != null && seq == 1){
            redisTemplate.expire(key, 2, TimeUnit.DAYS);
        }
        paymentNo += seq;

        return paymentNo;
    }
}
