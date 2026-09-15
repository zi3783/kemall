package com.kemall.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.product.domain.cache.SkuCache;
import com.kemall.product.domain.po.ProductSku;
import com.kemall.product.mapper.ProductSkuMapper;
import com.kemall.product.service.IProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
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

    @Override
    public Map<Long, Long> getSkuPriceByIds(List<Long> skuIds) {
        //todo 只查了redis 没查mysql
        List<String> skuIdsStr = skuIds.stream().map(Object::toString).toList();
        List<String> skus = redisTemplate.opsForValue().multiGet(skuIdsStr);
        Map<Long, Long> result = new HashMap<>();
        try {
            if (skus != null) {
                for (String skuJson : skus) {
                    if(skuJson == null) {
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
}
