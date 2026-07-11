
package porker.pp_legendarydungeons.dungeon_rules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Temporary builder-only particle preview.
 *
 * It runs only for players who explicitly press Preview in the configuration UI.
 */
public final class DungeonRulePreviewManager {
    private static final int PREVIEW_DURATION_TICKS = 20 * 20;
    private static final Map<UUID, PreviewSession> SESSIONS = new HashMap<>();

    private DungeonRulePreviewManager() {
    }

    public static void previewParent(ServerPlayer player, BlockPos pos) {
        SESSIONS.put(
                player.getUUID(),
                new PreviewSession(
                        player.serverLevel().dimension(),
                        pos,
                        pos,
                        player.serverLevel().getServer().getTickCount() + PREVIEW_DURATION_TICKS
                )
        );
    }

    public static void previewZone(ServerPlayer player, BlockPos minPos, BlockPos maxPos) {
        SESSIONS.put(
                player.getUUID(),
                new PreviewSession(
                        player.serverLevel().dimension(),
                        minPos,
                        maxPos,
                        player.serverLevel().getServer().getTickCount() + PREVIEW_DURATION_TICKS
                )
        );
    }

    public static void tickPlayer(ServerLevel level, ServerPlayer player) {
        PreviewSession session = SESSIONS.get(player.getUUID());

        if (session == null) {
            return;
        }

        if (player.serverLevel().getServer().getTickCount() > session.expiresAtTick()) {
            SESSIONS.remove(player.getUUID());
            return;
        }

        if (!session.dimension().equals(level.dimension())) {
            return;
        }

        drawBox(level, player, session.minPos(), session.maxPos());
    }

    private static void drawBox(
            ServerLevel level,
            ServerPlayer player,
            BlockPos min,
            BlockPos max
    ) {
        int stepX = Math.max(1, (max.getX() - min.getX() + 1) / 16);
        int stepY = Math.max(1, (max.getY() - min.getY() + 1) / 16);
        int stepZ = Math.max(1, (max.getZ() - min.getZ() + 1) / 16);

        for (int x = min.getX(); x <= max.getX(); x += stepX) {
            send(level, player, x, min.getY(), min.getZ());
            send(level, player, x, min.getY(), max.getZ());
            send(level, player, x, max.getY(), min.getZ());
            send(level, player, x, max.getY(), max.getZ());
        }

        for (int y = min.getY(); y <= max.getY(); y += stepY) {
            send(level, player, min.getX(), y, min.getZ());
            send(level, player, min.getX(), y, max.getZ());
            send(level, player, max.getX(), y, min.getZ());
            send(level, player, max.getX(), y, max.getZ());
        }

        for (int z = min.getZ(); z <= max.getZ(); z += stepZ) {
            send(level, player, min.getX(), min.getY(), z);
            send(level, player, min.getX(), max.getY(), z);
            send(level, player, max.getX(), min.getY(), z);
            send(level, player, max.getX(), max.getY(), z);
        }
    }

    private static void send(ServerLevel level, ServerPlayer player, int x, int y, int z) {
        level.sendParticles(
                player,
                ParticleTypes.END_ROD,
                false,
                x + 0.5D,
                y + 0.5D,
                z + 0.5D,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );
    }

    public static void clear() {
        SESSIONS.clear();
    }

    private record PreviewSession(
            ResourceKey<Level> dimension,
            BlockPos minPos,
            BlockPos maxPos,
            int expiresAtTick
    ) {
    }
}
