package sql;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlLiteral extends SqlColumn {
    public SqlLiteral(String literal, String alias) {
        super(null, null, alias);
        this.literal = literal;
        this.replacementValues = null;
    }

    public SqlLiteral(String literal) {
        super(null, null);
        this.literal = literal;
        this.replacementValues = null;
    }

    private SqlLiteral(String literal, String alias, List<Object> values) {
        super(null, null, alias);
        this.literal = literal;
        this.replacementValues = values;
    }

    public String qualifiedName() {
        return literal;
    }

    public SqlLiteral using(Object[] vals) {
        List<Object> data = this.replacementValues == null ? new ArrayList<>() : this.replacementValues;
        data.addAll(Arrays.asList(vals));
        return new SqlLiteral(literal, alias, data);
    }
    public SqlLiteral using(Object val) {
        return using(new Object[] { val });
    }

    @Expose public final String literal;
    @Expose public final List<Object> replacementValues;
}
