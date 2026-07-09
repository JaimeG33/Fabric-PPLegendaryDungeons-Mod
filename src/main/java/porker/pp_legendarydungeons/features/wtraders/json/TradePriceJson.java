package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.annotations.SerializedName;

/**
 * JSON definition for one side of a trade price.
 *
 * Example:
 * {
 *   "item": "minecraft:emerald",
 *   "count": 5,
 *   "variance_min": -1,
 *   "variance_max": 2
 * }
 */
public final class TradePriceJson {
    public String item;

    public Integer count;

    @SerializedName("variance_min")
    public Integer varianceMin;

    @SerializedName("variance_max")
    public Integer varianceMax;

    public int countOrDefault(int fallback) {
        return WTraderJsonValues.intOr(count, fallback);
    }

    public int varianceMinOrDefault(int fallback) {
        return WTraderJsonValues.intOr(varianceMin, fallback);
    }

    public int varianceMaxOrDefault(int fallback) {
        return WTraderJsonValues.intOr(varianceMax, fallback);
    }

    public boolean hasItem() {
        return !WTraderJsonValues.isBlank(item);
    }
}
