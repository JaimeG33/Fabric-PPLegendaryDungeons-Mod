package porker.pp_legendarydungeons.features.wtraders.json;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores the currently loaded wandering-trader JSON definitions.
 *
 * This registry is replaced as a whole whenever datapacks/resources reload.
 * For now, nothing in gameplay reads from this registry yet. The next later step
 * will be converting these loaded definitions into MerchantOffers.
 */
public final class WTraderJsonRegistry {
    private static Map<ResourceLocation, TradePoolJson> tradePools = Map.of();
    private static Map<ResourceLocation, TraderProfileJson> traderProfiles = Map.of();
    private static Map<ResourceLocation, SelectionTableJson> selectionTables = Map.of();
    private static Map<ResourceLocation, MapOfferJson> mapOffers = Map.of();

    private WTraderJsonRegistry() {
    }

    public static void replaceAll(
            Map<ResourceLocation, TradePoolJson> newTradePools,
            Map<ResourceLocation, TraderProfileJson> newTraderProfiles,
            Map<ResourceLocation, SelectionTableJson> newSelectionTables,
            Map<ResourceLocation, MapOfferJson> newMapOffers
    ) {
        tradePools = immutableCopy(newTradePools);
        traderProfiles = immutableCopy(newTraderProfiles);
        selectionTables = immutableCopy(newSelectionTables);
        mapOffers = immutableCopy(newMapOffers);
    }

    private static <T> Map<ResourceLocation, T> immutableCopy(Map<ResourceLocation, T> input) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(input));
    }

    public static Map<ResourceLocation, TradePoolJson> tradePools() {
        return tradePools;
    }

    public static Map<ResourceLocation, TraderProfileJson> traderProfiles() {
        return traderProfiles;
    }

    public static Map<ResourceLocation, SelectionTableJson> selectionTables() {
        return selectionTables;
    }

    public static Map<ResourceLocation, MapOfferJson> mapOffers() {
        return mapOffers;
    }

    public static Optional<TradePoolJson> getTradePool(ResourceLocation id) {
        return Optional.ofNullable(tradePools.get(id));
    }

    public static Optional<TraderProfileJson> getTraderProfile(ResourceLocation id) {
        return Optional.ofNullable(traderProfiles.get(id));
    }

    public static Optional<SelectionTableJson> getSelectionTable(ResourceLocation id) {
        return Optional.ofNullable(selectionTables.get(id));
    }

    public static Optional<MapOfferJson> getMapOffer(ResourceLocation id) {
        return Optional.ofNullable(mapOffers.get(id));
    }

    public static int tradePoolCount() {
        return tradePools.size();
    }

    public static int traderProfileCount() {
        return traderProfiles.size();
    }

    public static int selectionTableCount() {
        return selectionTables.size();
    }

    public static int mapOfferCount() {
        return mapOffers.size();
    }
}
