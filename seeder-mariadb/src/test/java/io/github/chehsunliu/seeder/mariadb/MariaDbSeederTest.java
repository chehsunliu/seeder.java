package io.github.chehsunliu.seeder.mariadb;

import io.github.chehsunliu.seeder.core.SeederManager;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MariaDbSeederTest {
    @Test
    void testIsUsable() {
        var manager = SeederManager.builder()
                .seeder(new MariaDbSeeder("jdbc:mariadb://127.0.0.1:13306/demo", "root", "xxx", "demo"))
                .build();
        manager.truncate();
        manager.seedResource("/test-data/data-1");
    }

    @Test
    void testNestedFolder() {
        var manager = SeederManager.builder()
                .seeder(new MariaDbSeeder("jdbc:mariadb://127.0.0.1:13306/demo", "root", "xxx", "demo")
                        .withGetFilePath((tableName) -> Path.of("mariadb").resolve(tableName + ".json")))
                .build();
        manager.truncate();
        manager.seedResource("/test-data/data-2");
    }
}
