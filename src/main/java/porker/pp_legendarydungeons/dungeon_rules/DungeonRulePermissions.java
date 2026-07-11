
package porker.pp_legendarydungeons.dungeon_rules;

import net.minecraft.world.entity.player.Player;

/**
 * Creative operators bypass dungeon restrictions so builders can edit structures.
 */
public final class DungeonRulePermissions {
    private DungeonRulePermissions() {
    }

    public static boolean canBypass(Player player) {
        return player != null && player.isCreative() && player.hasPermissions(2);
    }
}
