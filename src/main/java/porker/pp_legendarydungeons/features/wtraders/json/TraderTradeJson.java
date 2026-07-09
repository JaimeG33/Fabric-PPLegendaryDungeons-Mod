package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * A guaranteed trade definition inside a trader profile.
 *
 * Supported planned types:
 * - direct_item
 * - generated_map
 * - pool_roll
 *
 * This class intentionally contains all possible fields. The future validator
 * should check which fields are required for each type.
 */
public final class TraderTradeJson {
    public String type;

    @SerializedName("map_offer")
    public String mapOffer;

    @SerializedName("sell_item")
    public String sellItem;

    @SerializedName("sell_count_min")
    public Integer sellCountMin;

    @SerializedName("sell_count_max")
    public Integer sellCountMax;

    @SerializedName("base_price")
    public TradePriceJson basePrice;

    @SerializedName("secondary_price")
    public TradePriceJson secondaryPrice;

    @SerializedName("max_uses_min")
    public Integer maxUsesMin;

    @SerializedName("max_uses_max")
    public Integer maxUsesMax;

    public Integer xp;

    @SerializedName("category_filter_mode")
    public String categoryFilterMode;

    public List<String> categories;

    @SerializedName("tag_filter_mode")
    public String tagFilterMode;

    public List<String> tags;

    public Integer count;

    public int sellCountMinOrDefault(int fallback) {
        return WTraderJsonValues.intOr(sellCountMin, fallback);
    }

    public int sellCountMaxOrDefault(int fallback) {
        return WTraderJsonValues.intOr(sellCountMax, fallback);
    }

    public int maxUsesMinOrDefault(int fallback) {
        return WTraderJsonValues.intOr(maxUsesMin, fallback);
    }

    public int maxUsesMaxOrDefault(int fallback) {
        return WTraderJsonValues.intOr(maxUsesMax, fallback);
    }

    public int xpOrDefault(int fallback) {
        return WTraderJsonValues.intOr(xp, fallback);
    }

    public int countOrDefault(int fallback) {
        return WTraderJsonValues.intOr(count, fallback);
    }

    public List<String> categoriesOrEmpty() {
        return WTraderJsonValues.listOrEmpty(categories);
    }

    public List<String> tagsOrEmpty() {
        return WTraderJsonValues.listOrEmpty(tags);
    }

    public boolean hasType() {
        return !WTraderJsonValues.isBlank(type);
    }

    public boolean hasSecondaryPrice() {
        return secondaryPrice != null;
    }
}
