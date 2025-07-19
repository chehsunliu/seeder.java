package io.github.chehsunliu.seeder.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.chehsunliu.seeder.core.SeederManager;
import io.github.chehsunliu.seeder.postgres.mybatis.UserMapper;
import java.nio.file.Path;
import java.util.Properties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

class PostgresSeederTest {
    private static final String jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/demo?currentSchema=app";
    private static final String jdbcUsername = "postgres";
    private static final String jdbcPassword = "xxx";

    private static final SqlSessionFactory sqlSessionFactory;

    static {
        var props = new Properties();
        props.setProperty("driver", "org.postgresql.Driver");
        props.setProperty("url", jdbcUrl);
        props.setProperty("username", jdbcUsername);
        props.setProperty("password", jdbcPassword);
        sqlSessionFactory = new SqlSessionFactoryBuilder()
                .build(
                        PostgresSeederTest.class.getResourceAsStream(
                                "/io/github/chehsunliu/seeder/postgres/mybatis/mybatis-config.xml"),
                        props);
    }

    @Test
    void testIsUsable() {
        var manager = SeederManager.builder()
                .seeder(new PostgresSeeder(jdbcUrl, jdbcUsername, jdbcPassword, "app"))
                .build();
        manager.truncate();
        manager.seedResource("/test-data/data-1");

        try (var session = sqlSessionFactory.openSession()) {
            var mapper = session.getMapper(UserMapper.class);
            var users = mapper.listUsers();
            assertEquals(1, users.size());
            assertEquals("google", users.get(0).auth().type());
        }
    }

    @Test
    void testNestedFolder() {
        var manager = SeederManager.builder()
                .seeder(new PostgresSeeder(jdbcUrl, jdbcUsername, jdbcPassword, "app")
                        .withGetFilePath((tableName) -> Path.of("db").resolve(tableName + ".json")))
                .build();
        manager.truncate();
        manager.seedResource("/test-data/data-2");

        try (var session = sqlSessionFactory.openSession()) {
            var mapper = session.getMapper(UserMapper.class);
            var users = mapper.listUsers();
            assertEquals(1, users.size());
            assertEquals("google", users.get(0).auth().type());
        }
    }
}
