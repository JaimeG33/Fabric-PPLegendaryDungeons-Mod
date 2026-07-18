package porker.pp_legendarydungeons.summon;

import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.summon.aftermath.LegendarySummonAftermath;
import porker.pp_legendarydungeons.summon.condition.LegendarySummonCondition;

/**
 * Legendary encounter configuration.
 *
 * Pokémon generation is delegated to a reusable spawn profile while triggers,
 * conditions, one-time locks, and aftermath remain legendary-specific.
 */
public record LegendarySummonDefinition(
        String id,
        ResourceLocation pokemonSpawnProfileId,
        String fallbackPokemonSpec,
        String conditionTag,
        String spawnMarkerTag,
        double conditionSearchRadius,
        double spawnMarkerSearchRadius,
        LegendarySummonCondition condition,
        LegendarySummonAftermath aftermath
) {
    public String usedTag() {
        return "pp_" + id + "_summoned";
    }
}
