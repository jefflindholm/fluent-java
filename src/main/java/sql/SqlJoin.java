package sql;

import com.google.gson.annotations.Expose;

public class SqlJoin {
    public SqlJoin(SqlColumn from, SqlColumn to) {
        this.from = from;
        this.to = to;
    }

    @Expose public final SqlColumn from;
    @Expose public final SqlColumn to;

    public enum Type {
        inner,
        left,
        right
    }

    public void setType(Type t) {
        joinType = t;
    }

    public Type getType() {
        return joinType;
    }

    @Expose private Type joinType = Type.inner;
}
