package com.kemall.cart.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.api.dubbo.ProductionDubboService;
import com.kemall.api.result.Result;
import com.kemall.cart.constant.CartMqConstant;
import com.kemall.cart.constant.RedisConstant;
import com.kemall.cart.domain.dto.CartUpdateDTO;
import com.kemall.cart.domain.dto.ProductionDTO;
import com.kemall.cart.domain.po.Cart;
import com.kemall.cart.exception.ProductionNotExistException;
import com.kemall.cart.mapper.CartMapper;
import com.kemall.cart.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    @Override
    public void addToCart(Long pId,Long skuId, Integer quantity) {
        Long userId = UserContext.getUserId();
        String key = RedisConstant.CART_PREFIX + userId;


        //todo 发送lua脚本进行更新
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
                    CartMqConstant.EXCHANGE_NAME,
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
}
