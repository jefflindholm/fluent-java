package sql;

import java.util.Stack;

public final class SqlSettings implements IEncrypted, AutoCloseable {
    private static Stack<SqlSettings> _defaults;
    static {
        _defaults = new Stack<>();
        _defaults.add(new SqlSettings());
    }
    public static SqlSettings current() { return _defaults.peek(); }

    public static SqlSettings setActive(SqlSettings settings) {
        _defaults.push(settings);
        return current();
    }
    public static SqlSettings setToDefault() {
        while(_defaults.size() > 1) {
            _defaults.pop();
        }
        return current();
    }

    public SqlSettings() {
        this(false);
    }
    public SqlSettings(boolean setActive) {
        if ( setActive ) {
            this.setActive();
        }
    }
    public SqlSettings setActive() {
        SqlSettings.setActive(this);
        return this;
    }
    public void close() {
        if ( _defaults.size() > 1) {
            _defaults.pop();
        }
    }

    public String beginEscape = "[";
    public String endEscape = "]";
    public String concat = "+";
    public int defaultPageSize = 50;

    public enum EscapeLevel {
        none,
        all,
        alias
    }

    public EscapeLevel escapeLevel = EscapeLevel.none;

    public static String concat() {
        return current().concat;
    }
    public static String escapeTable(String name) {
        return current().escapeLevel != EscapeLevel.all ? name : String.format("%s%s%s", current().beginEscape, name, current().endEscape);
    }
    public static String escapeColumn(String name) {
        return current().escapeLevel != EscapeLevel.all ? name : String.format("%s%s%s", current().beginEscape, name, current().endEscape);
    }
    public static String escapeAlias(String name) {
        return current().escapeLevel != EscapeLevel.all && current().escapeLevel != EscapeLevel.alias ? name : String.format("%s%s%s", current().beginEscape, name, current().endEscape);
    }
    public static String decrypt(SqlColumn column, String defaultValue) {
        if (current().decrypter == null) {
            return defaultValue;
        }
        String result = current().decrypter.decrypt(column);
        return result == null ? defaultValue : result;
    }
    public static String encrypt(SqlColumn column, String defaultValue) {
        if (current().decrypter == null) {
            return defaultValue;
        }
        String result = current().decrypter.encrypt(column);
        return result == null ? defaultValue : result;
    }

    public IEncrypted decrypter = null;

}


