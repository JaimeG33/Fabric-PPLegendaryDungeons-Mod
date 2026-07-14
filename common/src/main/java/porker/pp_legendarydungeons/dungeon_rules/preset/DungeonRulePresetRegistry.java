
package porker.pp_legendarydungeons.dungeon_rules.preset;

import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRule;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Small Java-backed preset registry.
 *
 * Resource-style IDs are used now so these definitions can move to JSON later
 * without changing saved block data.
 */
public final class DungeonRulePresetRegistry {
    public static final ResourceLocation STANDARD_DUNGEON_ID = id("standard_dungeon");
    public static final ResourceLocation SKY_PILLAR_ID = id("sky_pillar");

    private static final Map<ResourceLocation, DungeonRulePreset> PRESETS = new LinkedHashMap<>();
    private static boolean bootstrapped;

    private DungeonRulePresetRegistry() {
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }

        EnumSet<DungeonRule> standardRules = EnumSet.of(
                DungeonRule.BLOCK_BREAKING,
                DungeonRule.BLOCK_PLACEMENT,
                DungeonRule.MINING_FATIGUE,
                DungeonRule.EXPLOSIONS,
                DungeonRule.BED_USE,
                DungeonRule.PC_USE,
                DungeonRule.HEALER_USE,
                DungeonRule.PORTABLE_PC_USE,
                DungeonRule.PORTABLE_HEALER_USE
        );

        register(new DungeonRulePreset(
                STANDARD_DUNGEON_ID,
                "Standard Dungeon",
                standardRules.clone()
        ));

        register(new DungeonRulePreset(
                SKY_PILLAR_ID,
                "Sky Pillar",
                standardRules.clone()
        ));

        bootstrapped = true;
    }

    private static void register(DungeonRulePreset preset) {
        PRESETS.put(preset.id(), preset);
    }

    public static Optional<DungeonRulePreset> get(ResourceLocation id) {
        bootstrap();
        return Optional.ofNullable(PRESETS.get(id));
    }

    public static DungeonRulePreset getOrDefault(ResourceLocation id) {
        bootstrap();
        return PRESETS.getOrDefault(id, PRESETS.get(STANDARD_DUNGEON_ID));
    }

    public static Map<ResourceLocation, DungeonRulePreset> all() {
        bootstrap();
        return Collections.unmodifiableMap(PRESETS);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                ProfessorPorkersLegendaryDungeons.MOD_ID,
                path
        );
    }
}
