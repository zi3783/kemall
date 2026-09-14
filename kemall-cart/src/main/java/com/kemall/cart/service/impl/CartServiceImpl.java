package com.kemall.cart.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.api.dubbo.ProductionDubboService;
import com.kemall.api.result.Result;
import com.kemall.cart.constant.CartMqConstant;
import com.kemall.cart.constant.RedisConstant;
import com.kemall.cart.domain.dto.CartDto;
import com.kemall.cart.domain.dto.CartUpdateDTO;
import com.kemall.cart.domain.dto.ProductionDTO;
import com.kemall.cart.domain.po.Cart;
import com.kemall.cart.exception.ProductionNotExistException;
import com.kemall.cart.mapper.CartMapper;
import com.kemall.cart.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * 购物车主表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-09-04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    @DubboReference(timeout = 500000)
    private final ProductionDubboService productionDubboService;

    private final StringRedisTemplate redisTemplate;

    private final RabbitTemplate rabbitTemplate;

    private final RedisScript<String> cartItemUpdateScript;

    private final ObjectMapper objectMapper;

    private final CartMapper cartMapper;

    @Override
    public void addToCart(Long pId,Long skuId, Integer quantity) {
        Long userId = UserContext.getUserId();
        String key = RedisConstant.CART_PREFIX + userId;

        Result<Long> result = productionDubboService.getSkuPrice(pId, skuId);
        if(result.getCode() != 200) {
            throw new ProductionNotExistException("未找到商品");
        }
        Long price = result.getData();
        String json = redisTemplate.execute(
                cartItemUpdateScript,
                List.of(key,key + ":" + "selected"),
                skuId.toString(),
                quantity.toString(),
                price.toString(),
                skuId.toString()
        );
        try {
            CartUpdateDTO response = objectMapper.readValue(json, CartUpdateDTO.class);

            rabbitTemplate.convertAndSend(
                    CartMqConstant.SYNC_EXCHANGE_NAME,
                    CartMqConstant.ROUTING_KEY_SYNC,
                    ProductionDTO.builder()
                            .userId(userId)
                            .skuId(skuId)
                            .quantity(response.getTotalQuantity())
                            .price(response.getTotalPrice())
                            .productionId(pId)
                            .selected(response.getSelected())
                            .build(),
                    new CorrelationData(UUID.randomUUID().toString())
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化失败",e);
        }
    }

    @Override
    public Map<String, Object> listCart(Long userId) {
        String cartKey =  RedisConstant.CART_PREFIX + userId;
        Map<Object, Object> cart = redisTemplate.opsForHash().entries(cartKey);

        if(!cart.isEmpty()) {
            return cart.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> (String) entry.getKey(),
                            entry -> Integer.valueOf((String) entry.getValue())
                    ));
        }

        //查询mysql
        List<CartDto> cartDto = cartMapper.selectCartByUserId(userId);
        if(cartDto.isEmpty()) {
            return new HashMap<>();
        }
        if(cartDto.size() != 1){
            throw new BusinessException("购物车数据库数据错误！");
        }
        //回写redis
        Map<String, Object> cartToRedisMap = cartDto.get(0).getItems().stream()
                .collect(Collectors.toMap(o -> o.getSkuId().toString(), o -> o.getQuantity().toString()));

        cartToRedisMap.put(RedisConstant.CART_TOTAL_PRICE,cartDto.get(0).getTotalPrice().toString());
        cartToRedisMap.put(RedisConstant.CART_TOTAL_QUANTITY,cartDto.get(0).getTotalQuantity().toString());

        redisTemplate.opsForHash().putAll(cartKey, cartToRedisMap);
        return cartToRedisMap;
    }
}
