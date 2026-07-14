
package porker.pp_legendarydungeons.dungeon_rules.preset;

import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRule;

import java.util.EnumSet;

/**
 * Reusable defaults shared by many generated dungeon instances.
 */
public record DungeonRulePreset(
        ResourceLocation id,
        String displayName,
        EnumSet<DungeonRule> defaultRules
) {
}
