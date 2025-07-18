package io.github.chehsunliu.seeder.mariadb;

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

public class MariaDbSeeder implements Seeder {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Connection connection;
    private final Map<String, TableInfo> tableInfos;
    private Function<String, Path> getFilePath;

    @SneakyThrows
    public MariaDbSeeder(String jdbcUrl, String username, String password, String dbName) {
        this.connection = DriverManager.getConnection(jdbcUrl, username, password);
        try (var stmt = connection.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
        }
        try (var connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            this.tableInfos = getTableInfos(connection, dbName);
        }
        this.getFilePath = (tableName) -> Path.of(tableName + ".json");
    }

    @Override
    @SneakyThrows
    public void truncate() {
        try (var stmt = this.connection.createStatement()) {
            for (var tableName : tableInfos.keySet()) {
                stmt.execute("TRUNCATE TABLE " + tableName);
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
                var columnText = String.join(",", item.keySet());
                var placeholderText = item.keySet().stream().map(k -> "?").collect(Collectors.joining(","));
                var sql = "INSERT INTO %s (%s) VALUES (%s)".formatted(tableName, columnText, placeholderText);

                try (var stmt = this.connection.prepareStatement(sql)) {
                    var i = 1;
                    for (var entry : item.entrySet()) {
                        var columnName = entry.getKey();
                        if (tableInfo.columnTypes().get(columnName).equals("LONGTEXT")
                                && tableInfo.jsonColumns().contains(columnName)) {
                            stmt.setObject(i, mapper.writeValueAsString(entry.getValue()));
                        } else {
                            stmt.setObject(i, entry.getValue());
                        }
                        i++;
                    }
                    stmt.executeUpdate();
                }
            }
        }
    }

    public MariaDbSeeder withGetFilePath(Function<String, Path> getFilePath) {
        this.getFilePath = getFilePath;
        return this;
    }

    @SneakyThrows
    private static Map<String, TableInfo> getTableInfos(Connection connection, String dbName) {
        var allJsonColumns = getJsonColumns(connection, dbName);
        var result = new HashMap<String, TableInfo>();
        var metaData = connection.getMetaData();

        try (var resultSet = metaData.getTables(dbName, null, null, new String[] {"TABLE"})) {
            while (resultSet.next()) {
                var tableName = resultSet.getString("TABLE_NAME");
                var columnTypes = new HashMap<String, String>();

                try (var columns = metaData.getColumns(dbName, null, tableName, null)) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String columnType = columns.getString("TYPE_NAME");
                        columnTypes.put(columnName, columnType);
                    }
                }

                result.put(tableName, new TableInfo(columnTypes, allJsonColumns.get(tableName)));
            }
        }

        return result;
    }

    @SneakyThrows
    private static Map<String, Set<String>> getJsonColumns(Connection conn, String dbName) {
        var sql =
                """
        SELECT TABLE_NAME, CONSTRAINT_NAME
        FROM information_schema.check_constraints
        WHERE CONSTRAINT_SCHEMA = ?
          AND LEVEL = 'Column'
          AND CHECK_CLAUSE LIKE CONCAT('json_valid(`', CONSTRAINT_NAME, '`)')
        """;

        var result = new HashMap<String, Set<String>>();
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dbName); // Set CONSTRAINT_SCHEMA param

            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    var tableName = rs.getString("TABLE_NAME");
                    var constraintName = rs.getString("CONSTRAINT_NAME");

                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new HashSet<>());
                    }

                    result.get(tableName).add(constraintName);
                }
            }
        }
        return result;
    }

    private record TableInfo(Map<String, String> columnTypes, Set<String> jsonColumns) {}
}
