package io.github.chehsunliu.seeder.mysql;

import io.github.chehsunliu.seeder.core.SeederManager;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MySqlSeederTest {
    @Test
    void testIsUsable() {
        var manager = SeederManager.builder()
                .seeder(new MySqlSeeder("jdbc:mysql://127.0.0.1:3306/demo", "root", "xxx", "demo"))
                .build();
        manager.truncate();
        manager.seedResource("/test-data/data-1");
    }

    @Test
    void testNestedFolder() {
        var manager = SeederManager.builder()
                .seeder(new MySqlSeeder("jdbc:mysql://127.0.0.1:3306/demo", "root", "xxx", "demo")
                        .withGetFilePath((tableName) -> Path.of("mysql").resolve(tableName + ".json")))
                .build();
        manager.truncate();
        manager.seedResource("/test-data/data-2");
    }
}
