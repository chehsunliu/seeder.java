package io.github.chehsunliu.seeder.postgres;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.chehsunliu.seeder.core.Seeder;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

public class PostgresSeeder implements Seeder {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String schema;
    private final Connection connection;
    private final Map<String, TableInfo> tableInfos;
    private Function<String, Path> getFilePath;

    @SneakyThrows
    public PostgresSeeder(String jdbcUrl, String username, String password, String schema) {
        this.schema = schema;
        this.connection = DriverManager.getConnection(jdbcUrl, username, password);
        try (var stmt = connection.createStatement()) {
            stmt.execute("set session_replication_role = 'replica'");
        }
        try (var connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            this.tableInfos = getTableInfos(connection, schema);
        }
        this.getFilePath = (tableName) -> Path.of(tableName + ".json");
    }

    @Override
    @SneakyThrows
    public void truncate() {
        try (var stmt = this.connection.createStatement()) {
            for (var tableName : tableInfos.keySet()) {
                stmt.execute("TRUNCATE TABLE %s.%s RESTART IDENTITY CASCADE".formatted(this.schema, tableName));
            }
        }
    }

    @Override
    @SneakyThrows
    public void seedResource(String name) {
        var url = Objects.requireNonNull(this.getClass().getResource(name));

        for (var tableName : tableInfos.keySet()) {
            var filePath = Path.of(url.getPath()).resolve(this.getFilePath.apply(tableName));
            if (!filePath.toFile().exists()) {
                continue;
            }

            var items =
                    mapper.readValue(filePath.toFile(), new TypeReference<List<LinkedHashMap<String, Object>>>() {});
            if (items.isEmpty()) {
                continue;
            }

            var tableInfo = tableInfos.get(tableName);

            for (var item : items) {
                var columnText = item.keySet().stream().map("\"%s\""::formatted).collect(Collectors.joining(","));
                var placeholderText = item.keySet().stream()
                        .map(k -> switch (tableInfo.columnTypes().get(k)) {
                            case "json" -> "?::json";
                            case "jsonb" -> "?::jsonb";
                            default -> "?";
                        })
                        .collect(Collectors.joining(","));
                var sql = "INSERT INTO %s.%s (%s) OVERRIDING SYSTEM VALUE VALUES (%s)"
                        .formatted(this.schema, tableName, columnText, placeholderText);

                try (var stmt = this.connection.prepareStatement(sql)) {
                    var i = 1;
                    for (var entry : item.entrySet()) {
                        stmt.setObject(
                                i,
                                switch (tableInfo.columnTypes().get(entry.getKey())) {
                                    case "json", "jsonb" -> mapper.writeValueAsString(entry.getValue());
                                    default -> entry.getValue();
                                });
                        i++;
                    }
                    stmt.executeUpdate();
                }
            }
        }
    }

    public PostgresSeeder withGetFilePath(Function<String, Path> getFilePath) {
        this.getFilePath = getFilePath;
        return this;
    }

    @SneakyThrows
    private static Map<String, TableInfo> getTableInfos(Connection connection, String schema) {
        var result = new HashMap<String, TableInfo>();

        var metaData = connection.getMetaData();
        try (var resultSet = metaData.getTables(null, schema, null, new String[] {"TABLE"})) {
            while (resultSet.next()) {
                var tableName = resultSet.getString("TABLE_NAME");
                var columnTypes = new HashMap<String, String>();

                try (var columns = metaData.getColumns(null, schema, tableName, null)) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String columnType = columns.getString("TYPE_NAME");
                        columnTypes.put(columnName, columnType);
                    }
                }

                result.put(tableName, new TableInfo(columnTypes));
            }
        }

        return result;
    }

    private record TableInfo(Map<String, String> columnTypes) {}
}
