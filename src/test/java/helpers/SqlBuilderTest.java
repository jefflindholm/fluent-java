package helpers;

import models.$;
import models.BusinessAddresses;
import models.Businesses;
import models.States;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sql.IEncrypted;
import sql.SqlColumn;
import sql.SqlCompoundStatement;
import sql.SqlQuery;
import sql.SqlSettings;
import validators.Base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SqlBuilderTest {

    public class Encrypted implements IEncrypted {

        public String encrypt(SqlColumn column) {
            return column.getColumnName().equals("state_tax_id") ? "Encrypt(?)" : null;
        }

        public String decrypt(SqlColumn column) {
            String format ="Decrypt(%s)";
            return column.getColumnName().equals("state_tax_id") ? String.format(format, column.qualifiedName()) : null;
        }

        public String openKey() { return "KEY;"; }

    }

    public class BusinessValidator implements Base {
        public static final String nameError = "Invalid Name";
        public List<String> validate(JSONObject data, boolean insert) {
            List<String> errors = new ArrayList<>();
            try {
                if (data.getString($.businesses.businessName.getAlias()).equals("invalid")) {
                    errors.add(nameError);
                }
            } catch (Exception e){
                // do nothing
            }
            // todo: do some validation
            return errors;
        }
    }

    IEncrypted old;
    validators.Base oldBusiness;

    @Before
    public void setUp() throws Exception {
        old = SqlSettings.current().decrypter;
        SqlSettings.current().decrypter = new Encrypted();
        oldBusiness = SqlBuilder.addValidator($.businesses, new BusinessValidator());
    }

    @After
    public void tearDown() throws Exception {
        SqlSettings.current().decrypter = old;
        SqlBuilder.addValidator($.businesses, oldBusiness);
    }

    @Test
    public void testGetColumn() throws Exception {
        Map<String, SqlBuilder.SqlTableInfo> tables = new HashMap<>();
        tables.put("mainAddress", new SqlBuilder.SqlTableInfo($.businessAddresses, $.businessAddresses.on($.businessAddresses.id).using($.businesses.mainAddressId), null));
        tables.put("state", new SqlBuilder.SqlTableInfo($.states, $.states.on($.states.id).using($.businessAddresses.stateId), "mainAddress"));

        SqlBuilder.SqlColumnDetails bid = SqlBuilder.getColumn("id", $.businesses, tables);
        SqlBuilder.SqlColumnDetails city = SqlBuilder.getColumn("mainAddress.city", $.businesses, tables);
        SqlBuilder.SqlColumnDetails state = SqlBuilder.getColumn("state.abbreviation", $.businesses, tables);

        assertEquals(bid.column.getTable().getTableName(), Businesses.TableName);
        assertEquals(city.column.getTable().getTableName(), BusinessAddresses.TableName);
        assertEquals(state.column.getTable().getTableName(), States.TableName);
    }

    @Test
    public void testBuildOrderFromString() throws Exception {

    }

    @Test
    public void testJoinDetails() throws Exception {

    }

    @Test
    public void testBuildFromQueryStrings() throws Exception {
        // all the special case tests...
        // between, isNull, isNotNull - these take argument counts not equal to 1
        SqlQuery query = SqlBuilder.buildFromQueryStrings("id,businessName,businessNumber",
                                                        "businessName.like.bubba;id.between.1.2;id.isNull;id.isNotNull",
                                                        "businessName", 1, 2, $.businesses, null);
        SqlCompoundStatement sql = query.buildCompound();
        String expected = String.format("SELECT * FROM (%n"
                + "SELECT *, row_number() OVER (ORDER BY %n"
                + "businessName asc) as Paging_RowNumber FROM (%n"
                + "SELECT%n"
                + "businesses.id as id,%n"
                + "businesses.business_name as businessName,%n"
                + "businesses.business_number as businessNumber%n"
                + "FROM%n"
                + "businesses as businesses%n"
                + "WHERE%n"
                + "(businesses.business_name like (?))%n"
                + "AND ((businesses.id >= (?)%n"
                + "AND businesses.id <= (?)))%n"
                + "AND (businesses.id is null)%n"
                + "AND (businesses.id is not null)%n"
                + ") base_sql%n"
                + ") detail_sql WHERE Paging_RowNumber BETWEEN 0 and 2");
        assertEquals(expected, sql.dataSql);
        assertEquals(3, sql.values.size());
        assertEquals("%bubba%", sql.values.get(0));
        assertEquals("1", sql.values.get(1));
        assertEquals("2", sql.values.get(2));
    }

    @Test
    public void testBuildInsert() throws Exception {
        JSONObject data = new JSONObject(String.format("{\"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\"}",
                "stateTaxId", "12345",
                "businessName", "test name",
                "not a column", "ignored",
                "timestamp", "12345"));
        String[] ignore = { "timestamp" };
        SqlBuilder builder = new SqlBuilder();
        SqlBuilder.InsertUpdateDetails details = builder.buildInsert(data, $.businesses, $.businesses.id, "newId", Arrays.asList(ignore));
        String expected = "KEY;INSERT INTO businesses (id,business_name,state_tax_id) VALUES (?,?,Encrypt(?))";
        assertEquals(expected, details.sql);
        assertEquals(3, details.values.size());
        assertEquals("test name", details.values.get(1));
        assertEquals("12345", details.values.get(2));
    }

    @Test
    public void testBuildUpdate() throws Exception {
        JSONObject data = new JSONObject(String.format("{\"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\"}",
                "stateTaxId", "12345",
                "businessName", "test name",
                "not a column", "ignored",
                "timestamp", "12345"));
        String[] ignore = { "timestamp" };
        SqlBuilder builder = new SqlBuilder();
        SqlBuilder.InsertUpdateDetails details = builder.buildUpdate(data, $.businesses, $.businesses.id, "newId", Arrays.asList(ignore));
        String expected = "KEY;UPDATE businesses SET business_name = ?,state_tax_id = Encrypt(?) WHERE id = ?";
        assertNull(details.errors);
        assertEquals(expected, details.sql);
        assertEquals(3, details.values.size());
        assertEquals("test name", details.values.get(0));
        assertEquals("12345", details.values.get(1));
        assertEquals("newId", details.values.get(2));
    }

    @Test
    public void testValidation() throws Exception {
        JSONObject data = new JSONObject(String.format("{\"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\"}",
                "stateTaxId", "12345",
                "businessName", "invalid",
                "timestamp", "12345"));
        String[] ignore = { "timestamp" };
        SqlBuilder builder = new SqlBuilder();
        SqlBuilder.InsertUpdateDetails details = builder.buildUpdate(data, $.businesses, $.businesses.id, "newId", Arrays.asList(ignore));
        assertEquals(1, details.errors.size());
        assertEquals(BusinessValidator.nameError, details.errors.get(0));
    }

    @Test
    public void testReadOnly() throws Exception {
        String[] readonly = {"business_number"};
        JSONObject data = new JSONObject(String.format("{\"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\"}",
                "stateTaxId", "12345",
                "businessName", "test name",
                "timestamp", "12345",
                "businessNumber", "should be marked readonly so not in the results"));
        String[] ignore = { "timestamp" };
        SqlBuilder.InsertUpdateDetails details = SqlBuilder.buildUpdate(data, $.businesses, $.businesses.id, "newId", Arrays.asList(ignore), Arrays.asList(readonly));
        String expected = "KEY;UPDATE businesses SET business_name = ?,state_tax_id = Encrypt(?) WHERE id = ?";
        assertNull(details.errors);
        assertEquals(expected, details.sql);
        assertEquals(3, details.values.size());
        assertEquals("test name", details.values.get(0));
        assertEquals("12345", details.values.get(1));
        assertEquals("newId", details.values.get(2));
    }
}