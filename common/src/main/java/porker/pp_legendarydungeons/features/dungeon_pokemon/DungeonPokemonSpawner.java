package porker.pp_legendarydungeons.features.dungeon_pokemon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.features.FeatureContext;
import porker.pp_legendarydungeons.pokemon.spawn.PokemonSpawnService;

import java.util.Optional;

/**
 * Converts one structure marker into a fresh, persistent Cobblemon entity.
 */
public final class DungeonPokemonSpawner {
    private DungeonPokemonSpawner() {
    }

    public static boolean spawn(
            FeatureContext context,
            ArmorStand marker,
            ResourceLocation dungeonProfileId
    ) {
        Optional<DungeonPokemonProfileJson> dungeonProfile =
                DungeonPokemonProfileRegistry.get(dungeonProfileId);

        if (dungeonProfile.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "[Dungeon Pokemon] Missing dungeon profile {} at {}.",
                    dungeonProfileId,
                    marker.blockPosition()
            );
            return false;
        }

        ResourceLocation spawnProfileId;

        try {
            spawnProfileId = ResourceLocation.parse(
                    dungeonProfile.get().spawn_profile
            );
        } catch (Exception exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[Dungeon Pokemon] Invalid spawn profile in {}.",
                    dungeonProfileId
            );
            return false;
        }

        BlockPos home = marker.blockPosition();

        DungeonPokemonProfileJson.Capture capture =
                dungeonProfile.get().capture == null
                        ? new DungeonPokemonProfileJson.Capture()
                        : dungeonProfile.get().capture;

        String additionalProperties =
                capture.allowed ? "" : "uncatchable=true";

        Optional<PokemonEntity> spawned = PokemonSpawnService.spawnProfile(
                context.level(),
                spawnProfileId,
                marker.getX(),
                marker.getY(),
                marker.getZ(),
                marker.getYRot(),
                marker.getXRot(),
                additionalProperties
        );

        if (spawned.isEmpty()) {
            return false;
        }

        PokemonEntity pokemon = spawned.get();
        pokemon.setPersistenceRequired();

        DungeonPokemonSpawnOptions.apply(
                context.level(),
                pokemon,
                dungeonProfileId,
                dungeonProfile.get()
        );

        /*
         * PokemonSpawnService already applied the spawn profile's
         * counts_towards_spawn_cap value. Do not overwrite it here.
         */
        String instanceId = DungeonRuleManager
                .findInstanceAt(context.level(), home)
                .orElse("");

        DungeonPokemonManager.register(
                pokemon,
                dungeonProfileId,
                home,
                instanceId
        );

        LegendaryDungeons.LOGGER.info(
                "[Dungeon Pokemon] Spawned profile {} using {} at {}.",
                dungeonProfileId,
                spawnProfileId,
                home
        );

        return true;
    }
}
