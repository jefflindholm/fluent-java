package sql;

import java.util.ArrayList;
import java.util.List;

public class SqlCompoundStatement {
    public SqlCompoundStatement(String dataSql, String countSql, List<Object> values) {
        this.dataSql = dataSql;
        this.countSql = countSql;
        this.values = new ArrayList<>();
        this.values.addAll(values);
    }

    public final String dataSql;
    public final String countSql;
    public final List<Object> values;
}
