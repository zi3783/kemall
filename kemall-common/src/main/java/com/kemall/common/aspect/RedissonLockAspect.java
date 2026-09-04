package com.kemall.common.aspect;


import com.kemall.common.annotation.RedissonLock;
import com.kemall.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class RedissonLockAspect {

    private final RedissonClient redissonClient;

    private final ExpressionParser parser;

    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    @Around("@annotation(redissonLock)")
    public Object lockTransaction(ProceedingJoinPoint joinPoint,  RedissonLock redissonLock) throws Throwable {
        //解析锁的key
        String lockKey = parseKey(joinPoint, redissonLock);
        //获得锁
        RLock lock = redissonClient.getLock(lockKey);
        //尝试加锁
        boolean locked = false;
        log.debug("尝试加锁");
        try {
            //leaseTime > 0 使用固定租期（不会自动续期，需保证业务在租期内完成）
            //leaseTime = 0 走看门狗模式，自动续期
            if (redissonLock.leaseTime() > 0) {
                locked = lock.tryLock(redissonLock.waitTime(), redissonLock.leaseTime(), TimeUnit.SECONDS);
            } else {
                locked = lock.tryLock(redissonLock.waitTime(), TimeUnit.SECONDS);
            }
            if(!locked){
                log.debug("加锁失败");
                throw new RuntimeException("系统繁忙");
            }
            log.debug("加锁成功，执行业务");
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁被中断", e);
        } catch (BusinessException e) {
            //业务异常原样抛出，避免包装后丢失类型
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            //释放锁
            if(locked && lock.isHeldByCurrentThread()){
                lock.unlock();
                log.debug("释放锁成功");
            }
        }
    }

    private String parseKey(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) {
        String key = redissonLock.key();
        if(!key.contains("#") && !key.contains("$")){
            return redissonLock.prefix() + redissonLock.key();
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] arguments = joinPoint.getArgs();

        StandardEvaluationContext map = new StandardEvaluationContext();
        for(int i = 0; i < parameterNames.length; i++){
            map.setVariable(parameterNames[i], arguments[i]);
        }

        Expression expression = expressionCache.computeIfAbsent(key, parser::parseExpression);
        key = expression.getValue(map, String.class);

        String lockKey = redissonLock.prefix() + key;

        log.debug("key = {}", lockKey);
        return lockKey;
    }

}
