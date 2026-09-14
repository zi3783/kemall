package com.kemall.product.service.lock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.common.annotation.RedissonLock;
import com.kemall.common.utils.BeanUtil;
import com.kemall.product.constants.RedisConstants;
import com.kemall.product.domain.cache.SkuCache;
import com.kemall.product.domain.po.ProductSku;
import com.kemall.product.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SkuLockService {

    private final StringRedisTemplate redisTemplate;

    private final ProductSkuMapper productSkuMapper;

    private final ObjectMapper objectMapper;

    @RedissonLock(key = "#skuId", prefix = RedisConstants.PRODUCT_SKU_LOCK_PREFIX, waitTime = 3000)
    public SkuCache getSkuFromDBAndToRedis(Long skuId) {
        //再次查询一次redis
        String skuKey = RedisConstants.PRODUCT_SKU_PREFIX + skuId;
        String skuJson = redisTemplate.opsForValue().get(skuKey);
        if(skuJson != null){
            try {
                return objectMapper.readValue(skuJson, SkuCache.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("json to object is fail:", e);
            }
        }

        ProductSku sku = productSkuMapper.selectById(skuId);

        SkuCache skuCache = BeanUtil.copyBean(sku, SkuCache.class);
        try {
            String json = objectMapper.writeValueAsString(skuCache);
            redisTemplate.opsForValue().set(skuKey, json, 2, TimeUnit.HOURS);
            return skuCache;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("object to json is fail:", e);
        }
    }
}
