package edu.session;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisSessionStore {

    private final StringRedisTemplate redis;

    public RedisSessionStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void save(Long userId,String json) {
        redis.opsForValue().set(String.valueOf(userId),json,Duration.ofHours(2));
    }

    public String get(Long userId) {
        return redis.opsForValue().get(String.valueOf(userId));
    }

    public void delete(Long userId) {
        redis.delete(String.valueOf(userId));
    }
}

