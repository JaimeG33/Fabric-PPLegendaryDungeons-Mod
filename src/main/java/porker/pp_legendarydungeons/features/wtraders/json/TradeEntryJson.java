package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * One weighted possible sell item inside a trade pool.
 *
 * An entry can define either:
 * - item: a direct item registry id, such as minecraft:oak_sapling
 * - loot_table: a loot table id that generates the sell ItemStack, such as
 *   pp_legendarydungeons:wtrader/items/fortune_pickaxe
 *
 * Use loot_table entries for enchanted items, custom components, random enchants,
 * custom names/lore, or anything that is easier to express with vanilla loot table
 * JSON functions.
 */
public final class TradeEntryJson {
    public String item;

    @SerializedName("loot_table")
    public String lootTable;

    public Integer weight;

    @SerializedName("count_min")
    public Integer countMin;

    @SerializedName("count_max")
    public Integer countMax;

    @SerializedName("max_uses_min")
    public Integer maxUsesMin;

    @SerializedName("max_uses_max")
    public Integer maxUsesMax;

    public Integer xp;

    public List<String> tags;

    public int weightOrDefault(int fallback) {
        return WTraderJsonValues.intOr(weight, fallback);
    }

    public int countMinOrDefault(int fallback) {
        return WTraderJsonValues.intOr(countMin, fallback);
    }

    public int countMaxOrDefault(int fallback) {
        return WTraderJsonValues.intOr(countMax, fallback);
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

    public List<String> tagsOrEmpty() {
        return WTraderJsonValues.listOrEmpty(tags);
    }

    public boolean hasItem() {
        return !WTraderJsonValues.isBlank(item);
    }

    public boolean hasLootTable() {
        return !WTraderJsonValues.isBlank(lootTable);
    }
}
