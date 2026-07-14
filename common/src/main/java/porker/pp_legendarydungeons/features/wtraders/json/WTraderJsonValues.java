package porker.pp_legendarydungeons.features.wtraders.json;

import java.util.Collections;
import java.util.List;

/**
 * Small helpers for nullable JSON fields.
 *
 * These classes intentionally use nullable wrapper types such as Integer, Float,
 * and Boolean so the loader can distinguish "field missing" from a value that
 * was explicitly set to 0/false.
 */
public final class WTraderJsonValues {
    private WTraderJsonValues() {
    }

    public static <T> List<T> listOrEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    public static int intOr(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    public static float floatOr(Float value, float fallback) {
        return value == null ? fallback : value;
    }

    public static boolean booleanOr(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }

    public static String stringOr(String value, String fallback) {
        return value == null ? fallback : value;
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
