package models;
import sql.SqlTable;
import java.util.HashMap;
import java.util.Map;
public class $ {
    private static Map<String, SqlTable> _allTables;
    public static SqlTable get(String tableName) {
        return _allTables.get(tableName);
    }
    public static BusinessAddresses businessAddresses = new BusinessAddresses();
    public static Businesses businesses = new Businesses();
    public static States states = new States();
    static {
        _allTables = new HashMap<>();
        _allTables.put(BusinessAddresses.TableName, businessAddresses);
        _allTables.put(Businesses.TableName, businesses);
        _allTables.put(States.TableName, states);
    }
}
