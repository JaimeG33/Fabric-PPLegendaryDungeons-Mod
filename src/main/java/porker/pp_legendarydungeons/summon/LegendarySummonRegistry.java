package porker.pp_legendarydungeons.summon;

import porker.pp_legendarydungeons.summon.aftermath.RayquazaAftermath;
import porker.pp_legendarydungeons.summon.condition.RayquazaEmeraldBlockCondition;

import java.util.List;

/**
 * Stores all legendary summon definitions.
 *
 * For now, only Rayquaza exists.
 *
 * Later, adding a simple Diancie summon would mostly mean:
 * 1. Create DiancieCondition.java
 * 2. Create DiancieAftermath.java
 * 3. Add another LegendarySummonDefinition to this list.
 *
 * You do NOT need to create a new global ticker for every legendary.
 */
public final class LegendarySummonRegistry {
    private static final List<LegendarySummonDefinition> SUMMONS = List.of(
            new LegendarySummonDefinition(
                    // Internal summon ID.
                    "rayquaza",

                    // Cobblemon species string.
                    "rayquaza",

                    // Pokémon level.
                    70,

                    // Search for this armor stand near pp_legendary_summon.
                    "pp_rayquaza_conditions",

                    // Preferred final spawn marker.
                    "pp_summon_rayquaza",

                    // Search radius from pp_legendary_summon to pp_rayquaza_conditions.
                    16.0D,

                    // Search radius from pp_rayquaza_conditions to pp_summon_rayquaza.
                    24.0D,

                    // Rayquaza-specific condition checker.
                    new RayquazaEmeraldBlockCondition(),

                    // Rayquaza-specific aftermath.
                    new RayquazaAftermath()
            )
    );

    private LegendarySummonRegistry() {
    }

    /**
     * Returns all registered legendary summons.
     *
     * LegendarySummonService loops over this list when a player gets near a summon marker.
     */
    public static List<LegendarySummonDefinition> all() {
        return SUMMONS;
    }
}