package io.github.chehsunliu.seeder.postgres.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import lombok.SneakyThrows;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class JsonTypeHandler<T> extends BaseTypeHandler<T> {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Class<T> type;

    public JsonTypeHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    @SneakyThrows
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) {
        ps.setObject(i, mapper.writeValueAsString(parameter), Types.OTHER);
    }

    @Override
    @SneakyThrows
    public T getNullableResult(ResultSet rs, String columnName) {
        String json = rs.getString(columnName);
        return json == null ? null : mapper.readValue(json, type);
    }

    @Override
    @SneakyThrows
    public T getNullableResult(ResultSet rs, int columnIndex) {
        String json = rs.getString(columnIndex);
        return json == null ? null : mapper.readValue(json, type);
    }

    @Override
    @SneakyThrows
    public T getNullableResult(CallableStatement cs, int columnIndex) {
        String json = cs.getString(columnIndex);
        return json == null ? null : mapper.readValue(json, type);
    }
}
