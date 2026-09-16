package com.kemall.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.api.dto.SkuCache;
import com.kemall.common.exception.BusinessException;
import com.kemall.product.constants.RedisConstants;
import com.kemall.product.convert.SkuCacheConvert;
import com.kemall.product.convert.SkuConvert;
import com.kemall.product.domain.po.ProductSku;
import com.kemall.product.domain.vo.SkuVo;
import com.kemall.product.mapper.ProductSkuMapper;
import com.kemall.product.service.IProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Override
    public Map<Long, Long> getSkuPriceByIds(List<Long> skuIds) {
        //todo 只查了redis 没查mysql
        List<String> skuIdsStr = skuIds.stream().map(Object::toString).toList();
        List<String> skus = redisTemplate.opsForValue().multiGet(skuIdsStr);
        Map<Long, Long> result = new HashMap<>();
        try {
            if (skus != null) {
                for (String skuJson : skus) {
                    if (skuJson == null) {
                        continue;
                    }
                    SkuCache sku = objectMapper.readValue(skuJson, SkuCache.class);

                    Long skuId = sku.getId();
                    Long price = sku.getPrice();
                    result.put(skuId, price);
                }
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SkuVo queryById(Long skuId) {
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
    public List<SkuVo> queryBySpuId(Long spuId) {
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
}
