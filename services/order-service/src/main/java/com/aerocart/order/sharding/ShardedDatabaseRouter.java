package com.aerocart.order.sharding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShardedDatabaseRouter {

    private static final Logger log = LoggerFactory.getLogger(ShardedDatabaseRouter.class);

    /**
     * Determines target shard ID based on userId hash modulo
     */
    public String resolveShardId(Long userId) {
        if (userId == null) {
            return "shard0";
        }
        int shardIndex = Math.abs(userId.hashCode()) % 2;
        String shardId = "shard" + shardIndex;
        log.debug("Routed userId {} to database shard: {}", userId, shardId);
        return shardId;
    }
}
