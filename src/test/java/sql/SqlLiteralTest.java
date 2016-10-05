package sql;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;

import static org.junit.Assert.*;


public class SqlLiteralTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testQualifiedName() throws Exception {
        String sql = "SELECT TOP 1 id FROM businesses WHERE id = ?";
        SqlLiteral literal = new SqlLiteral(sql);
        assertEquals(literal.qualifiedName(), sql);
    }

    @Test
    public void testConstructedAlias() throws Exception {
        String sql = "SELECT TOP 1 id FROM businesses WHERE id = ?";
        SqlLiteral literal = new SqlLiteral(sql, "businessId");
        assertEquals(literal.qualifiedName(), sql);
        assertEquals(literal.alias, "businessId");
    }

    @Test
    public void testUsing() throws Exception {
        String sql = "SELECT TOP 1 id FROM businesses WHERE id = ?";
        SqlLiteral literal = new SqlLiteral(sql);
        SqlLiteral literal2 = literal.using(1234);
        assertNotSame(literal, literal2);
        assertArrayEquals(literal2.replacementValues.toArray(), new Integer[]{1234});
    }

    @Test
    public void testUsing1() throws Exception {
        String sql = "SELECT TOP 1 id FROM businesses WHERE id = ?";
        SqlLiteral literal = new SqlLiteral(sql);
        SqlLiteral literal2 = literal.using(new Integer[]{1234, 5678});
        assertArrayEquals(literal2.replacementValues.toArray(), new Integer[]{1234, 5678});
    }
}