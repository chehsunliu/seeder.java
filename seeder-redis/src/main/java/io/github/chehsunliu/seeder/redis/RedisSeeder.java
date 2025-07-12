package io.github.chehsunliu.seeder.redis;

import io.github.chehsunliu.seeder.core.Seeder;
import redis.clients.jedis.UnifiedJedis;

public class RedisSeeder implements Seeder {
    private final UnifiedJedis client;

    public RedisSeeder(String connectionString) {
        this.client = new UnifiedJedis(connectionString);
    }

    @Override
    public void truncate() {
        this.client.flushAll();
    }

    @Override
    public void seedResource(String name) {}
}
