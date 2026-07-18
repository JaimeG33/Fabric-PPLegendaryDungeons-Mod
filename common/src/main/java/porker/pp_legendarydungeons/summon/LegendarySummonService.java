package porker.pp_legendarydungeons.summon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.pokemon.spawn.PokemonSpawnService;

import java.util.Optional;

/**
 * Central coordinator for legendary encounter triggers and aftermath.
 *
 * The actual Pokémon entity is now created by the general-purpose
 * PokemonSpawnService.
 */
public final class LegendarySummonService {
    private LegendarySummonService() {
    }

    public static boolean trySummon(SummonContext originalContext) {
        for (LegendarySummonDefinition definition : LegendarySummonRegistry.all()) {
            Optional<SummonContext> preparedContext =
                    definition.condition().prepareContext(
                            originalContext,
                            definition
                    );

            if (preparedContext.isEmpty()) {
                continue;
            }

            SummonContext context = preparedContext.get();
            Optional<PokemonEntity> spawned =
                    spawnPokemonForDefinition(definition, context);

            if (spawned.isEmpty()) {
                return false;
            }

            definition.aftermath().run(
                    context,
                    definition,
                    spawned.get()
            );

            return true;
        }

        return false;
    }

    private static Optional<PokemonEntity> spawnPokemonForDefinition(
            LegendarySummonDefinition definition,
            SummonContext context
    ) {
        BlockPos spawnPos = context.spawnPos();
        double x = spawnPos.getX() + 0.5D;
        double y = spawnPos.getY();
        double z = spawnPos.getZ() + 0.5D;

        Optional<PokemonEntity> profileSpawn =
                PokemonSpawnService.spawnProfile(
                        context.level(),
                        definition.pokemonSpawnProfileId(),
                        x,
                        y,
                        z,
                        0.0F,
                        0.0F
                );

        if (profileSpawn.isPresent()) {
            return profileSpawn;
        }

        /*
         * Built-in legendary encounters retain a code fallback so a malformed
         * external datapack cannot permanently disable the existing summon.
         */
        LegendaryDungeons.LOGGER.warn(
                "[Legendary Summon] Profile {} was unavailable; using fallback spec for {}.",
                definition.pokemonSpawnProfileId(),
                definition.id()
        );

        return PokemonSpawnService.spawnFromSpec(
                context.level(),
                definition.fallbackPokemonSpec(),
                x,
                y,
                z,
                0.0F,
                0.0F
        );
    }
}
