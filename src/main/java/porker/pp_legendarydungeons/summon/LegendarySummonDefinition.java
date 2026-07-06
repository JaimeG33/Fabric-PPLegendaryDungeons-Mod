package porker.pp_legendarydungeons.summon;

import porker.pp_legendarydungeons.summon.aftermath.LegendarySummonAftermath;
import porker.pp_legendarydungeons.summon.condition.LegendarySummonCondition;

/**
 * LegendarySummonDefinition describes one legendary summon type.
 *
 * Think of this as the config/data for a legendary encounter.
 *
 * For Rayquaza, this definition says:
 * - summon id = rayquaza
 * - Cobblemon species = rayquaza
 * - level = 70
 * - condition marker tag = pp_rayquaza_conditions
 * - spawn marker tag = pp_summon_rayquaza
 * - condition checker = RayquazaEmeraldBlockCondition
 * - aftermath = RayquazaAftermath
 */
public record LegendarySummonDefinition(
        /**
         * Internal ID for this summon.
         *
         * Used to build tags like:
         * pp_rayquaza_summoned
         */
        String id,

        /**
         * Cobblemon species string.
         *
         * Example:
         * rayquaza
         * diancie
         * hoopa
         */
        String species,

        /**
         * Level of the Pokémon that will be spawned.
         */
        int level,

        /**
         * Armor stand tag used to find the condition marker for this legendary.
         *
         * For Rayquaza:
         * pp_rayquaza_conditions
         */
        String conditionTag,

        /**
         * Armor stand tag used to find the preferred spawn position.
         *
         * For Rayquaza:
         * pp_summon_rayquaza
         */
        String spawnMarkerTag,

        /**
         * How far from the main pp_legendary_summon marker to search for the condition marker.
         */
        double conditionSearchRadius,

        /**
         * How far from the condition marker to search for the final spawn marker.
         */
        double spawnMarkerSearchRadius,

        /**
         * The condition logic for this legendary.
         *
         * This checks whether the summon requirements are met.
         */
        LegendarySummonCondition condition,

        /**
         * The aftermath logic for this legendary.
         *
         * This only runs after the Pokémon successfully spawns.
         */
        LegendarySummonAftermath aftermath
) {
    /**
     * Returns a reusable "this summon was already used" tag.
     *
     * For Rayquaza, this becomes:
     * pp_rayquaza_summoned
     */
    public String usedTag() {
        return "pp_" + id + "_summoned";
    }
}