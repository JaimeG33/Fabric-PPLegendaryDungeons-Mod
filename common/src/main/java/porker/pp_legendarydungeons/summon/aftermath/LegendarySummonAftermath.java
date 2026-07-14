package porker.pp_legendarydungeons.summon.aftermath;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import porker.pp_legendarydungeons.summon.LegendarySummonDefinition;
import porker.pp_legendarydungeons.summon.SummonContext;

/**
 * Aftermath logic runs only after the Pokémon successfully spawns.
 *
 * This is where you put things like:
 * - removing the required item
 * - marking the marker as used
 * - playing sounds
 * - spawning particles
 * - sending messages
 * - changing blocks
 * - starting a boss phase
 */
public interface LegendarySummonAftermath {
    void run(
            SummonContext context,
            LegendarySummonDefinition definition,
            PokemonEntity spawnedPokemon
    );
}