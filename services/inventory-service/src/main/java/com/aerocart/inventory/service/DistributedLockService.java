package com.aerocart.inventory.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;

    // Atomic stock deduction Lua script
    private static final String RESERVE_STOCK_LUA =
            "local key = KEYS[1] " +
            "local qty = tonumber(ARGV[1]) " +
            "local current = tonumber(redis.call('get', key) or '-1') " +
            "if current == -1 then " +
            "  return -2 " + // Key does not exist in cache
            "end " +
            "if current >= qty then " +
            "  redis.call('decrby', key, qty) " +
            "  return 1 " + // Success
            "else " +
            "  return -1 " + // Insufficient stock
            "end";

    public DistributedLockService(RedissonClient redissonClient, StringRedisTemplate redisTemplate) {
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Executes critical section under a Redisson Distributed Lock
     */
    public <T> T executeWithLock(String lockKey, long waitTimeMs, long leaseTimeMs, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock("lock:" + lockKey);
        try {
            boolean acquired = lock.tryLock(waitTimeMs, leaseTimeMs, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.warn("Could not acquire distributed lock for key: {}", lockKey);
                throw new IllegalStateException("System busy, failed to acquire distributed lock for key: " + lockKey);
            }
            log.debug("Acquired lock for key: {}", lockKey);
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while acquiring lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Released lock for key: {}", lockKey);
            }
        }
    }

    /**
     * Ultra-fast atomic stock deduction via Redis Lua script
     */
    public long tryReserveStockLua(String productId, int quantity) {
        String redisKey = "stock:" + productId;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(RESERVE_STOCK_LUA, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(redisKey), String.valueOf(quantity));
        return result != null ? result : -1L;
    }

    public void setStockInCache(String productId, int quantity) {
        redisTemplate.opsForValue().set("stock:" + productId, String.valueOf(quantity));
    }
}
