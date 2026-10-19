package com.kemall.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.api.constant.SeckillMqConstant;
import com.kemall.api.dto.UserOrderDto;
import com.kemall.common.core.exception.BusinessException;
import com.kemall.common.core.utils.UserContext;
import com.kemall.seckill.constants.RedisConstants;
import com.kemall.seckill.domain.po.Activity;
import com.kemall.seckill.domain.po.LocalMessage;
import com.kemall.seckill.mapper.ActivityMapper;
import com.kemall.seckill.mapper.LocalMessageMapper;
import com.kemall.seckill.service.IActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-10-19
 */
@RequiredArgsConstructor
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements IActivityService {

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> seckillDeductScript;

    private final LocalMessageMapper localMessageMapper;

    private final ObjectMapper objectMapper;

    private final RabbitTemplate rabbitTemplate;

    private final DefaultRedisScript<Long> seckillRollbackScript;

    @Override
    public void seckill(Long skuId) {
        String inventoryKey = RedisConstants.SECKILL_INVENTORY_SKU_PREFIX + "{" + skuId + "}";
        String usersKey = RedisConstants.SECKILLED_SKU_ID_PREFIX + "{" + skuId + "}";

        Long userId = UserContext.getUserId();

        List<String> keys = List.of(inventoryKey, usersKey);

        String messageId = UUID.randomUUID().toString();

        String json = null;
        try {
            json = objectMapper.writeValueAsString(UserOrderDto.builder().userId(userId).skuId(skuId).messageId(messageId).build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        LocalMessage message = LocalMessage.builder()
                .messageId(messageId)
                .exchange(SeckillMqConstant.SECKILL_ORDER_EXCHANGE)
                .routingKey(SeckillMqConstant.SECKILL_ORDER_ROUTING_KEY)
                .status(0)
                .payload(json)
                .build();

        Long code = redisTemplate.execute(
                seckillDeductScript,
                keys,
                userId.toString()
        );
        int i = code.intValue();
        switch (i){
            case -1:{
                throw new BusinessException("库存不存在");
            }
            case 1:{
                throw new BusinessException("库存不足");
            }
            case 2:{
                throw new BusinessException("已经购买过sku");
            }
        }

        try {
            localMessageMapper.insert(message);
        }catch (Exception e){
            redisTemplate.execute(
                    seckillRollbackScript,
                    keys,
                    userId.toString()
            );
            throw e;
        }

        rabbitTemplate.convertAndSend(SeckillMqConstant.SECKILL_ORDER_EXCHANGE, SeckillMqConstant.SECKILL_ORDER_ROUTING_KEY, json);
    }
}
