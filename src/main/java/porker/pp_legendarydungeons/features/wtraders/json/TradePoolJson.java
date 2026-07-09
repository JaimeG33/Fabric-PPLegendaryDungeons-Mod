package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Runtime JSON model for:
 * data/<namespace>/wtrader_trade_pools/*.json
 */
public final class TradePoolJson {
    public String id;
    public String category;

    @SerializedName("category_weight")
    public Integer categoryWeight;

    public List<String> tags;

    @SerializedName("base_price")
    public TradePriceJson basePrice;

    @SerializedName("secondary_price")
    public TradePriceJson secondaryPrice;

    public List<TradeEntryJson> entries;

    public int categoryWeightOrDefault(int fallback) {
        return WTraderJsonValues.intOr(categoryWeight, fallback);
    }

    public List<String> tagsOrEmpty() {
        return WTraderJsonValues.listOrEmpty(tags);
    }

    public List<TradeEntryJson> entriesOrEmpty() {
        return WTraderJsonValues.listOrEmpty(entries);
    }

    public boolean hasId() {
        return !WTraderJsonValues.isBlank(id);
    }

    public boolean hasCategory() {
        return !WTraderJsonValues.isBlank(category);
    }

    public boolean hasSecondaryPrice() {
        return secondaryPrice != null;
    }
}
