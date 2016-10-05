package sql;

import com.google.gson.annotations.Expose;

public class SqlOrder {
    public SqlOrder(SqlColumn column) {
        this(column, Direction.asc);
    }

    public SqlOrder(SqlColumn column, Direction direction) {
        this.column = column;
        this.direction = direction;
    }

    @Expose public final SqlColumn column;

    enum Direction {
        asc,
        desc
    }

    @Expose public final Direction direction;
}
