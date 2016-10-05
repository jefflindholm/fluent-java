package sql;

import models.$;
import models.Businesses;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SqlSettingsTest {

    SqlSettings foo = new SqlSettings();
    SqlSettings none = new SqlSettings();
    SqlSettings alias = new SqlSettings();

    public class Encrypt implements IEncrypted {
        public String decrypt(SqlColumn column) {
            return String.format("ENCRYPT(%s)", column.getColumnName());
        }
        public String encrypt(SqlColumn column) {
            return String.format("DECRYPT(%s)", column.getColumnName());
        }
        public String openKey() { return "OPEN_KEY"; }
    }

    @Before
    public void setup() {
        foo.beginEscape = "|";
        foo.endEscape = "|";
        foo.concat = "*";
        foo.escapeLevel = SqlSettings.EscapeLevel.all;
        foo.decrypter = new Encrypt();

        none.escapeLevel = SqlSettings.EscapeLevel.none;
        alias.escapeLevel = SqlSettings.EscapeLevel.alias;
    }

    @After
    public void tearDown() {
        SqlSettings.setToDefault();
    }

    @Test
    public void testSetActive() throws Exception {
        SqlSettings.setActive(foo);
        assertEquals(foo, SqlSettings.current());
    }

    @Test
    public void testTryWithResources() throws Exception {
        SqlSettings cur = SqlSettings.current();
        try(SqlSettings bar = new SqlSettings(true)) {
            assertEquals(bar, SqlSettings.current());
        }
        try(SqlSettings bar = new SqlSettings()) {
            assertEquals(cur, SqlSettings.current());
            bar.setActive();
            assertEquals(bar, SqlSettings.current());
        }
        assertEquals(cur, SqlSettings.current());
    }

    @Test
    public void testConcat() throws Exception {
        assert(!foo.concat.equals(SqlSettings.concat()));
        foo.setActive();
        assert(foo.concat.equals(SqlSettings.concat()));
    }

    @Test
    public void testEscapeTable() throws Exception {
        SqlSettings.setActive(foo);
        assertEquals(SqlSettings.escapeTable("table"), "|table|");
        SqlSettings.setActive(none);
        assertEquals(SqlSettings.escapeTable("table"), "table");
        SqlSettings.setActive(alias);
        assertEquals(SqlSettings.escapeTable("table"), "table");
    }

    @Test
    public void testEscapeColumn() throws Exception {
        SqlSettings.setActive(foo);
        assertEquals(SqlSettings.escapeColumn("column"), "|column|");
        SqlSettings.setActive(none);
        assertEquals(SqlSettings.escapeColumn("column"), "column");
        SqlSettings.setActive(alias);
        assertEquals(SqlSettings.escapeColumn("column"), "column");
    }

    @Test
    public void testEscapeAlias() throws Exception {
        SqlSettings.setActive(foo);
        assertEquals(SqlSettings.escapeAlias("alias"), "|alias|");
        SqlSettings.setActive(none);
        assertEquals(SqlSettings.escapeAlias("alias"), "alias");
        SqlSettings.setActive(alias);
        assertEquals(SqlSettings.escapeAlias("alias"), "[alias]");
    }

    @Test
    public void testDecrypt() throws Exception {
        assertEquals(SqlSettings.decrypt($.businesses.id, "default"), "default");
        SqlSettings.setActive(foo);
        Encrypt e = new Encrypt();
        assertEquals(SqlSettings.decrypt($.businesses.id, "default"), e.decrypt($.businesses.id));
    }

    @Test
    public void testEncrypt() throws Exception {
        assertEquals(SqlSettings.encrypt($.businesses.id, "default"), "default");
        SqlSettings.setActive(foo);
        Encrypt e = new Encrypt();
        assertEquals(SqlSettings.encrypt($.businesses.id, "default"), e.encrypt($.businesses.id));
    }
}