package helpers;

import com.google.common.base.CaseFormat;

public final class Strings {
    public static String toCamelCase(String source) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, source);
    }

    public static String toSnakeCase(String source) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, source);
    }
}
