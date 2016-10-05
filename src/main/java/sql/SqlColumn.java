package sql;

import com.google.gson.annotations.Expose;

import java.util.Map;

public class SqlColumn {
    public SqlColumn(ISqlTable table, String columnName) {
        this.table = table;
        this.columnName = columnName;
        this.alias = columnName;
    }

    protected SqlColumn(ISqlTable table, String columnName, String alias) {
        this.table = table;
        this.columnName = columnName;
        this.alias = alias;
    }

    public SqlColumn as(String alias) {
        if ( this instanceof SqlLiteral ) {
            return new SqlLiteral(((SqlLiteral)this).literal, alias);
        }
        return new SqlColumn(this.table, this.columnName, alias);
    }
    public SqlColumn groupBy() {
        SqlColumn ret;
        if ( this instanceof SqlLiteral ) {
            ret = new SqlLiteral(((SqlLiteral)this).literal, alias);
        } else {
            ret = new SqlColumn(this.table, this.columnName, alias);
        }
        ret.groupBy = true;
        return ret;
    }

    public SqlJoin using(SqlColumn column) {
        return new SqlJoin(this, column);
    }

    @Expose private final ISqlTable table;
    @Expose private final String columnName;
    @Expose protected final String alias;
    @Expose private Boolean groupBy = false;

    public String getAlias() {
        return SqlSettings.escapeAlias(alias);
    }
    public String getColumnName() { return SqlSettings.escapeColumn(columnName); }
    public ISqlTable getTable() { return this.table; }
    public Boolean isGroupedBy() { return this.groupBy; }

    public String qualifiedName() {
        return String.format("%s.%s", table.getAlias(), getColumnName());
    }
    public String getSelectString(Map<SqlColumn, Integer> masks) {
        String columnValue;
        if ( this instanceof SqlLiteral ) {
            columnValue = '('+((SqlLiteral) this).literal+')';
        } else {
            columnValue = SqlSettings.decrypt(this, qualifiedName());
            for (Map.Entry<SqlColumn, Integer> entry : masks.entrySet()) {
                SqlColumn mask = entry.getKey();
                if (mask.getTable().getTableName().equals(getTable().getTableName()) && mask.getColumnName().equals(getColumnName())) {
                    int value = entry.getValue();
                    if (value > 0) {
                        columnValue = String.format("(LEFT(%s, %d)%s'*')", columnValue, value, SqlSettings.concat());
                    } else {
                        columnValue = String.format("('*'%sRIGHT(%s, %d))", SqlSettings.concat(), columnValue, -value);
                    }
                }
            }
        }
        return (columnValue + " as " + getAlias());
    }

    public SqlWhere lt(Object value) {
        return new SqlWhere(this, "<", value);
    }

    public SqlWhere lte(Object value) {
        return new SqlWhere(this, "<=", value);
    }

    public SqlWhere gt(Object value) {
        return new SqlWhere(this, ">", value);
    }

    public SqlWhere gte(Object value) {
        return new SqlWhere(this, ">=", value);
    }

    public SqlWhere eq(Object value) {
        return new SqlWhere(this, "=", value);
    }

    public SqlWhere neq(Object value) {
        return new SqlWhere(this, "!=", value);
    }

    public SqlWhere like(Object value) {
        return new SqlWhere(this, "like", '%' + value.toString() + '%');
    }

    public SqlWhere between(Object value1, Object value2) throws Exception {
        return (this.gte(value1).and(this.lte(value2)));
    }

    public SqlWhere in(Object value) {
        return new SqlWhere(this, "in", value);
    }

    public SqlWhere isNull() {
        return new SqlWhere(this, "isNull", null);
    }

    public SqlWhere isNotNull() {
        return new SqlWhere(this, "isNotNull", null);
    }

    public SqlOrder asc() {
        return new SqlOrder(this, SqlOrder.Direction.asc);
    }

    public SqlOrder desc() {
        return new SqlOrder(this, SqlOrder.Direction.desc);
    }

    public SqlOrder order(String dir) {
        return new SqlOrder(this, dir.toUpperCase().equals("ASC") ? SqlOrder.Direction.asc : SqlOrder.Direction.desc);
    }

    private String converter = null;

    public void setConverter(String converter) {
        this.converter = converter;
    }

    public String getReplacement(String identifier) {
        if (converter != null) {
            identifier = String.format("%s(%s)", converter, identifier);
        }
        return SqlSettings.encrypt(this, identifier);
    }
}
