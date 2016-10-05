package sql;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class SqlTableTest {

    public static class TestTable extends SqlTable {
        public static final String TableName = "test_table";
        public TestTable() { super(TableName); setConverters(); }
        private TestTable(String alias) { super(TableName, alias); setConverters(); }
        public TestTable as(String alias) { return new TestTable(alias); }
        private void setConverters() {}
        public final SqlColumn typeId = new SqlColumn(this, "type_id").as("typeId");
        public final SqlColumn description = new SqlColumn(this, "description").as("description");
        public final SqlColumn id = new SqlColumn(this, "id").as("id");
        public final SqlColumn isActive = new SqlColumn(this, "is_active").as("isActive");
        public final SqlColumn name = new SqlColumn(this, "name").as("name");
        public final SqlColumn price = new SqlColumn(this, "price").as("price");
        public final SqlColumn timestamp = new SqlColumn(this, "timestamp").as("timestamp");
        public SqlColumn[] star() { return new SqlColumn[] { typeId, description, id, isActive, name, price, timestamp }; }
        public final static TestTable $ = new TestTable();
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOn() throws Exception {
        TestTable tt = new TestTable();
        SqlColumn col = TestTable.$.on(TestTable.$.id);
        assertEquals(col, TestTable.$.id);
        assertNull(TestTable.$.on(tt.id));
    }

    @Test
    public void testGetTableName() throws Exception {

    }

    @Test
    public void testGetAlias() throws Exception {
        SqlTable t1 = TestTable.$;
        SqlTable t2 = TestTable.$.as("bar");
        assertNotSame(t1, t2);
        assertEquals(t2.getAlias(), "bar");
        assertEquals(t1.getAlias(), "test_table");
    }

    @Test
    public void testGetFrom() throws Exception {
        assertEquals(TestTable.$.getFrom(), "test_table as test_table");
    }

    @Test
    public void testStar() throws Exception {
        Class<?> c = TestTable.$.getClass();
        SqlColumn col = new SqlColumn(TestTable.$, "unused");
        int count = 0;
        for(Field f : c.getFields()) {
            if ( f.getType().isAssignableFrom(col.getClass())) {
                count++;
            }
        }
        assertEquals(count, TestTable.$.star().length);
    }

    @Test
    public void testAs() throws Exception {
        assertEquals(TestTable.$.as("bar").getFrom(), "test_table as bar");
    }

    @Test
    public void testGetColumn() throws Exception {
        assertEquals(TestTable.$.getColumn("id"), TestTable.$.id);
        assertNull(TestTable.$.getColumn("unknown"));
    }
}