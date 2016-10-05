package helpers;

import org.json.JSONObject;
import sql.*;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public final class SqlBuilder {
    public static class SqlTableInfo {
        public SqlTableInfo(SqlTable table, SqlJoin join, String dependentTable) {
            sqlTable = table;
            sqlJoin = join;
            this.dependentTable = dependentTable;
        }

        public SqlTable sqlTable;
        public SqlJoin sqlJoin;
        public String dependentTable;
        public boolean joined = false;
    }

    public static class SqlColumnDetails {
        public SqlColumnDetails(SqlColumn c, SqlTableInfo i) {
            column = c;
            info = i;
        }

        public SqlColumn column;
        public SqlTableInfo info;
    }

    public static SqlColumnDetails getColumn(String column, SqlTable mainTable, Map<String, SqlTableInfo> tableMap) {
        SqlColumn sqlColumn;
        if ((sqlColumn = mainTable.getColumn(column)) != null) {
            return new SqlColumnDetails(sqlColumn, null);
        } else if (tableMap != null) {
            boolean compound = column.contains(".");
            for (Map.Entry<String, SqlTableInfo> entity : tableMap.entrySet()) {
                String key = entity.getKey();
                if (compound && column.startsWith(key)) {
                    String newCol = StringUtils.stripStart(column.substring(key.length()), ".");
                    SqlTableInfo info = entity.getValue();
                    return new SqlColumnDetails(info.sqlTable.getColumn(newCol), info);
                } else {
                    SqlTableInfo info = entity.getValue();
                    if ( (sqlColumn = info.sqlTable.getColumn(column)) != null) {
                        return new SqlColumnDetails(sqlColumn, info);
                    }
                }
            }
        }
        return new SqlColumnDetails(null, null);
    }

    public static SqlQuery buildOrderFromString(SqlQuery query, String orders, SqlTable mainTable, Map<String, SqlTableInfo> tableMap) {
        if ( orders != null ) {
            SqlColumnDetails details;
            for (String order : orders.split(";")) {
                String[] parts = order.split("\\.");
                details = getColumn(parts[0], mainTable, tableMap);
                if (details.column != null) {
                    joinDetails(query, details.info, tableMap);
                    query.orderBy(details.column.order(parts.length > 1 ? parts[1] : "ASC"));
                }
            }
        }
        return query;
    }

    public static void joinDetails(SqlQuery query, SqlTableInfo info, Map<String, SqlTableInfo> tableMap) {
        if ( info != null && !info.joined ) {
            if ( info.dependentTable != null && info.dependentTable.length() > 1) {
                joinDetails(query, tableMap.get(info.dependentTable), tableMap);
            }
            query.join(info.sqlJoin);
            info.joined = true;
        }
    }
    public static SqlQuery buildFromQueryStrings(String select, String filter, String orders, int pageNo, int pageSize, SqlTable mainTable,
            Map<String, SqlTableInfo> tableMap)
            throws Exception {
        SqlColumn sqlColumn;
        SqlColumnDetails details;
        SqlQuery query = new SqlQuery().from(mainTable);

        if ( select != null ) {
            String[] columns = select.split(",");
            for (String column : columns) {
                details = getColumn(column, mainTable, tableMap);
                if (details.column != null) {
                    query.select(details.column.as(column));
                    joinDetails(query, details.info, tableMap);
                }
            }
        }

        //"businessName.like.bubba,businessName.like.boy;businessNumber>10001";
        if ( filter != null ) {
            SqlWhere where = new SqlWhere();
            Class sqlColumnClass = Class.forName("sql.SqlColumn");
            Class[] params = new Class[1];
            params[0] = Object.class;
            String[] ors = filter.split(";");
            for (String or : ors) {
                SqlWhere orPiece = new SqlWhere();
                for (String comparison : or.split(",")) {
                    String[] parts = comparison.split("\\.");
                    // 0 = column, 1 = op, 2 = value (ex: businessName.like.bubba)
                    details = getColumn(parts[0], mainTable, tableMap);
                    sqlColumn = details.column;
                    if (details.column != null) {
                        joinDetails(query, details.info, tableMap);
                    }
                    if (sqlColumn != null) {
                        // special case ops
                        switch(parts[1]) {
                            case "between":
                                Class[] tmpParams = new Class[2];
                                tmpParams[0] = Object.class;
                                tmpParams[1] = Object.class;
                                orPiece.or((SqlWhere) (sqlColumnClass.getDeclaredMethod(parts[1], tmpParams).invoke(sqlColumn, parts[2], parts[3])));
                                break;
                            case "isNull":
                            case "isNotNull":
                                orPiece.or((SqlWhere) (sqlColumnClass.getDeclaredMethod(parts[1], (Class[])null).invoke(sqlColumn)));
                                break;
                            default:
                                orPiece.or((SqlWhere) (sqlColumnClass.getDeclaredMethod(parts[1], params).invoke(sqlColumn, parts[2])));
                                break;
                        }
                    }
                }
                where.and(orPiece);
            }
            query.where(where);
        }
        if ( pageNo > 0 || pageSize > 0 ) {
            query.pageNo(pageNo).pageSize(pageSize);
        }

        buildOrderFromString(query, orders, mainTable, tableMap);

        return query;
    }

    public static class InsertUpdateDetails {
        public InsertUpdateDetails(String sql, List<Object> values) {
            this.sql = SqlSettings.current().decrypter.openKey() + sql;
            this.values = values;
            this.errors = null;
        }
        public InsertUpdateDetails(List<String> errors) {
            this.values = null;
            this.sql = null;
            this.errors = errors;
        }
        public final List<Object> values;
        public final String sql;
        public final List<String> errors;
    }

    private static Map<String, validators.Base> _validators = new HashMap<>();
    public static validators.Base addValidator(SqlTable table, validators.Base validator) {
        validators.Base old = null;
        if ( _validators.containsKey(table.getTableName())) {
            old = _validators.get(table.getTableName());
        }
        _validators.put(table.getTableName(), validator);
        return old;
    }

    private Map<String, List<String>> _tableNameToReadonly = new HashMap<>();
    private List<String> readOnlyColNames(SqlTable table) {
        if ( !_tableNameToReadonly.containsKey(table.getTableName())) {
            List<String> readonly = new ArrayList<>();
            _tableNameToReadonly.put(table.getTableName(), readonly);
            // TODO: get readonly fields for this table and cache them
        }

        return _tableNameToReadonly.get(table.getTableName());
    }

    public InsertUpdateDetails buildInsert(JSONObject data, SqlTable table, SqlColumn id, String idValue, List<String> ignoreCols) throws Exception {
        return SqlBuilder.buildInsert(data, table, id, idValue, ignoreCols, readOnlyColNames(table));
    }
    public InsertUpdateDetails buildUpdate(JSONObject data, SqlTable table, SqlColumn id, String idValue, List<String> ignoreCols) throws Exception {
        return SqlBuilder.buildUpdate(data, table, id, idValue, ignoreCols, readOnlyColNames(table));
    }

    public static InsertUpdateDetails buildInsert(JSONObject data, SqlTable table, SqlColumn id, String idValue, List<String> ignoreCols, List<String> readOnlyColNames) throws Exception {
        removeReadOnly(data, readOnlyColNames);
        if ( _validators.containsKey(table.getTableName())) {
            List<String> errors = _validators.get(table.getTableName()).validate(data, true);
            if ( errors != null && !errors.isEmpty()) {
                return new InsertUpdateDetails(errors);
            }
        }

        List<Object> values = new ArrayList<>();
        List<String> colNames = new ArrayList<>();
        List<String> colValues = new ArrayList<>();

        colNames.add(id.getColumnName());
        colValues.add(id.getReplacement("?"));
        values.add(idValue);

        Iterator<?> keys = data.keys();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            if (ignoreCols.contains(key)) {
                continue;
            }
            SqlColumn column = table.getColumn(key);
            if ( column == null) {
                continue;
            }
            //colNames.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key));
            colNames.add(column.getColumnName());
            colValues.add(column.getReplacement("?"));
            values.add(data.get(key));
        }
        String insert = String.format("INSERT INTO %s (%s) VALUES (%s)", table.getTableName(), StringUtils.join(colNames.toArray(), ","), StringUtils.join(colValues.toArray(), ","));
        return new InsertUpdateDetails(insert, values);
    }

    public static InsertUpdateDetails buildUpdate(JSONObject data, SqlTable table, SqlColumn id, String idValue, List<String> ignoreCols, List<String> readOnlyColNames) throws Exception {
        removeReadOnly(data, readOnlyColNames);
        if ( _validators.containsKey(table.getTableName())) {
            List<String> errors = _validators.get(table.getTableName()).validate(data, false);
            if ( errors != null && !errors.isEmpty()) {
                return new InsertUpdateDetails(errors);
            }
        }


        List<Object> values = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        Iterator<?> keys = data.keys();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            if (ignoreCols.contains(key)) {
                continue;
            }
            SqlColumn column = table.getColumn(key);
            if ( column == null) {
                continue;
            }
            columns.add(String.format("%s = %s", column.getColumnName(), column.getReplacement("?")));
            values.add(data.get(key));
        }
        values.add(idValue);
        String update = String.format("UPDATE %s SET %s WHERE %s = %s", table.getTableName(), StringUtils.join(columns.toArray(), ","), id.getColumnName(), id.getReplacement("?"));
        return new InsertUpdateDetails(update, values);
    }

    private static void removeReadOnly(JSONObject data, List<String> readOnlyColNames) throws Exception {
        Iterator<?> keys = data.keys();
        List<String> remove = new ArrayList<>();
        while(keys.hasNext()) {
            String key = (String) keys.next();
            String col = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key);
            if (readOnlyColNames.contains(col)) {
                remove.add(key);
            }
        }
        for(String key: remove) {
            data.remove(key);
        }
    }
}
