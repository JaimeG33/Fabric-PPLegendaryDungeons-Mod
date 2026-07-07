package porker.pp_legendarydungeons.summon;

import porker.pp_legendarydungeons.summon.aftermath.RayquazaAftermath;
import porker.pp_legendarydungeons.summon.aftermath.RayquazaSecretAftermath;
import porker.pp_legendarydungeons.summon.condition.RayquazaEmeraldRemovedCondition;
import porker.pp_legendarydungeons.summon.condition.RayquazaSecretNetherStarCondition;

import java.util.List;

/**
 * Stores all legendary summon definitions.
 *
 * Important:
 * More specific/special summons should usually be listed before normal summons.
 *
 * For Rayquaza:
 * 1. Secret shiny/perfect-IV Rayquaza
 * 2. Normal Rayquaza
 */
public final class LegendarySummonRegistry {
    private static final List<LegendarySummonDefinition> SUMMONS = List.of(
            /*
             * Secret shiny/perfect-IV Rayquaza.
             *
             * This checks first so the nether star secret has priority over
             * normal Rayquaza logic.
             */
            new LegendarySummonDefinition(
                    // Internal summon ID.
                    "rayquaza_secret",

                    // Cobblemon species string.
                    // This is still useful for logs/future fallback, even though
                    // this definition uses pokemonSpec.
                    "rayquaza",

                    // Pokémon level.
                    70,

                    // Full Cobblemon spec string.
                    // This is used instead of the simple species + level spawn.
                    "rayquaza level=70 shiny=true attack_iv=31 defence_iv=31 hp_iv=31 special_attack_iv=31 special_defence_iv=31 speed_iv=31",

                    // Search for this armor stand near pp_legendary_summon.
                    "pp_rayquaza_conditions",

                    // Preferred final spawn marker.
                    "pp_summon_rayquaza",

                    // Search radius from pp_legendary_summon to pp_rayquaza_conditions.
                    24.0D,

                    // Search radius from pp_rayquaza_conditions to pp_summon_rayquaza.
                    32.0D,

                    // Secret Rayquaza condition.
                    new RayquazaSecretNetherStarCondition(),

                    // Secret Rayquaza aftermath.
                    new RayquazaSecretAftermath()
            ),

            /*
             * Normal Rayquaza.
             *
             * This checks after the secret version.
             */
            new LegendarySummonDefinition(
                    // Internal summon ID.
                    "rayquaza",

                    // Cobblemon species string.
                    "rayquaza",

                    // Pokémon level.
                    70,

                    // No special spec string.
                    // This means the service uses species + level.
                    null,

                    // Search for this armor stand near pp_legendary_summon.
                    "pp_rayquaza_conditions",

                    // Preferred final spawn marker.
                    "pp_summon_rayquaza",

                    // Search radius from pp_legendary_summon to pp_rayquaza_conditions.
                    24.0D,

                    // Search radius from pp_rayquaza_conditions to pp_summon_rayquaza.
                    32.0D,

                    // Normal Rayquaza condition.
                    new RayquazaEmeraldRemovedCondition(),

                    // Normal Rayquaza aftermath.
                    new RayquazaAftermath()
            )
    );

    private LegendarySummonRegistry() {
    }

    public static List<LegendarySummonDefinition> all() {
        return SUMMONS;
    }
}