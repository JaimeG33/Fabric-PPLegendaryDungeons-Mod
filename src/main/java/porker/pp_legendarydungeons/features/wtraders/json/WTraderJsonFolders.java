package porker.pp_legendarydungeons.features.wtraders.json;

/**
 * Runtime data folders that the future reload listener should scan.
 *
 * These are relative to data/<namespace>/.
 *
 * Example:
 * data/pp_legendarydungeons/wtrader_trade_pools/saplings.json
 */
public final class WTraderJsonFolders {
    public static final String TRADE_POOLS = "wtrader_trade_pools";
    public static final String PROFILES = "wtrader_profiles";
    public static final String SELECTION_TABLES = "wtrader_selection_tables";
    public static final String MAP_OFFERS = "wtrader_map_offers";

    private WTraderJsonFolders() {
    }
}
