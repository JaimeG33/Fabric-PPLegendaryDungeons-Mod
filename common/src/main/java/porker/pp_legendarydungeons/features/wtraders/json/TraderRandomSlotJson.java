package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * A random trade slot in a trader profile.
 *
 * Example use:
 * Pick 1 random trade from any pool in categories saplings or utility_items,
 * excluding entries tagged legendary.
 */
public final class TraderRandomSlotJson {
    public Integer count;

    @SerializedName("category_filter_mode")
    public String categoryFilterMode;

    public List<String> categories;

    @SerializedName("tag_filter_mode")
    public String tagFilterMode;

    public List<String> tags;

    public int countOrDefault(int fallback) {
        return WTraderJsonValues.intOr(count, fallback);
    }

    public List<String> categoriesOrEmpty() {
        return WTraderJsonValues.listOrEmpty(categories);
    }

    public List<String> tagsOrEmpty() {
        return WTraderJsonValues.listOrEmpty(tags);
    }
}
