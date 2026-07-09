package porker.pp_legendarydungeons.features.wtraders.json;

/**
 * Runtime data folders that the wandering trader JSON reload listener scans.
 *
 * These are relative to data/<namespace>/.
 *
 * Example:
 * data/pp_legendarydungeons/wtrader/trade_pools/saplings.json
 */
public final class WTraderJsonFolders {
    public static final String TRADE_POOLS = "wtrader/trade_pools";
    public static final String PROFILES = "wtrader/profiles";
    public static final String SELECTION_TABLES = "wtrader/selection_tables";
    public static final String MAP_OFFERS = "wtrader/map_offers";

    private WTraderJsonFolders() {
    }
}