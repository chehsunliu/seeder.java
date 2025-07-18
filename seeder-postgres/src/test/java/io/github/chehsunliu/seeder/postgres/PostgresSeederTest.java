package io.github.chehsunliu.seeder.postgres;

import io.github.chehsunliu.seeder.core.SeederManager;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PostgresSeederTest {
    @Test
    void testIsUsable() {
        var manager = SeederManager.builder()
                .seeder(new PostgresSeeder("jdbc:postgresql://127.0.0.1:5432/demo", "postgres", "xxx", "app"))
                .build();
        manager.truncate();
        manager.seedResource("/test-data/data-1");
    }

    @Test
    void testNestedFolder() {
        var manager = SeederManager.builder()
                .seeder(new PostgresSeeder("jdbc:postgresql://127.0.0.1:5432/demo", "postgres", "xxx", "app")
                        .withGetFilePath((tableName) -> Path.of("db").resolve(tableName + ".json")))
                .build();
        manager.truncate();
        manager.seedResource("/test-data/data-2");
    }
}
