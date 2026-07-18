package porker.pp_legendarydungeons.summon;

import porker.pp_legendarydungeons.summon.aftermath.LegendarySummonAftermath;
import porker.pp_legendarydungeons.summon.condition.LegendarySummonCondition;

/**
 * LegendarySummonDefinition describes one legendary summon type.
 *
 * Think of this as the config/data for a legendary encounter.
 */
public record LegendarySummonDefinition(
        /**
         * Internal ID for this summon.
         *
         * Used to build tags like:
         * pp_rayquaza_summoned
         * pp_rayquaza_secret_summoned
         */
        String id,

        /**
         * Cobblemon species string.
         *
         * Used for simple species + level spawns.
         *
         * Example:
         * rayquaza
         */
        String species,

        /**
         * Level of the Pokémon that will be spawned.
         *
         * Used for simple species + level spawns.
         */
        int level,

        /**
         * Optional full Cobblemon spec string.
         *
         * If this is not null and not blank, LegendarySummonService will use this
         * instead of the simple species + level spawn.
         *
         * Use this for special summons like shiny/perfect-IV Rayquaza.
         */
        String pokemonSpec,

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
         */
        LegendarySummonCondition condition,

        /**
         * The aftermath logic for this legendary.
         */
        LegendarySummonAftermath aftermath
) {
    /**
     * Returns a reusable "this summon was already used" tag.
     *
     * For normal Rayquaza:
     * pp_rayquaza_summoned
     *
     * For secret Rayquaza:
     * pp_rayquaza_secret_summoned
     */
    public String usedTag() {
        return "pp_" + id + "_summoned";
    }

    /**
     * Whether this definition should spawn from a full Cobblemon spec string.
     */
    public boolean hasPokemonSpec() {
        return pokemonSpec != null && !pokemonSpec.isBlank();
    }
}