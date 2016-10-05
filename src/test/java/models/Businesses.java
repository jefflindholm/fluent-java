package models;

import sql.SqlColumn;
import sql.SqlTable;

public class Businesses extends SqlTable {
    public static final String TableName = "businesses";
    public Businesses() { super(TableName); setConverters(); }
    private Businesses(String alias) { super(TableName, alias); setConverters(); }
    public Businesses as(String alias) { return new Businesses(alias); }
    private void setConverters() {}
    public final SqlColumn id = new SqlColumn(this, "id").as("id");

    public final SqlColumn accountManagerUserId = new SqlColumn(this, "account_manager_user_id").as("accountManagerUserId");
    public final SqlColumn businessEmail = new SqlColumn(this, "business_email").as("businessEmail");
    public final SqlColumn businessName = new SqlColumn(this, "business_name").as("businessName");
    public final SqlColumn businessNumber = new SqlColumn(this, "business_number").as("businessNumber");
    public final SqlColumn federalTaxId = new SqlColumn(this, "federal_tax_id").as("federalTaxId");
    public final SqlColumn legalName = new SqlColumn(this, "legal_name").as("legalName");
    public final SqlColumn mainAddressId = new SqlColumn(this, "main_address_id").as("mainAddressId");
    public final SqlColumn stateTaxId = new SqlColumn(this, "state_tax_id").as("stateTaxId");
    public SqlColumn[] star() { return new SqlColumn[] { id, accountManagerUserId, businessEmail, businessName, businessNumber, federalTaxId, legalName, mainAddressId, stateTaxId }; }
}

