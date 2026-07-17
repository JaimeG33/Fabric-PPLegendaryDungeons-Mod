package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads custom wandering-trader JSON definitions from server-data resources.
 *
 * <p>The Architectury registrar owns loader integration. This class uses
 * Minecraft's loader-neutral reload-listener base and remains focused on
 * parsing, validation, and atomic registry replacement.</p>
 */
public final class WTraderJsonReloadListener
        extends SimplePreparableReloadListener<Unit> {
    public static final WTraderJsonReloadListener INSTANCE =
            new WTraderJsonReloadListener();

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private WTraderJsonReloadListener() {
    }

    /**
     * No asynchronous preparation is required because the existing parser
     * performs one atomic load/apply pass on the reload apply executor.
     */
    @Override
    protected Unit prepare(
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        return Unit.INSTANCE;
    }

    @Override
    protected void apply(
            Unit prepared,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        reload(resourceManager);
    }

    public static void reload(ResourceManager resourceManager) {
        Map<ResourceLocation, TradePoolJson> tradePools = loadTradePools(resourceManager);
        Map<ResourceLocation, TraderProfileJson> traderProfiles = loadTraderProfiles(resourceManager);
        Map<ResourceLocation, SelectionTableJson> selectionTables = loadSelectionTables(resourceManager);
        Map<ResourceLocation, MapOfferJson> mapOffers = loadMapOffers(resourceManager);

        WTraderJsonRegistry.replaceAll(
                tradePools,
                traderProfiles,
                selectionTables,
                mapOffers
        );

        LegendaryDungeons.LOGGER.info(
                "[WTrader JSON] Loaded {} trade pools, {} trader profiles, {} selection tables, and {} map offers.",
                WTraderJsonRegistry.tradePoolCount(),
                WTraderJsonRegistry.traderProfileCount(),
                WTraderJsonRegistry.selectionTableCount(),
                WTraderJsonRegistry.mapOfferCount()
        );

        WTraderJsonGeneratorSmokeTest.logSmokeTest();
    }

    private static Map<ResourceLocation, TradePoolJson> loadTradePools(ResourceManager resourceManager) {
        Map<ResourceLocation, TradePoolJson> loaded = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry : listJsonResources(resourceManager, WTraderJsonFolders.TRADE_POOLS).entrySet()) {
            ResourceLocation fileId = entry.getKey();
            TradePoolJson json = readJson(fileId, entry.getValue(), TradePoolJson.class);

            if (WTraderJsonValidator.validateTradePool(fileId, json)) {
                ResourceLocation dataId = ResourceLocation.parse(json.id);
                putDefinition(loaded, dataId, json, fileId, "trade pool");
            }
        }

        return loaded;
    }

    private static Map<ResourceLocation, TraderProfileJson> loadTraderProfiles(ResourceManager resourceManager) {
        Map<ResourceLocation, TraderProfileJson> loaded = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry : listJsonResources(resourceManager, WTraderJsonFolders.PROFILES).entrySet()) {
            ResourceLocation fileId = entry.getKey();
            TraderProfileJson json = readJson(fileId, entry.getValue(), TraderProfileJson.class);

            if (WTraderJsonValidator.validateTraderProfile(fileId, json)) {
                ResourceLocation dataId = ResourceLocation.parse(json.id);
                putDefinition(loaded, dataId, json, fileId, "trader profile");
            }
        }

        return loaded;
    }

    private static Map<ResourceLocation, SelectionTableJson> loadSelectionTables(ResourceManager resourceManager) {
        Map<ResourceLocation, SelectionTableJson> loaded = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry : listJsonResources(resourceManager, WTraderJsonFolders.SELECTION_TABLES).entrySet()) {
            ResourceLocation fileId = entry.getKey();
            SelectionTableJson json = readJson(fileId, entry.getValue(), SelectionTableJson.class);

            if (WTraderJsonValidator.validateSelectionTable(fileId, json)) {
                ResourceLocation dataId = ResourceLocation.parse(json.id);
                putDefinition(loaded, dataId, json, fileId, "selection table");
            }
        }

        return loaded;
    }

    private static Map<ResourceLocation, MapOfferJson> loadMapOffers(ResourceManager resourceManager) {
        Map<ResourceLocation, MapOfferJson> loaded = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry : listJsonResources(resourceManager, WTraderJsonFolders.MAP_OFFERS).entrySet()) {
            ResourceLocation fileId = entry.getKey();
            MapOfferJson json = readJson(fileId, entry.getValue(), MapOfferJson.class);

            if (WTraderJsonValidator.validateMapOffer(fileId, json)) {
                ResourceLocation dataId = ResourceLocation.parse(json.id);
                putDefinition(loaded, dataId, json, fileId, "map offer");
            }
        }

        return loaded;
    }

    private static Map<ResourceLocation, Resource> listJsonResources(ResourceManager resourceManager, String folder) {
        return resourceManager.listResources(
                folder,
                resourceLocation -> resourceLocation.getPath().endsWith(".json")
        );
    }

    private static <T> T readJson(ResourceLocation fileId, Resource resource, Class<T> type) {
        try (Reader reader = resource.openAsReader()) {
            return GSON.fromJson(reader, type);
        } catch (JsonSyntaxException exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] {} has invalid JSON syntax: {}",
                    fileId,
                    exception.getMessage()
            );
            return null;
        } catch (Exception exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] Failed to read {}: {}",
                    fileId,
                    exception.getMessage()
            );
            return null;
        }
    }

    private static <T> void putDefinition(
            Map<ResourceLocation, T> loaded,
            ResourceLocation dataId,
            T json,
            ResourceLocation fileId,
            String typeName
    ) {
        T previous = loaded.put(dataId, json);

        if (previous != null) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] Duplicate {} id {}. Latest file {} replaced an earlier definition.",
                    typeName,
                    dataId,
                    fileId
            );
        }
    }
}
