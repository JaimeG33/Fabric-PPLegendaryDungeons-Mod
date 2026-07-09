package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Runtime JSON model for:
 * data/<namespace>/wtrader_map_offers/*.json
 *
 * A map offer links a profile trade to an existing loot table that creates the
 * actual functional filled map.
 */
public final class MapOfferJson {
    public String id;

    @SerializedName("loot_table")
    public String lootTable;

    public List<String> tags;

    public List<String> tagsOrEmpty() {
        return WTraderJsonValues.listOrEmpty(tags);
    }

    public boolean hasId() {
        return !WTraderJsonValues.isBlank(id);
    }

    public boolean hasLootTable() {
        return !WTraderJsonValues.isBlank(lootTable);
    }
}
