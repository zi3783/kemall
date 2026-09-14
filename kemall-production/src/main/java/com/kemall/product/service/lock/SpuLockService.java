package com.kemall.product.service.lock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.common.annotation.RedissonLock;
import com.kemall.common.utils.BeanUtil;
import com.kemall.product.constants.RedisConstants;
import com.kemall.product.domain.cache.SpuCache;
import com.kemall.product.domain.po.Product;
import com.kemall.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SpuLockService {

    private final StringRedisTemplate redisTemplate;

    private final ProductMapper productMapper;

    private final ObjectMapper objectMapper;

    @RedissonLock(key = "#spuId", prefix = RedisConstants.PRODUCT_SPU_LOCK_PREFIX, waitTime = 3000)
    public SpuCache getSpuFromDBAndToRedis(Long spuId) {
        //再次查询一次redis
        String spuKey = RedisConstants.PRODUCT_SPU_PREFIX + spuId;
        String spuJson = redisTemplate.opsForValue().get(spuKey);
        if(spuJson != null){
            try {
                return objectMapper.readValue(spuJson, SpuCache.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("json to object is fail:", e);
            }
        }

        Product spu = productMapper.selectById(spuId);

        SpuCache spuCache = BeanUtil.copyBean(spu, SpuCache.class);
        try {
            String json = objectMapper.writeValueAsString(spuCache);
            redisTemplate.opsForValue().set(spuKey, json, 2, TimeUnit.HOURS);
            return spuCache;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("object to json is fail:", e);
        }
    }
}
