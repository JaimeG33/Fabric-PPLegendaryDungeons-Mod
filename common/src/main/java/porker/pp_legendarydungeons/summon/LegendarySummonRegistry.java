package porker.pp_legendarydungeons.summon;

import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.summon.aftermath.RayquazaAftermath;
import porker.pp_legendarydungeons.summon.aftermath.RayquazaSecretAftermath;
import porker.pp_legendarydungeons.summon.condition.RayquazaEmeraldRemovedCondition;
import porker.pp_legendarydungeons.summon.condition.RayquazaSecretNetherStarCondition;

import java.util.List;

/**
 * Stores all legendary encounter definitions.
 *
 * More specific/special summons are evaluated before normal summons.
 */
public final class LegendarySummonRegistry {
    private static final List<LegendarySummonDefinition> SUMMONS = List.of(
            new LegendarySummonDefinition(
                    "rayquaza_secret",
                    profile("legendary/rayquaza/secret"),
                    "rayquaza level=70 shiny=true attack_iv=31 defence_iv=31 hp_iv=31 special_attack_iv=31 special_defence_iv=31 speed_iv=31",
                    "pp_rayquaza_conditions",
                    "pp_summon_rayquaza",
                    24.0D,
                    48.0D,
                    new RayquazaSecretNetherStarCondition(),
                    new RayquazaSecretAftermath()
            ),
            new LegendarySummonDefinition(
                    "rayquaza",
                    profile("legendary/rayquaza/normal"),
                    "rayquaza level=70",
                    "pp_rayquaza_conditions",
                    "pp_summon_rayquaza",
                    24.0D,
                    48.0D,
                    new RayquazaEmeraldRemovedCondition(),
                    new RayquazaAftermath()
            )
    );

    private LegendarySummonRegistry() {
    }

    private static ResourceLocation profile(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                LegendaryDungeons.MOD_ID,
                path
        );
    }

    public static List<LegendarySummonDefinition> all() {
        return SUMMONS;
    }
}
