package com.kemall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class RedisLuaConfiguration {

    @Bean
    public RedisScript<String> cartItemUpdateScript() {
        DefaultRedisScript<String> stringDefaultRedisScript = new DefaultRedisScript<>();
        stringDefaultRedisScript.setScriptSource(
                new ResourceScriptSource(
                        new ClassPathResource("lua/cart_item_update.lua")
                )
        );
        stringDefaultRedisScript.setResultType(String.class);
        return stringDefaultRedisScript;
    }
}
