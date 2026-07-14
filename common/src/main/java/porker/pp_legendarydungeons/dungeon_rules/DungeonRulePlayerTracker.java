
package porker.pp_legendarydungeons.dungeon_rules;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Lightweight player-state portion of dungeon rules.
 *
 * Called from the existing FeatureTicker pulse. Event-driven restrictions do not
 * use this tag; they query the exact action position directly.
 */
public final class DungeonRulePlayerTracker {
    public static final String IN_DUNGEON_TAG = "pp_in_dungeon_zone";

    private DungeonRulePlayerTracker() {
    }

    public static void checkPlayer(ServerLevel level, ServerPlayer player) {
        boolean insideDungeon = DungeonRuleManager
                .findInstanceAt(level, player.blockPosition())
                .isPresent();

        if (insideDungeon) {
            player.addTag(IN_DUNGEON_TAG);
        } else {
            player.removeTag(IN_DUNGEON_TAG);
        }

        if (DungeonRuleManager.isRuleEnforced(
                level,
                player.blockPosition(),
                DungeonRule.MINING_FATIGUE
        )) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    60,
                    2,
                    false,
                    false,
                    true
            ));
        }

        DungeonRulePreviewManager.tickPlayer(level, player);
    }
}
