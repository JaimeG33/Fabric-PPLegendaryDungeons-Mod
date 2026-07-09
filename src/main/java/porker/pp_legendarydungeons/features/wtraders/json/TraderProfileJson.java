package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Runtime JSON model for:
 * data/<namespace>/wtrader_profiles/*.json
 */
public final class TraderProfileJson {
    public String id;

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("show_display_name")
    public Boolean showDisplayName;

    @SerializedName("trader_type")
    public String traderType;

    @SerializedName("map_type")
    public String mapType;

    @SerializedName("total_trades")
    public Integer totalTrades;

    @SerializedName("guaranteed_trades")
    public List<TraderTradeJson> guaranteedTrades;

    @SerializedName("random_slots")
    public List<TraderRandomSlotJson> randomSlots;

    public boolean showDisplayNameOrDefault(boolean fallback) {
        return WTraderJsonValues.booleanOr(showDisplayName, fallback);
    }

    public int totalTradesOrDefault(int fallback) {
        return WTraderJsonValues.intOr(totalTrades, fallback);
    }

    public List<TraderTradeJson> guaranteedTradesOrEmpty() {
        return WTraderJsonValues.listOrEmpty(guaranteedTrades);
    }

    public List<TraderRandomSlotJson> randomSlotsOrEmpty() {
        return WTraderJsonValues.listOrEmpty(randomSlots);
    }

    public boolean hasId() {
        return !WTraderJsonValues.isBlank(id);
    }

    public boolean hasDisplayName() {
        return !WTraderJsonValues.isBlank(displayName);
    }
}
