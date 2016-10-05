package sql;

import junit.framework.TestCase;
import models.$;
import models.BusinessAddresses;
import models.Businesses;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class SqlQueryTest {

    @Test
    public void testSelect() throws Exception {
        Businesses b = $.businesses.as("b");
        SqlQuery query = new SqlQuery()
                .from(b)
                .select(b.id)
                .select(b.businessName,
                        b.accountManagerUserId,
                        new SqlLiteral("SELECT id FROM business_addresses").as("someId"))
                ;
        SqlSimpleStatement sql = query.buildSimple();
        assertEquals(sql.sql, String.format("SELECT%n"+
                "b.id as id,%n"+
                "b.business_name as businessName,%n"+
                "b.account_manager_user_id as accountManagerUserId,%n"+
                "(SELECT id FROM business_addresses) as someId%n"+
                "FROM%n"+
                "businesses as b"));
    }

    @Test
    public void testGroupBy() throws Exception {
        Businesses b = $.businesses.as("b");
        SqlQuery query = new SqlQuery()
                .from(b)
                .select(b.accountManagerUserId.groupBy(),
                        new SqlLiteral("count(*)").as("count"))
                ;
        SqlSimpleStatement sql = query.buildSimple();
        String expected = String.format("SELECT%n"+
                "b.account_manager_user_id as accountManagerUserId,%n"+
                "(count(*)) as count%n"+
                "FROM%n"+
                "businesses as b"+
                "%nGROUP BY%nb.account_manager_user_id");
        assertEquals(sql.sql, expected);
        SqlCompoundStatement compound = query.orderBy(b.accountManagerUserId).pageSize(10).buildCompound();
        String fetchSql = String.format(
                "SELECT * FROM (%n"
                + "SELECT *, row_number() OVER (ORDER BY %n"
                + "accountManagerUserId asc) as Paging_RowNumber FROM (%n"
                + "SELECT%n"
                + "b.account_manager_user_id as accountManagerUserId,%n"
                + "(count(*)) as count%n"
                + "FROM%n"
                + "businesses as b%n"
                + "GROUP BY%n"
                + "b.account_manager_user_id%n"
                + ") base_sql%n"
                + ") detail_sql WHERE Paging_RowNumber BETWEEN 0 and 10");
        String countSql = String.format(
                "SELECT count(*) as RecordCount FROM ("+
                "%nSELECT"+
                "%nb.account_manager_user_id as accountManagerUserId,"+
                "%n(count(*)) as count"+
                "%nFROM"+
                "%nbusinesses as b"+
                "%nGROUP BY"+
                "%nb.account_manager_user_id"+
                "%n) count_sql");
        assertEquals(compound.countSql, countSql);

        assertEquals(compound.dataSql, fetchSql);

    }

    @Test
    public void testFrom() throws Exception {
        SqlQuery query = new SqlQuery()
                .from($.businesses)
                .from($.businessAddresses)
                .select($.businesses.id.as("bid"), $.businessAddresses.id.as("aid"))
                .where($.businesses.mainAddressId.eq($.businessAddresses.id));
        SqlSimpleStatement sql = query.buildSimple();
        assertEquals(sql.sql, String.format(
                "SELECT%n"+
                "businesses.id as bid,%n"+
                "business_addresses.id as aid%n"+
                "FROM%n"+
                "businesses as businesses,%n"+
                "business_addresses as business_addresses%n"+
                "WHERE%n"+
                "businesses.main_address_id = (business_addresses.id)"
        ));
    }

    @Test
    public void testJoin() throws Exception {
        SqlQuery query = new SqlQuery()
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName);
        SqlSimpleStatement sql = query.buildSimple();
        assertEquals(sql.sql, String.format(
                "SELECT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "businesses.business_name as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n"+
                        "ORDER BY%n"+
                        "businesses.business_name asc"
        ));
    }

    @Test
    public void testLeft() throws Exception {
        SqlQuery query = new SqlQuery()
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .left($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName);
        SqlSimpleStatement sql = query.buildSimple();
        assertEquals(sql.sql, String.format(
                "SELECT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "businesses.business_name as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "LEFT JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n"+
                        "ORDER BY%n"+
                        "businesses.business_name asc"
        ));
    }

    @Test
    public void testRight() throws Exception {
        SqlQuery query = new SqlQuery()
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .right($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName);
        SqlSimpleStatement sql = query.buildSimple();
        assertEquals(sql.sql, String.format(
                "SELECT%n" +
                        "businesses.id as id,%n" +
                        "business_addresses.city as city,%n" +
                        "businesses.business_name as businessName%n" +
                        "FROM%n" +
                        "businesses as businesses%n" +
                        "RIGHT JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n" +
                        "ORDER BY%n" +
                        "businesses.business_name asc"
        ));
    }

    @Test
    public void testSimpleStatementWhere() throws Exception {
        int val1 = 10;
        String val2 = "100";
        SqlQuery query = new SqlQuery()
                .distinct()
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName)
                .where($.businesses.id.gt(val1))
                .where($.businesses.id.lt(val2));
        SqlSimpleStatement sql = query.buildSimple();
        assertEquals(sql.sql, String.format(
                "SELECT DISTINCT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "businesses.business_name as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n"+
                        "WHERE%n"+
                        "businesses.id > (?)%n"+
                        "AND businesses.id < (?)%n" +
                        "ORDER BY%n"+
                        "businesses.business_name asc"
        ));
        assertEquals(2, sql.values.size());
        assertEquals(val1, sql.values.get(0));
        assertEquals(val2, sql.values.get(1));

    }

    @Test
    public void testCompountStatementWhere() throws Exception {
        int val1 = 10;
        String val2 = "100";
        SqlQuery query = new SqlQuery()
                .distinct()
                .pageNo(1)
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName)
                .where($.businesses.id.gt(val1))
                .where($.businesses.id.lt(val2));
        SqlCompoundStatement sql = query.buildCompound();
        assertEquals(sql.dataSql, String.format(
                "SELECT * FROM (%n" +
                        "SELECT *, row_number() OVER (ORDER BY %n" +
                        "businessName asc) as Paging_RowNumber FROM (%n" +
                "SELECT DISTINCT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "businesses.business_name as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n"+
                        "WHERE%n"+
                        "businesses.id > (?)%n"+
                        "AND businesses.id < (?)%n" +
                        ") base_sql%n" +
                        ") detail_sql WHERE Paging_RowNumber BETWEEN 0 and 50"
        ));
        assertEquals(2, sql.values.size());
        assertEquals(val1, sql.values.get(0));
        assertEquals(val2, sql.values.get(1));

    }
    @Test
    public void testOrderBy() throws Exception {
        SqlOrder order = $.businesses.businessName.asc();
        SqlQuery query = new SqlQuery()
                .distinct()
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy(order);
        SqlSimpleStatement sql = query.buildSimple();
        assertEquals(sql.sql, String.format(
                "SELECT DISTINCT%n" +
                        "businesses.id as id,%n" +
                        "business_addresses.city as city,%n" +
                        "businesses.business_name as businessName%n" +
                        "FROM%n" +
                        "businesses as businesses%n" +
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n" +
                        "ORDER BY%n" +
                        "businesses.business_name asc"
        ));
    }

    @Test
    public void testDistinct() throws Exception {
        SqlQuery query = new SqlQuery()
                .distinct()
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName);
        SqlSimpleStatement sql = query.buildSimple();
        assertEquals(sql.sql, String.format(
                "SELECT DISTINCT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "businesses.business_name as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n"+
                        "ORDER BY%n"+
                        "businesses.business_name asc"
        ));
    }

    @Test
    public void testPageNo() throws Exception {
        SqlQuery query = new SqlQuery()
                .pageNo(2)
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName);
        SqlCompoundStatement sql = query.buildCompound();
        assertEquals(sql.dataSql, String.format(
                "SELECT * FROM (%n" +
                        "SELECT *, row_number() OVER (ORDER BY %n" +
                        "businessName asc) as Paging_RowNumber FROM (%n" +
                "SELECT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "businesses.business_name as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n"+
                ") base_sql%n" +
                ") detail_sql WHERE Paging_RowNumber BETWEEN 50 and 100"
        ));
    }

    @Test
    public void testPageSize() throws Exception {
        int page = 2;
        int pageSize = 2;
        SqlQuery query = new SqlQuery()
                .pageNo(page)
                .pageSize(pageSize)
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName);
        SqlCompoundStatement sql = query.buildCompound();
        assertEquals(sql.dataSql, String.format(
                "SELECT * FROM (%n" +
                        "SELECT *, row_number() OVER (ORDER BY %n" +
                        "businessName asc) as Paging_RowNumber FROM (%n" +
                        "SELECT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "businesses.business_name as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n"+
                        ") base_sql%n" +
                        ") detail_sql WHERE Paging_RowNumber BETWEEN %d and %d",
                (page - 1)*pageSize, ((page - 1)*pageSize)+pageSize
        ));
    }

    @Test
    public void testTop() throws Exception {
        SqlQuery query = new SqlQuery()
                .top(10)
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName);
        SqlSimpleStatement sql = query.buildSimple();
        assertEquals(sql.sql, String.format(
                "SELECT TOP 10%n" +
                        "businesses.id as id,%n" +
                        "business_addresses.city as city,%n" +
                        "businesses.business_name as businessName%n" +
                        "FROM%n" +
                        "businesses as businesses%n" +
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n" +
                        "ORDER BY%n" +
                        "businesses.business_name asc"
        ));
    }

    @Test
    public void testGetByAlias() throws Exception {
        SqlColumn bid = $.businesses.id.as("bid");
        SqlQuery query = new SqlQuery()
                .select(bid);
        assertEquals(bid, query.getByAlias("bid"));
        assertEquals(null, query.getByAlias("id"));
    }

    @Test
    public void testHasColumnAlias() throws Exception {
        SqlQuery query = new SqlQuery()
                .select($.businesses.id.as("bid"));
        assert(query.hasColumnAlias("bid"));
        assert(!query.hasColumnAlias("id"));
    }

    @Test
    public void testBuildSimpleWithMasks() throws Exception {
        Map<SqlColumn, Integer> masks = new HashMap<>();
        masks.put($.businesses.businessName, -4);
        SqlQuery query = new SqlQuery()
                .distinct()
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName);
        SqlSimpleStatement sql = query.buildSimple(masks);
        assertEquals(sql.sql, String.format(
                "SELECT DISTINCT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "('*'+RIGHT(businesses.business_name, 4)) as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n"+
                        "ORDER BY%n"+
                        "businesses.business_name asc"
        ));

    }

    @Test
    public void testBuildCompoundNoPaging() throws Exception {
        SqlQuery query = new SqlQuery()
                .distinct()
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName);
        SqlCompoundStatement sql = query.buildCompound();
        assertEquals(sql.dataSql, String.format(
                "SELECT DISTINCT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "businesses.business_name as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n"+
                        "ORDER BY%n"+
                        "businesses.business_name asc"
        ));
    }

    @Test
    public void testBuildCompoundNoPagingNoOrder() throws Exception {
        SqlQuery query = new SqlQuery()
                .distinct()
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId));

        SqlCompoundStatement sql = query.buildCompound();
        assertEquals(sql.dataSql, String.format(
                "SELECT DISTINCT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "businesses.business_name as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id"
        ));
    }

    @Test(expected = FluentException.class)
    public void testBuildCompoundNoOrder() throws Exception {
        SqlQuery query = new SqlQuery()
                .distinct()
                .pageNo(1)
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId));

        SqlCompoundStatement sql = query.buildCompound();
    }

    @Test
    public void testBuildCompoundWithMasks() throws Exception {
        Map<SqlColumn, Integer> masks = new HashMap<>();
        masks.put($.businesses.businessName, 4);
        SqlQuery query = new SqlQuery()
                .distinct()
                .pageNo(1)
                .from($.businesses)
                .select($.businesses.id, $.businessAddresses.city)
                .select($.businesses.businessName)
                .join($.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId))
                .orderBy($.businesses.businessName);
        SqlCompoundStatement sql = query.buildCompound(masks);
        assertEquals(sql.dataSql, String.format(
                "SELECT * FROM (%n" +
                        "SELECT *, row_number() OVER (ORDER BY %n" +
                        "businessName asc) as Paging_RowNumber FROM (%n" +
                "SELECT DISTINCT%n"+
                        "businesses.id as id,%n"+
                        "business_addresses.city as city,%n"+
                        "(LEFT(businesses.business_name, 4)+'*') as businessName%n"+
                        "FROM%n"+
                        "businesses as businesses%n"+
                        "JOIN business_addresses as business_addresses on business_addresses.id = businesses.main_address_id%n"+
                        ") base_sql%n" +
                        ") detail_sql WHERE Paging_RowNumber BETWEEN 0 and 50"
        ));
    }

    @Test
    public void testUpdateAlias() throws Exception {
        SqlQuery query = new SqlQuery()
                .select($.businesses.id);
        query.updateAlias($.businesses.id, $.businesses.id.as("bid"));
        assert(query.hasColumnAlias("bid"));
    }

}