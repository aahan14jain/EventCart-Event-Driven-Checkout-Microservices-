package com.eventcart.inventory_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class RedisStartupCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RedisStartupCheck.class);
    private static final String HEALTH_KEY = "health:redis";
    private static final String HEALTH_VALUE = "connected";

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisStartupCheck(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        redisTemplate.opsForValue().set(HEALTH_KEY, HEALTH_VALUE);
        log.info("Redis is working: wrote key \"{}\" = \"{}\"", HEALTH_KEY, HEALTH_VALUE);
    }
}
