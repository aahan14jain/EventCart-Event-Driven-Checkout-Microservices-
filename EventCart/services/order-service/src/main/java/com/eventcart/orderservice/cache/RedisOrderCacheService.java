package com.eventcart.orderservice.cache;

import java.time.Duration;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Short-lived read-through cache of order status for GETs. Keys: {@code order:status:} plus orderId. Values are plain
 * status strings (e.g. PENDING, RESERVED, CONFIRMED, FAILED). {@link com.eventcart.orderservice.OrderStore} remains
 * the source of truth; on cache miss, callers load from the store and repopulate Redis.
 */
@Service
public class RedisOrderCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisOrderCacheService.class);
    private static final String NO_EVENT_ID = "n/a";

    private static final String KEY_PREFIX = "order:status:";

    private final StringRedisTemplate stringRedisTemplate;
    private final Duration ttl;
    private final long ttlSeconds;

    public RedisOrderCacheService(StringRedisTemplate stringRedisTemplate,
            @Value("${order.cache.ttl-seconds:60}") long ttlSeconds) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.ttlSeconds = ttlSeconds;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    @PostConstruct
    void logCacheConfiguration() {
        log.info("event processed: eventType={} orderId={} eventId={} result={} ttlSeconds={} keyPrefix={}",
                "order.cache.config", "n/a", NO_EVENT_ID, "ready", ttlSeconds, KEY_PREFIX);
    }

    public void saveOrderStatus(String orderId, String status) {
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + orderId, status, ttl);
    }

    public Optional<String> getOrderStatus(String orderId) {
        String value = stringRedisTemplate.opsForValue().get(KEY_PREFIX + orderId);
        return Optional.ofNullable(value).filter(s -> !s.isBlank());
    }
}
