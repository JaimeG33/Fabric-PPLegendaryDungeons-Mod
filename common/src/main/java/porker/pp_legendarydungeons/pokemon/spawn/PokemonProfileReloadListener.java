package porker.pp_legendarydungeons.pokemon.spawn;

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
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonProfileJson;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonProfileRegistry;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonProfileValidator;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads both reusable Pokémon spawn profiles and dungeon behavior profiles.
 */
public final class PokemonProfileReloadListener
        extends SimplePreparableReloadListener<Unit> {
    public static final PokemonProfileReloadListener INSTANCE =
            new PokemonProfileReloadListener();

    public static final String SPAWN_PROFILE_FOLDER = "pokemon_spawn_profiles";
    public static final String DUNGEON_PROFILE_FOLDER = "dungeon_pokemon_profiles";

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private PokemonProfileReloadListener() {
    }

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
        Map<ResourceLocation, PokemonSpawnProfileJson> spawnProfiles =
                loadSpawnProfiles(resourceManager);

        /*
         * Dungeon profile validation resolves spawn-profile references, so the
         * new spawn registry is installed before dungeon profiles are parsed.
         */
        PokemonSpawnProfileRegistry.replaceAll(spawnProfiles);

        Map<ResourceLocation, DungeonPokemonProfileJson> dungeonProfiles =
                loadDungeonProfiles(resourceManager);

        DungeonPokemonProfileRegistry.replaceAll(dungeonProfiles);

        LegendaryDungeons.LOGGER.info(
                "[Pokemon Profiles] Loaded {} spawn profiles and {} dungeon profiles.",
                PokemonSpawnProfileRegistry.size(),
                DungeonPokemonProfileRegistry.size()
        );
    }

    private static Map<ResourceLocation, PokemonSpawnProfileJson> loadSpawnProfiles(
            ResourceManager resourceManager
    ) {
        Map<ResourceLocation, PokemonSpawnProfileJson> loaded = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry :
                listJsonResources(resourceManager, SPAWN_PROFILE_FOLDER).entrySet()) {
            ResourceLocation fileId = entry.getKey();
            ResourceLocation profileId = profileId(fileId, SPAWN_PROFILE_FOLDER);
            PokemonSpawnProfileJson profile =
                    readJson(fileId, entry.getValue(), PokemonSpawnProfileJson.class);

            if (profileId != null
                    && PokemonSpawnProfileValidator.validate(fileId, profileId, profile)) {
                putDefinition(loaded, profileId, profile, fileId, "spawn profile");
            }
        }

        return loaded;
    }

    private static Map<ResourceLocation, DungeonPokemonProfileJson> loadDungeonProfiles(
            ResourceManager resourceManager
    ) {
        Map<ResourceLocation, DungeonPokemonProfileJson> loaded = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry :
                listJsonResources(resourceManager, DUNGEON_PROFILE_FOLDER).entrySet()) {
            ResourceLocation fileId = entry.getKey();
            ResourceLocation profileId = profileId(fileId, DUNGEON_PROFILE_FOLDER);
            DungeonPokemonProfileJson profile =
                    readJson(fileId, entry.getValue(), DungeonPokemonProfileJson.class);

            if (profileId != null
                    && DungeonPokemonProfileValidator.validate(fileId, profileId, profile)) {
                putDefinition(loaded, profileId, profile, fileId, "dungeon profile");
            }
        }

        return loaded;
    }

    private static Map<ResourceLocation, Resource> listJsonResources(
            ResourceManager resourceManager,
            String folder
    ) {
        return resourceManager.listResources(
                folder,
                resourceLocation -> resourceLocation.getPath().endsWith(".json")
        );
    }

    private static ResourceLocation profileId(
            ResourceLocation fileId,
            String folder
    ) {
        String prefix = folder + "/";
        String path = fileId.getPath();

        if (!path.startsWith(prefix) || !path.endsWith(".json")) {
            LegendaryDungeons.LOGGER.warn(
                    "[Pokemon Profiles] Could not derive a profile ID from {}.",
                    fileId
            );
            return null;
        }

        String relative = path.substring(prefix.length(), path.length() - ".json".length());

        if (relative.isBlank()) {
            LegendaryDungeons.LOGGER.warn(
                    "[Pokemon Profiles] Profile file {} has an empty relative path.",
                    fileId
            );
            return null;
        }

        return ResourceLocation.fromNamespaceAndPath(
                fileId.getNamespace(),
                relative
        );
    }

    private static <T> T readJson(
            ResourceLocation fileId,
            Resource resource,
            Class<T> type
    ) {
        try (Reader reader = resource.openAsReader()) {
            return GSON.fromJson(reader, type);
        } catch (JsonSyntaxException exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[Pokemon Profiles] {} has invalid JSON syntax: {}",
                    fileId,
                    exception.getMessage()
            );
            return null;
        } catch (Exception exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[Pokemon Profiles] Failed to read {}: {}",
                    fileId,
                    exception.getMessage()
            );
            return null;
        }
    }

    private static <T> void putDefinition(
            Map<ResourceLocation, T> loaded,
            ResourceLocation profileId,
            T profile,
            ResourceLocation fileId,
            String typeName
    ) {
        T previous = loaded.put(profileId, profile);

        if (previous != null) {
            LegendaryDungeons.LOGGER.warn(
                    "[Pokemon Profiles] Duplicate {} ID {}. File {} replaced an earlier definition.",
                    typeName,
                    profileId,
                    fileId
            );
        }
    }
}
