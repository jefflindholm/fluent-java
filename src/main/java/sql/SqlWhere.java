package sql;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlWhere {

    public SqlWhere() {
        column = null;
        op = null;
        value = null;
    }

    public SqlWhere(SqlColumn column, String op, Object value) {
        this.column = column;
        this.op = op;
        this.value = value;
    }

    public SqlWhere or(SqlWhere where) throws Exception {
        if (type == Type.and) {
            throw new FluentException("Cannot add 'or' to 'and' group");
        }
        type = Type.or;
        return add(where);
    }

    public SqlWhere and(SqlWhere where) throws Exception {
        if (type == Type.or) {
            throw new FluentException("Cannot add 'and' to 'or' group");
        }
        type = Type.and;
        return add(where);
    }

    private SqlWhere add(SqlWhere where) {
        SqlWhere result = this;
        if (this.column != null) {
            result = new SqlWhere();
            result.type = this.type;
            this.type = Type.none;
            result.wheres.add(this);
        }
        result.wheres.add(where);
        return result;
    }

    @Expose
    public final SqlColumn column;
    @Expose
    public final String op;
    @Expose
    public final Object value;

    @Expose
    public List<SqlWhere> wheres = new ArrayList<>();

    enum Type {
        none,
        or,
        and
    }

    @Expose
    public Type type = Type.none;
    private static final String newLine = System.lineSeparator();

    @Override
    public String toString() {
        return build(new ArrayList<>());
    }

    public String build(List<Object> values) {
        if ( this.column != null ) {
            return SqlWhere.build(Arrays.asList(this), SqlWhere.Type.and, values);
        } else {
            return SqlWhere.build(this.wheres, this.type, values);
        }
    }

    private static String build(List<SqlWhere> wheres, SqlWhere.Type conjunction, List<Object> values) {
        StringBuilder sql = new StringBuilder();
        boolean first = true;
        for (SqlWhere where : wheres) {
            if (where.type == SqlWhere.Type.none && where.column == null && where.wheres.isEmpty()) {
                continue;
            }

            if (!first) {
                sql.append(newLine + (conjunction == SqlWhere.Type.or ? "OR " : "AND "));
            }
            first = false;

            if (where.column != null) {
                // if the replacement is in the literal, the ? for the literal will come first
                if ( where.column instanceof SqlLiteral && ((SqlLiteral)where.column).replacementValues != null ) {
                    values.addAll(((SqlLiteral) where.column).replacementValues);
                }

                if (where.value instanceof SqlColumn) {
                    SqlColumn col = (SqlColumn) where.value;
                    sql.append(String.format("%s %s (%s)", where.column.qualifiedName(), where.op, col.qualifiedName()));
                    if ( where.value instanceof SqlLiteral && ((SqlLiteral)where.value).replacementValues != null ) {
                        values.addAll(((SqlLiteral) where.value).replacementValues);
                    }
                } else {
                    if ( where.op.equals("isNull") ) {
                        sql.append(String.format("%s is null", where.column.qualifiedName()));
                    }else if ( where.op.equals("isNotNull") ) {
                        sql.append(String.format("%s is not null", where.column.qualifiedName()));
                    } else {
                        values.add(where.value);
                        sql.append(String.format("%s %s (%s)", where.column.qualifiedName(), where.op, where.column.getReplacement("?")));
                    }
                }
            }

            if (where.wheres != null && where.wheres.size() > 0) {
                String sub = build(where.wheres, where.type, values);
                if (sub.length() > 1) {
                    sql.append(String.format("(%s)", sub));
                }
            }
        }
        return sql.toString();
    }
}
