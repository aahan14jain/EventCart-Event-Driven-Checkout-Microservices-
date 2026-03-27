package com.eventcart.orderservice.idempotency;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Deduplicates Kafka events per (event type, order id) using Redis SETNX with a TTL.
 * Key format: {@code processed:event:<eventType>:<orderId>}. TTL: {@code event.idempotency.ttl-hours} (default 24).
 */
@Service
public class IdempotencyService {

    private static final String KEY_PREFIX = "processed:event:";
    private static final String PROCESSED_MARKER = "1";

    private final StringRedisTemplate stringRedisTemplate;
    private final Duration ttl;

    public IdempotencyService(StringRedisTemplate stringRedisTemplate,
            @Value("${event.idempotency.ttl-hours:24}") long ttlHours) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.ttl = Duration.ofHours(ttlHours);
    }

    /**
     * Atomically records that this event is being handled (SETNX + expiry). Call before updating order state.
     *
     * @return {@code true} if this is the first occurrence (key was absent); {@code false} if duplicate.
     */
    public boolean markIfNotProcessed(String eventType, String orderId) {
        String key = KEY_PREFIX + eventType + ":" + orderId;
        Boolean set = stringRedisTemplate.opsForValue().setIfAbsent(key, PROCESSED_MARKER, ttl);
        return Boolean.TRUE.equals(set);
    }
}
