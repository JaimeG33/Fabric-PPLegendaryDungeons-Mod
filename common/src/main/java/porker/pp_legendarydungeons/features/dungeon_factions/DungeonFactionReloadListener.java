package porker.pp_legendarydungeons.features.dungeon_factions;

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
 * Loads dungeon faction relationship resources from server datapacks.
 */
public final class DungeonFactionReloadListener
        extends SimplePreparableReloadListener<Unit> {
    public static final DungeonFactionReloadListener INSTANCE =
            new DungeonFactionReloadListener();

    public static final String FACTION_FOLDER = "dungeon_factions";

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private DungeonFactionReloadListener() {
    }

    @Override
    protected Unit prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return Unit.INSTANCE;
    }

    @Override
    protected void apply(
            Unit prepared,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        Map<ResourceLocation, DungeonFactionProfileJson> loaded = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry :
                resourceManager.listResources(
                        FACTION_FOLDER,
                        id -> id.getPath().endsWith(".json")
                ).entrySet()) {
            ResourceLocation fileId = entry.getKey();
            ResourceLocation factionId = factionId(fileId);

            if (factionId == null) {
                continue;
            }

            DungeonFactionProfileJson profile = readJson(fileId, entry.getValue());

            if (!DungeonFactionProfileValidator.validate(fileId, factionId, profile)) {
                continue;
            }

            DungeonFactionProfileJson previous = loaded.put(factionId, profile);

            if (previous != null) {
                LegendaryDungeons.LOGGER.warn(
                        "[Dungeon Factions] Duplicate faction ID {}. File {} replaced an earlier definition.",
                        factionId,
                        fileId
                );
            }
        }

        DungeonFactionProfileRegistry.replaceAll(loaded);
        LegendaryDungeons.LOGGER.info(
                "[Dungeon Factions] Loaded {} faction definitions.",
                DungeonFactionProfileRegistry.size()
        );
    }

    private static ResourceLocation factionId(ResourceLocation fileId) {
        String prefix = FACTION_FOLDER + "/";
        String path = fileId.getPath();

        if (!path.startsWith(prefix) || !path.endsWith(".json")) {
            LegendaryDungeons.LOGGER.warn(
                    "[Dungeon Factions] Could not derive a faction ID from {}.",
                    fileId
            );
            return null;
        }

        String relative = path.substring(prefix.length(), path.length() - ".json".length());

        if (relative.isBlank()) {
            LegendaryDungeons.LOGGER.warn(
                    "[Dungeon Factions] Faction file {} has an empty relative path.",
                    fileId
            );
            return null;
        }

        return ResourceLocation.fromNamespaceAndPath(fileId.getNamespace(), relative);
    }

    private static DungeonFactionProfileJson readJson(
            ResourceLocation fileId,
            Resource resource
    ) {
        try (Reader reader = resource.openAsReader()) {
            return GSON.fromJson(reader, DungeonFactionProfileJson.class);
        } catch (JsonSyntaxException exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[Dungeon Factions] {} has invalid JSON syntax: {}",
                    fileId,
                    exception.getMessage()
            );
        } catch (Exception exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[Dungeon Factions] Failed to read {}: {}",
                    fileId,
                    exception.getMessage()
            );
        }

        return null;
    }
}
