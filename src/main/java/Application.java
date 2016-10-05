import sql.*;

import java.util.*;

public class Application {

    public static class Businesses extends SqlTable {
        public static final String TableName = "businesses";
        public Businesses() { super(TableName); setConverters(); }
        private Businesses(String alias) { super(TableName, alias); setConverters(); }
        public Businesses as(String alias) { return new Businesses(alias); }
        private void setConverters() {}
        public final SqlColumn federalTaxId = new SqlColumn(this, "federalTaxId").as("federalTaxId");
        public final SqlColumn id = new SqlColumn(this, "id").as("id");
        public final SqlColumn businessName = new SqlColumn(this, "businessName").as("businessName");
        public final SqlColumn businessNumber = new SqlColumn(this, "businessNumber").as("businessNumber");
        public SqlColumn[] star() { return new SqlColumn[] { id, businessName, businessNumber, federalTaxId }; }
    }


    public static void main(String[] args) throws Exception {
        String a = "123";
        String b = "123";
        String c = new String("123");
        System.out.println(a == b);
        System.out.println(a == c);

//        SqlSettings.current().decrypter = new Encrypted();
//        testInserUpdate();
//        Businesses b = new Businesses();
//        String test = b.stateTaxId.getReplacement("?");
//        System.out.println(test);
        masks();
        businesses();
    }


    public static void masks() throws Exception {
        Map<SqlColumn, Integer> masks = new HashMap<>();

        Businesses b = new Businesses();
        masks.put(b.federalTaxId, 4);
        SqlQuery query = new SqlQuery()
                .select(b.id, b.federalTaxId, b.businessName, b.businessNumber)
                .from(b);
        query.pageNo(1).pageSize(10).orderBy(b.businessName);
        SqlCompoundStatement sql = query.buildCompound(masks);
        System.out.println(sql.dataSql);
    }

    public static void businesses() throws Exception {
        Businesses b = new Businesses();
        SqlQuery query = new SqlQuery()
                .select(b.star())
                .from(b);
        query.pageNo(1).pageSize(10).orderBy(b.businessName);
        SqlCompoundStatement sql = query.buildCompound();
        System.out.println(sql.dataSql);
    }

}
