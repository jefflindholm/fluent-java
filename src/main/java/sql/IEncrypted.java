package sql;

public interface IEncrypted {
    default String decrypt(SqlColumn column) {
        return null;
    }
    default String encrypt(SqlColumn column) {
        return null;
    }
    default String openKey() { return ""; }
}
