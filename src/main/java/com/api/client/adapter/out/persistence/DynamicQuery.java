package com.api.client.adapter.out.persistence;

import org.springframework.r2dbc.core.DatabaseClient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class DynamicQuery {

    private final StringBuilder sql;
    private final List<UnaryOperator<DatabaseClient.GenericExecuteSpec>> bindings = new ArrayList<>();

    public DynamicQuery(String baseSql) {
        this.sql = new StringBuilder(baseSql);
    }

    public void addLikeFilter(String column, String param, String value) {
        if (value != null && !value.isBlank()) {
            sql.append(" AND LOWER(").append(column).append(") LIKE :").append(param);
            bindings.add(q -> q.bind(param, "%" + value.toLowerCase() + "%"));
        }
    }

    public void addFilter(String column, String param, Object value) {
        if (value != null) {
            sql.append(" AND ").append(column).append(" >= :").append(param);
            bindings.add(q -> q.bind(param, value));
        }
    }

    public void addRangeFilter(String column, String param, Object value, boolean isUpper) {
        if (value != null) {
            String op = isUpper ? "<=" : ">=";
            sql.append(" AND ").append(column).append(" ").append(op).append(" :").append(param);
            bindings.add(q -> q.bind(param, value));
        }
    }

    public DatabaseClient.GenericExecuteSpec apply(DatabaseClient db, int limit, int offset) {
        sql.append(" ORDER BY data_added DESC");
        sql.append(" LIMIT :limit OFFSET :offset");
        DatabaseClient.GenericExecuteSpec query = db.sql(sql.toString());
        for (var binding : bindings) {
            query = binding.apply(query);
        }
        return query.bind("limit", limit).bind("offset", offset);
    }
}