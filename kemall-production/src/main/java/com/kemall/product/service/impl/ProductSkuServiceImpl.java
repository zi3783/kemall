package com.kemall.product.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.api.dto.SkuCache;
import com.kemall.common.core.exception.BusinessException;
import com.kemall.product.constants.RedisConstants;
import com.kemall.product.convert.SkuCacheConvert;
import com.kemall.product.convert.SkuConvert;
import com.kemall.product.domain.po.ProductSku;
import com.kemall.product.domain.vo.SkuVo;
import com.kemall.product.mapper.ProductSkuMapper;
import com.kemall.product.service.IProductSkuService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Service
@RequiredArgsConstructor
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku> implements IProductSkuService {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final SkuCacheConvert skuCacheConvert;

    private final SkuConvert skuConvert;

    private final ProductSkuMapper productSkuMapper;

    private final RedissonClient redissonClient;


    /**
     * @return Id -> Price == null ? null : Long(&)
     */
    @Override
    public Map<Long, Long> getSkuPriceByIds(List<Long> skuIds) {
        List<String> skuKeys = skuIds.stream().map(skuId -> RedisConstants.PRODUCT_SKU_PREFIX + skuId).toList();
        List<String> skus = redisTemplate.opsForValue().multiGet(skuKeys);
        Map<Long, Long> result = new HashMap<>();

        //存不在redis缓存的key
        Map<Long, SkuCache> notExistKeyPriceMap = new HashMap<>();
        try {
            if (skus != null) {
                for (int i = 0; i < skus.size(); i++) {
                    String skuJson = skus.get(i);
                    if (skuJson == null) {
                        notExistKeyPriceMap.put(skuIds.get(i), null);
                        continue;
                    }
                    if (skuJson.isEmpty()){
                        result.put(skuIds.get(i), null);
                        continue;
                    }
                    SkuCache sku = objectMapper.readValue(skuJson, SkuCache.class);

                    Long skuId = sku.getId();
                    Long price = sku.getPrice();
                    result.put(skuId, price);
                }
            }

            Set<Long> ners = notExistKeyPriceMap.keySet();
            LambdaQueryChainWrapper<ProductSku> queryWrapper = new LambdaQueryChainWrapper<>(productSkuMapper);
            if(!ners.isEmpty()){
                List<ProductSku> list = queryWrapper.in(ProductSku::getId, ners)
                        .eq(ProductSku::getStatus, 1)
                        .list();
                for (ProductSku productSku : list) {
                    SkuCache skuCache = skuConvert.toSkuCache(productSku);
                    notExistKeyPriceMap.put(productSku.getId(), skuCache);
                }
            }

            Map<String, String> reWriteMap = new HashMap<>();
            Map<String, Long> ttlMap = new HashMap<>();
            notExistKeyPriceMap.forEach((skuId, skuCache) -> {
                if(skuCache != null){
                    result.put(skuId, skuCache.getPrice());
                    ttlMap.put(RedisConstants.PRODUCT_SKU_PREFIX + skuId, 30 * 60L);
                    try {
                        String skuJson = objectMapper.writeValueAsString(skuCache);
                        reWriteMap.put(RedisConstants.PRODUCT_SKU_PREFIX + skuId, skuJson);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    reWriteMap.put(RedisConstants.PRODUCT_SKU_PREFIX + skuId, "");
                    result.put(skuId, null);
                    ttlMap.put(RedisConstants.PRODUCT_SKU_PREFIX + skuId, 5 * 60L);
                }
            });
            if(!reWriteMap.isEmpty()){
                redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    RedisStringCommands stringCommands = connection.stringCommands();
                    reWriteMap.forEach((key, value) -> {
                        byte[] rawKey = redisTemplate.getStringSerializer().serialize(key);
                        byte[] rawValue = redisTemplate.getStringSerializer().serialize(value);
                        stringCommands.set(
                                rawKey,
                                rawValue,
                                Expiration.seconds(ttlMap.get(key)),
                                RedisStringCommands.SetOption.UPSERT
                        );
                    });
                    return null;
                });
            }

            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SkuVo queryById(Long skuId) { //todo 缓存空值出现问题，需要修改
        // 先查 redis
        String skuKey = RedisConstants.PRODUCT_SKU_PREFIX + skuId;
        String skuJson = redisTemplate.opsForValue().get(skuKey);
        try {
            if (skuJson != null) {
                SkuCache skuCache = objectMapper.readValue(skuJson, SkuCache.class);
                return skuCacheConvert.toSkuVo(skuCache);
            }
            // 再查 db
            ProductSku sku = lambdaQuery().eq(ProductSku::getId, skuId).one();
            if (sku == null) {
                throw new BusinessException("商品规格已下架");
            }
            SkuCache skuCache = skuConvert.toSkuCache(sku);
            String json = objectMapper.writeValueAsString(skuCache);
            redisTemplate.opsForValue().set(skuKey, json);
            return skuConvert.toSkuVo(sku);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SkuVo> queryBySpuId(Long spuId) {  //todo 缓存空值出现问题，需要修改
        String skusKey = RedisConstants.PRODUCT_SKUS_PREFIX + spuId;
        Set<String> skuIds = redisTemplate.opsForSet().members(skusKey);
        try {
            if (skuIds != null && !skuIds.isEmpty()) {
                List<String> skuIdList = skuIds.stream().map(s -> RedisConstants.PRODUCT_SKU_PREFIX + s).toList();
                List<String> skuJsons = redisTemplate.opsForValue().multiGet(skuIdList);
                List<SkuVo> result = new ArrayList<>();
                if (skuJsons != null) {
                    for (String skuJson : skuJsons) {
                        if(skuJson == null) {
                            continue;
                        }
                        SkuCache skuCache = objectMapper.readValue(skuJson, SkuCache.class);
                        SkuVo skuVo = skuCacheConvert.toSkuVo(skuCache);
                        result.add(skuVo);
                    }
                }
                return result;
            }
            List<ProductSku> skus = lambdaQuery().eq(ProductSku::getProductId, spuId).list();
            if(skus.isEmpty()){
                return new ArrayList<>();
            }
            List<SkuVo> result = new ArrayList<>(skus.size());
            Map<String, String> toCacheMap = new HashMap<>();
            skuIds = new HashSet<>();
            for (ProductSku sku : skus) {
                SkuVo skuVo = skuConvert.toSkuVo(sku);
                SkuCache skuCache = skuConvert.toSkuCache(sku);
                result.add(skuVo);
                skuIds.add(sku.getId().toString());
                toCacheMap.put(
                        RedisConstants.PRODUCT_SKU_PREFIX + sku.getId().toString(),
                        objectMapper.writeValueAsString(skuCache)
                );
            }
            redisTemplate.opsForSet().add(skusKey, skuIds.toArray(new String[0]));
            redisTemplate.opsForValue().multiSet(toCacheMap);
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean skuIsExist(Long skuId) {
        String key = RedisConstants.PRODUCT_SKU_PREFIX + skuId;
        String exist = redisTemplate.opsForValue().get(key);
        if(exist != null) {
            return !exist.isEmpty();
        }

        //不存在
        RLock lock = redissonClient.getLock(RedisConstants.PRODUCT_SKU_LOCK_PREFIX + skuId);
        boolean success = false;
        for(int i = 0; i < 3; ++i){
            try {
                success = lock.tryLock(5, -1, TimeUnit.SECONDS);
                if (success) {
                    //执行业务
                    //双检
                    exist = redisTemplate.opsForValue().get(key);
                    if(exist != null) {
                        return !exist.isEmpty();
                    }

                    LambdaQueryChainWrapper<ProductSku> queryWrapper = new LambdaQueryChainWrapper<>(productSkuMapper);
                    ProductSku one = queryWrapper.eq(ProductSku::getId, skuId)
                            .eq(ProductSku::getStatus, 1)
                            .one();
                    if (one == null) {
                        redisTemplate.opsForValue().set(key, "", 5, TimeUnit.MINUTES);
                        return false;
                    }
                    SkuCache skuCache = skuConvert.toSkuCache(one);
                    String json = objectMapper.writeValueAsString(skuCache);
                    redisTemplate.opsForValue().set(key, json, 30 , TimeUnit.MINUTES);
                    return true;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("尝试加锁之前失败了" + e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("obj to json is fail" + e);
            } finally {
                if (success) {
                    lock.unlock();
                }
            }
        }
        log.error("无法获得锁：skuId:" + skuId);
        throw new RuntimeException("无法获得锁：skuId:" + skuId);
    }
}
