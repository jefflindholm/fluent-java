package models;
import sql.SqlColumn;
import sql.SqlTable;


public class States extends SqlTable {
    public static final String TableName = "states";
    public States() { super(TableName); setConverters(); }
    private States(String alias) { super(TableName, alias); setConverters(); }
    public States as(String alias) { return new States(alias); }
    private void setConverters() {}
    public final SqlColumn abbreviation = new SqlColumn(this, "abbreviation").as("abbreviation");
    public final SqlColumn countryId = new SqlColumn(this, "country_id").as("countryId");
    public final SqlColumn id = new SqlColumn(this, "id").as("id");
    public final SqlColumn name = new SqlColumn(this, "name").as("name");
    public SqlColumn[] star() { return new SqlColumn[] { abbreviation, countryId, id, name}; }
}
