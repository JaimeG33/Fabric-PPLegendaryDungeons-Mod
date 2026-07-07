package porker.pp_legendarydungeons.summon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * LegendarySummonService is the central coordinator for legendary summons.
 *
 * Trigger files should call this.
 *
 * It does the universal summon pipeline:
 *
 * 1. Loop through registered legendary summon definitions.
 * 2. Ask each definition's condition if it can summon.
 * 3. If yes, get the prepared context.
 * 4. Use LegendarySummonHelper to spawn the Cobblemon Pokémon.
 * 5. If spawn succeeds, run the aftermath.
 */
public final class LegendarySummonService {
    private LegendarySummonService() {
    }

    /**
     * Attempts to perform a legendary summon from the given context.
     *
     * Returns true if a summon successfully happened.
     * Returns false if no condition matched or the Pokémon failed to spawn.
     */
    public static boolean trySummon(SummonContext originalContext) {
        for (LegendarySummonDefinition definition : LegendarySummonRegistry.all()) {
            Optional<SummonContext> preparedContext =
                    definition.condition().prepareContext(originalContext, definition);

            if (preparedContext.isEmpty()) {
                continue;
            }

            SummonContext context = preparedContext.get();
            BlockPos spawnPos = context.spawnPos();

            Optional<PokemonEntity> spawned = spawnPokemonForDefinition(
                    definition,
                    context,
                    spawnPos
            );

            if (spawned.isEmpty()) {
                return false;
            }

            definition.aftermath().run(context, definition, spawned.get());

            return true;
        }

        return false;
    }

    /**
     * Chooses whether this summon should use:
     *
     * - full Cobblemon spec string, or
     * - simple species + level
     */
    private static Optional<PokemonEntity> spawnPokemonForDefinition(
            LegendarySummonDefinition definition,
            SummonContext context,
            BlockPos spawnPos
    ) {
        double x = spawnPos.getX() + 0.5D;
        double y = spawnPos.getY();
        double z = spawnPos.getZ() + 0.5D;

        if (definition.hasPokemonSpec()) {
            return LegendarySummonHelper.spawnPokemonFromSpec(
                    context.level(),
                    definition.pokemonSpec(),
                    x,
                    y,
                    z,
                    0.0F,
                    0.0F
            );
        }

        return LegendarySummonHelper.spawnPokemon(
                context.level(),
                definition.species(),
                definition.level(),
                x,
                y,
                z,
                0.0F,
                0.0F
        );
    }
}