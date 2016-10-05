package sql;

import models.$;
import models.BusinessAddresses;
import models.Businesses;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SqlColumnTest {

    static final String newLine = System.lineSeparator();

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testAs() throws Exception {
        Businesses table = new Businesses();
        SqlColumn col1 = table.id;
        SqlColumn col2 = col1.as("someId");
        assert(col1 != col2);
        Assert.assertEquals(col1.getAlias(), "id");
        Assert.assertEquals(col2.getAlias(), "someId");
        Assert.assertEquals(col1.getColumnName(), col2.getColumnName());
    }

    @Test
    public void testUsing() throws Exception {
        SqlJoin join = $.businesses.mainAddressId.using($.businessAddresses.id);
        Assert.assertEquals(join.from.getColumnName(), $.businesses.mainAddressId.getColumnName());
        Assert.assertEquals(join.to.getColumnName(), $.businessAddresses.id.getColumnName());
    }

    @Test
    public void testGetAlias() throws Exception {
        assert($.businesses.id != $.businesses.id.as("someId"));
        assertEquals($.businesses.id.getColumnName(), $.businesses.id.as("someId").getColumnName());
        assertEquals($.businesses.id.as("test").getAlias(), "test");
    }

    @Test
    public void testGetColumnName() throws Exception {
        Assert.assertEquals($.businesses.id.getColumnName(), $.businesses.id.as("someOtherId").getColumnName());
    }

    @Test
    public void testGetTable() throws Exception {
        Businesses b = $.businesses.as("b");
        Assert.assertNotSame(b.id.getTable(), $.businesses.id.getTable());
        Assert.assertSame(b.id.getTable(), b.businessEmail.getTable());
    }

    @Test
    public void testQualifiedName() throws Exception {
        Businesses b = $.businesses.as("b");
        Assert.assertEquals(b.id.qualifiedName(), "b.id");
    }

    @Test
    public void testLt() throws Exception {
        SqlWhere w = $.businesses.id.lt(10000);
        List<Object> values = new ArrayList<>();
        String where = w.build(values);
        Assert.assertEquals(where, $.businesses.id.qualifiedName() + " < (?)" );
        Assert.assertEquals(values.get(0), 10000);
    }

    @Test
    public void testLte() throws Exception {
        SqlWhere w = $.businesses.id.lte(10000);
        List<Object> values = new ArrayList<>();
        String where = w.build(values);
        Assert.assertEquals(where, $.businesses.id.qualifiedName() + " <= (?)");
        Assert.assertEquals(values.get(0), 10000);
    }

    @Test
    public void testGt() throws Exception {
        SqlWhere w = $.businesses.id.gt(10000);
        List<Object> values = new ArrayList<>();
        String where = w.build(values);
        Assert.assertEquals(where, $.businesses.id.qualifiedName() + " > (?)" );
        Assert.assertEquals(values.get(0), 10000);
    }

    @Test
    public void testGte() throws Exception {
        SqlWhere w = $.businesses.id.gte(10000);
        List<Object> values = new ArrayList<>();
        String where = w.build(values);
        Assert.assertEquals(where, $.businesses.id.qualifiedName() + " >= (?)" );
        Assert.assertEquals(values.get(0), 10000);
    }

    @Test
    public void testEq() throws Exception {
        SqlWhere w = $.businesses.id.eq(10000);
        List<Object> values = new ArrayList<>();
        String where = w.build(values);
        Assert.assertEquals(where, $.businesses.id.qualifiedName() + " = (?)" );
        Assert.assertEquals(values.get(0), 10000);
    }

    @Test
    public void testNeq() throws Exception {
        SqlWhere w = $.businesses.id.neq(10000);
        List<Object> values = new ArrayList<>();
        String where = w.build(values);
        Assert.assertEquals(where, $.businesses.id.qualifiedName() + " != (?)" );
        Assert.assertEquals(values.get(0), 10000);
    }

    @Test
    public void testLike() throws Exception {
        SqlWhere w = $.businesses.id.like("10000");
        List<Object> values = new ArrayList<>();
        String where = w.build(values);
        Assert.assertEquals(where, $.businesses.id.qualifiedName() + " like (?)" );
        Assert.assertEquals(values.get(0), "%10000%");
    }

    @Test
    public void testBetween() throws Exception {
        SqlWhere w = $.businesses.id.between(10000, 10001);
        List<Object> values = new ArrayList<>();
        String where = w.build(values);
        Assert.assertEquals(where, $.businesses.id.qualifiedName() + " >= (?)"+newLine+"AND " + $.businesses.id.qualifiedName() + " <= (?)" );
        Assert.assertEquals(values.get(0), 10000);
        Assert.assertEquals(values.get(1), 10001);
    }

    @Test
    public void testIn() throws Exception {
        SqlWhere w = $.businesses.id.in(new int[]{10000, 10001});
        List<Object> values = new ArrayList<>();
        String where = w.build(values);
        Assert.assertEquals(where, $.businesses.id.qualifiedName() + " in (?)" );
        assertArrayEquals((int[])values.get(0), new int[] {10000,10001});
    }

    @Test
    public void testIsNull() throws Exception {
        SqlWhere w = $.businesses.id.isNull();
        List<Object> values = new ArrayList<>();
        String where = w.build(values);
        Assert.assertEquals(where, $.businesses.id.qualifiedName() + " is null" );
        assert(values.isEmpty());
    }

    @Test
    public void testAsc() throws Exception {
        SqlOrder o = $.businesses.id.asc();
        assertEquals(SqlOrder.Direction.asc, o.direction);
    }

    @Test
    public void testDesc() throws Exception {
        SqlOrder o = $.businesses.id.desc();
        assertEquals(SqlOrder.Direction.desc, o.direction);
    }

    @Test
    public void testOrder() throws Exception {
        SqlOrder o = $.businesses.id.order("asc");
        assertEquals(SqlOrder.Direction.asc, o.direction);
        o = $.businesses.id.order("desc");
        assertEquals(SqlOrder.Direction.desc, o.direction);
    }

    @Test
    public void testGetReplacement() throws Exception {
        SqlColumn id = $.businesses.id.as("foo");
        id.setConverter("uuid");
        Assert.assertEquals(id.getReplacement("?"), "uuid(?)");
    }
}