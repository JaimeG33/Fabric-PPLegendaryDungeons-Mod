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
 *
 * This keeps your trigger logic, condition logic, spawn logic, and aftermath logic separated.
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
        // Check every registered legendary summon.
        // Right now this only checks Rayquaza.
        for (LegendarySummonDefinition definition : LegendarySummonRegistry.all()) {
            // Ask this legendary's condition whether it can summon.
            //
            // For Rayquaza, this checks:
            // - nearby pp_rayquaza_conditions
            // - emerald block in hand
            // - pp_summon_rayquaza spawn marker or fallback
            Optional<SummonContext> preparedContext =
                    definition.condition().prepareContext(originalContext, definition);

            // If this definition's conditions are not met, check the next definition.
            if (preparedContext.isEmpty()) {
                continue;
            }

            SummonContext context = preparedContext.get();
            BlockPos spawnPos = context.spawnPos();

            // Actually create and spawn the Cobblemon PokemonEntity.
            Optional<PokemonEntity> spawned = LegendarySummonHelper.spawnPokemon(
                    context.level(),
                    definition.species(),
                    definition.level(),
                    spawnPos.getX() + 0.5D,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5D,
                    0.0F,
                    0.0F
            );

            // If Cobblemon spawning failed, do not run aftermath.
            // This means the emerald block is not consumed unless Rayquaza really spawned.
            if (spawned.isEmpty()) {
                return false;
            }

            // Now that the Pokémon exists, run the summon-specific aftermath.
            definition.aftermath().run(context, definition, spawned.get());

            return true;
        }

        // No registered legendary condition matched.
        return false;
    }
}