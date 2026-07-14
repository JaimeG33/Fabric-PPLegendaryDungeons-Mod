
package porker.pp_legendarydungeons.dungeon_rules;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Prevents denial messages from spamming every tick while a player holds a button.
 */
public final class DungeonRuleFeedback {
    private static final int MESSAGE_COOLDOWN_TICKS = 20;
    private static final Map<UUID, Integer> LAST_MESSAGE_TICK = new HashMap<>();

    private DungeonRuleFeedback() {
    }

    public static void denied(ServerPlayer player, String message) {
        int now = player.tickCount;
        int last = LAST_MESSAGE_TICK.getOrDefault(player.getUUID(), Integer.MIN_VALUE / 2);

        if (now - last < MESSAGE_COOLDOWN_TICKS) {
            return;
        }

        LAST_MESSAGE_TICK.put(player.getUUID(), now);
        player.displayClientMessage(
                Component.literal(message).withStyle(ChatFormatting.RED),
                true
        );
    }

    public static void clear() {
        LAST_MESSAGE_TICK.clear();
    }
}
