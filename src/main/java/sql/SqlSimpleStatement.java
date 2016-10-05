package sql;

import java.util.ArrayList;
import java.util.List;

public class SqlSimpleStatement {
    public SqlSimpleStatement(String sql, List<Object> values) {
        this.sql = sql;
        this.values.addAll(values);
    }

    public final String sql;
    public final List<Object> values = new ArrayList<>();
}
