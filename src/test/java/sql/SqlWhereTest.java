package sql;

import models.$;
import models.BusinessAddresses;
import models.Businesses;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SqlWhereTest {

    @Test(expected = FluentException.class)
    public void testOr() throws Exception {
        SqlWhere where = new SqlWhere($.businesses.id, "=", 1234);
        where = where.or($.businesses.id.eq(1234));
        assertEquals(where.type, SqlWhere.Type.or);
        assertEquals(where.toString(), String.format("businesses.id = (?)%nOR businesses.id = (?)"));
        where.and($.businesses.id.eq(1));
    }

    @Test(expected = FluentException.class)
    public void testAnd() throws Exception {
        SqlWhere where = new SqlWhere($.businesses.id, "=", 1234);
        where = where.and($.businesses.id.eq(1234));
        assertEquals(where.type, SqlWhere.Type.and);
        assertEquals(where.toString(), String.format("businesses.id = (?)%nAND businesses.id = (?)"));
        where.or($.businesses.id.eq(1));
    }

    @Test
    public void testToString() throws Exception {

    }

    @Test
    public void testBuildSimple() throws Exception {
        SqlWhere where = new SqlWhere($.businesses.id, "=", 1234);
        List<Object> values = new ArrayList<>();
        String sqlWhere = where.build(values);
        assertEquals(sqlWhere, String.format("businesses.id = (?)"));
        assertEquals(values.size(), 1);
        assertEquals(values.get(0), 1234);
    }
    @Test
    public void testBuildComplex() throws Exception {
        SqlWhere where1 = new SqlWhere();
        SqlWhere where2 = new SqlWhere();
        where1 = where1.and($.businesses.id.eq(1234))
                .and($.businesses.id.eq($.businessAddresses.businessId))
                .and($.businesses.id.in(new SqlLiteral("SELECT business_id FROM business_dbas WHERE where name like ?").using("%bubba1%")));

        where2 = where2.or($.businesses.businessName.like("bubba2"))
                .or($.businesses.legalName.like("bubba3"))
                .or(new SqlLiteral("(CASE when business_type_id = ? then 0 else 1)").using(10).eq(1))
                .or(new SqlWhere());
        SqlWhere where = new SqlWhere();
        where = where.and(where1).and(where2);
        List<Object> values = new ArrayList<>();
        String sqlWhere = where.build(values);
        assertEquals(sqlWhere, String.format("(businesses.id = (?)%n" +
                "AND businesses.id = (business_addresses.business_id)%n" +
                "AND businesses.id in (SELECT business_id FROM business_dbas WHERE where name like ?))%n" +
                "AND (businesses.business_name like (?)%n" +
                "OR businesses.legal_name like (?)%n" +
                "OR (CASE when business_type_id = ? then 0 else 1) = (?))"));
        assertEquals(values.size(), 6);
        assertEquals(values.get(0), 1234);
        assertEquals(values.get(1), "%bubba1%");
        assertEquals(values.get(2), "%bubba2%");
        assertEquals(values.get(3), "%bubba3%");
        assertEquals(values.get(4), 10);
        assertEquals(values.get(5), 1);
    }
}