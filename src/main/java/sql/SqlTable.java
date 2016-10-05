package sql;

import com.google.gson.annotations.Expose;
import helpers.Strings;

public abstract class SqlTable implements ISqlTable {
    public SqlTable(String tableName) {
        this.tableName = tableName;
        this.alias = tableName;
    }

    protected SqlTable(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    public SqlColumn on(SqlColumn column) {
        if (column.getTable() == this) {
            return column;
        } else {
            return null;
        }
    }

    public String getTableName() {
        return SqlSettings.escapeTable(tableName);
    }

    public String getAlias() {
        return SqlSettings.escapeAlias(alias);
    }

    public String getFrom() {
        return String.format("%s as %s", getTableName(), getAlias());
    }

    @Expose
    private final String tableName;
    @Expose
    private final String alias;

    public abstract SqlColumn[] star();

    public abstract SqlTable as(String alias);

    public SqlColumn getColumn(String camelCaseName) {
        String snake = Strings.toSnakeCase(camelCaseName);
        for (SqlColumn column : this.star()) {
            String name = column.getColumnName();
            String alias = column.getAlias();
            if (name != null && (name.equalsIgnoreCase(snake) || name.equalsIgnoreCase(camelCaseName) || alias.equalsIgnoreCase(snake) || alias.equalsIgnoreCase(camelCaseName))) {
                return column;
            }
        }
        return null;
    }
}
