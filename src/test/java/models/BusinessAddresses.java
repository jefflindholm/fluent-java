package models;
import sql.SqlColumn;
import sql.SqlTable;


public class BusinessAddresses extends SqlTable {
    public static final String TableName = "business_addresses";
    public BusinessAddresses() { super(TableName); setConverters(); }
    private BusinessAddresses(String alias) { super(TableName, alias); setConverters(); }
    public BusinessAddresses as(String alias) { return new BusinessAddresses(alias); }
    private void setConverters() {}
    public final SqlColumn businessId = new SqlColumn(this, "business_id").as("businessId");
    public final SqlColumn city = new SqlColumn(this, "city").as("city");
    public final SqlColumn county = new SqlColumn(this, "county").as("county");
    public final SqlColumn fax = new SqlColumn(this, "fax").as("fax");
    public final SqlColumn id = new SqlColumn(this, "id").as("id");
    public final SqlColumn latitude = new SqlColumn(this, "latitude").as("latitude");
    public final SqlColumn line1 = new SqlColumn(this, "line1").as("line1");
    public final SqlColumn line2 = new SqlColumn(this, "line2").as("line2");
    public final SqlColumn longitude = new SqlColumn(this, "longitude").as("longitude");
    public final SqlColumn name = new SqlColumn(this, "name").as("name");
    public final SqlColumn number = new SqlColumn(this, "number").as("number");
    public final SqlColumn phone = new SqlColumn(this, "phone").as("phone");
    public final SqlColumn stateId = new SqlColumn(this, "state_id").as("stateId");
    public final SqlColumn zip = new SqlColumn(this, "zip").as("zip");
    public SqlColumn[] star() { return new SqlColumn[] {businessId, city, county, fax, id, latitude, line1, line2, longitude, name, number, phone, stateId, zip }; }
}
