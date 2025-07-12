package io.github.chehsunliu.seeder.redis;

import io.github.chehsunliu.seeder.core.SeederManager;
import org.junit.jupiter.api.Test;

class RedisSeederTest {
    @Test
    void testIsUsable() {
        var manager = SeederManager.builder()
                .seeder(new RedisSeeder("redis://127.0.0.1:6379"))
                .build();
        manager.truncate();
    }
}
