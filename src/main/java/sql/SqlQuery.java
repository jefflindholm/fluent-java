package sql;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqlQuery {

    public SqlQuery select(SqlColumn... columns) {
        for (SqlColumn column : columns) {
            _columns.add(column);
        }
        return this;
    }

    public SqlQuery from(SqlTable... tables) {
        for(SqlTable table : tables ) {
            _from.add(table);
        }
        return this;
    }

    public SqlQuery join(SqlJoin join) {
        _joins.add(join);
        return this;
    }

    public SqlQuery left(SqlJoin join) {
        join.setType(SqlJoin.Type.left);
        _joins.add(join);
        return this;
    }

    public SqlQuery right(SqlJoin join) {
        join.setType(SqlJoin.Type.right);
        _joins.add(join);
        return this;
    }

    public SqlQuery where(SqlWhere where) {
        _wheres.add(where);
        return this;
    }

    public SqlQuery orderBy(Object... orders) {
        for (Object order : orders) {
            if (order instanceof SqlColumn) {
                _orders.add(((SqlColumn) order).asc());
            } else {
                _orders.add((SqlOrder) order);
            }
        }
        return this;
    }

    public SqlQuery distinct() {
        _distinct = true;
        return this;
    }

    public SqlQuery pageNo(int page) {
        _pageNo = page > 0 ? page : 1;
        return this;
    }

    public SqlQuery pageSize(int pageSize) {
        _pageSize = pageSize > 0 ? pageSize : SqlSettings.current().defaultPageSize;
        return this;
    }

    public SqlQuery top(int top) {
        _top = top;
        return this;
    }

    @Expose private List<SqlColumn> _columns = new ArrayList<>();
    @Expose private List<SqlColumn> _groupBy = new ArrayList<>();
    @Expose private List<SqlTable> _from = new ArrayList<>();
    @Expose private List<SqlJoin> _joins = new ArrayList<>();
    @Expose private List<SqlWhere> _wheres = new ArrayList<>();
    @Expose private List<SqlOrder> _orders = new ArrayList<>();
    @Expose private boolean _distinct = false;
    @Expose private Integer _pageNo = null;
    @Expose private Integer _pageSize = null;
    @Expose private Integer _top = null;

    private List<Object> _values = null;

    public SqlColumn getByAlias(String alias) {
        for(SqlColumn col : _columns) {
            if ( col.getAlias().equals(SqlSettings.escapeAlias(alias)) ) {
                return col;
            }
        }
        return null;
    }
    public Boolean hasColumnAlias(String alias) {
        return getByAlias(alias) != null;
    }

    public SqlSimpleStatement buildSimple() {
        return buildSimple(new HashMap<>());
    }
    public SqlSimpleStatement buildSimple(Map<SqlColumn, Integer> masks) {
        buildSqlParts(masks);
        String format;

        if (orderClause != null && orderClause.length() > 0) {
            format = "SELECT%s%s%s%nFROM%s%s%s%nORDER BY%s";
        } else {
            format = "SELECT%s%s%s%nFROM%s%s%s%s";
        }
        String sql = String
                .format(format, (_distinct ? " DISTINCT" : ""), (_top != null ? " TOP " + _top : ""), columns, fromClause, whereClause, groupByClause, orderClause);
        return new SqlSimpleStatement(sql, _values);
    }

    public SqlCompoundStatement buildCompound() throws FluentException {
        return buildCompound(new HashMap<>());
    }
    public SqlCompoundStatement buildCompound(Map<SqlColumn, Integer> masks) throws FluentException {
        buildSqlParts(masks);
        String baseSql = String.format("SELECT%s%s%s%nFROM%s%s%s",
                        (_distinct ? " DISTINCT" : ""),
                        (_top != null ? " TOP " + _top : ""),
                        columns,
                        fromClause,
                        whereClause,
                        groupByClause);
        if (_pageNo == null && _pageSize == null) {
            if (orderClause != null && orderClause.length() > 0) {
                return new SqlCompoundStatement(baseSql + newLine + "ORDER BY" + orderClause, null, _values);
            } else {
                return new SqlCompoundStatement(baseSql, null, _values);
            }
        }
        if (_pageNo == null) {
            _pageNo = 1;
        }
        if (_pageSize == null ) {
            _pageSize = SqlSettings.current().defaultPageSize;
        }
        if (StringUtils.isEmpty(aliasOrderClause)) {
            throw new FluentException("Cannot page an unordered query");
        }
        String countSql = String.format("SELECT count(*) as RecordCount FROM (%n%s%n) count_sql", baseSql);
        String pagingSql = String.format("SELECT *, row_number() OVER (ORDER BY %s) as Paging_RowNumber FROM (%n%s%n) base_sql", aliasOrderClause, baseSql);
        String pagedSql = String
                .format("SELECT * FROM (%n%s%n) detail_sql WHERE Paging_RowNumber BETWEEN %d and %d", pagingSql, (_pageNo - 1) * _pageSize,
                        _pageNo * _pageSize);
        return new SqlCompoundStatement(pagedSql, countSql, _values);
    }

    private String columns;
    private StringBuilder whereClause;
    private StringBuilder fromClause;
    private String groupByClause;
    private String orderClause;
    private String aliasOrderClause;
    private static final String newLine = System.lineSeparator();

    private void buildSqlParts(Map<SqlColumn, Integer> masks) {
        _values = new ArrayList<>();
        _groupBy = new ArrayList<>();

        columns = _columns.stream().map(column -> {
            if ( column instanceof SqlLiteral && ((SqlLiteral)column).replacementValues != null ) {
                _values.addAll(((SqlLiteral) column).replacementValues);
            }
            if (column.isGroupedBy()) {
                _groupBy.add(column);
            }
            return newLine + column.getSelectString(masks);
        }).collect(Collectors.joining(","));

        String groupBy = _groupBy.stream().map(column -> newLine + column.qualifiedName()).collect(Collectors.joining(","));
        if ( groupBy != null && !groupBy.isEmpty() ) {
            groupByClause = String.format("%nGROUP BY%s", groupBy);
        } else {
            groupByClause = "";
        }

        fromClause = new StringBuilder();
        fromClause.append(_from
                            .stream()
                            .map(table -> newLine + table.getFrom())
                            .collect(Collectors.joining(",")));

        if (_joins != null && _joins.size() > 0) {
            for (SqlJoin join : _joins) {
                fromClause.append(newLine);
                switch (join.getType()) {
                case inner:
                    fromClause.append("JOIN ");
                    break;
                case left:
                    fromClause.append("LEFT JOIN ");
                    break;
                case right:
                    fromClause.append("RIGHT JOIN ");
                    break;
                }
                fromClause.append(String.format("%s on %s = %s", join.from.getTable().getFrom(), join.from.qualifiedName(), join.to.qualifiedName()));
            }
        }

        whereClause = new StringBuilder();
        if (_wheres != null && _wheres.size() > 0) {
            whereClause.append(newLine);
            whereClause.append("WHERE");
            whereClause.append(buildWhere(_wheres, SqlWhere.Type.and));
        }

        orderClause = _orders.stream()
                .map(o -> String.format("%n%s %s", o.column.qualifiedName(), o.direction.toString()))
                .collect(Collectors.joining(","));

        aliasOrderClause = _orders.stream()
                .map(o -> String.format("%n%s %s", SqlSettings.escapeAlias(o.column.getAlias()), o.direction.toString()))
                .collect(Collectors.joining(","));
    }

    private String buildWhere(List<SqlWhere> wheres, SqlWhere.Type conjunction) {
        StringBuilder sql = new StringBuilder();
        boolean first = true;
        for (SqlWhere where : wheres) {
            if (!first) {
                sql.append(newLine + (conjunction == SqlWhere.Type.or ? "OR " : "AND "));
            } else {
                sql.append(newLine);
            }
            first = false;
            sql.append(where.build(_values));
        }
        return sql.toString();
    }

    public SqlQuery updateAlias(SqlColumn column, SqlColumn newColumn) {
        for(int i = 0; i < _columns.size(); i++) {
            if ( _columns.get(i) == column ) {
                _columns.set(i, newColumn);
                break;
            }
        }
        return this;
    }
}
