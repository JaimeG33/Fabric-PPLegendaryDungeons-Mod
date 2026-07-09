package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads custom wandering-trader JSON definitions from server data resources.
 *
 * This class does not register itself. WTraderJsonReloadRegistrar handles Fabric
 * registration so this class can stay focused on parsing and storing data.
 */
public final class WTraderJsonReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private WTraderJsonReloadListener() {
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

        ProfessorPorkersLegendaryDungeons.LOGGER.info(
                "[WTrader JSON] Loaded {} trade pools, {} trader profiles, {} selection tables, and {} map offers.",
                WTraderJsonRegistry.tradePoolCount(),
                WTraderJsonRegistry.traderProfileCount(),
                WTraderJsonRegistry.selectionTableCount(),
                WTraderJsonRegistry.mapOfferCount()
        );
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
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] {} has invalid JSON syntax: {}",
                    fileId,
                    exception.getMessage()
            );
            return null;
        } catch (Exception exception) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
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
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] Duplicate {} id {}. Latest file {} replaced an earlier definition.",
                    typeName,
                    dataId,
                    fileId
            );
        }
    }
}
