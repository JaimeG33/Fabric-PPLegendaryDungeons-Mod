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
 * Builder-only particle previews for dungeon-rule controllers.
 *
 * A player can have at most one active preview. A preview remains enabled until
 * the same controller is toggled off, another controller is toggled on, or the
 * server stops.
 *
 * Particles are still refreshed only through the existing low-frequency player
 * pulse, so an enabled preview does not create a new per-tick task.
 */
public final class DungeonRulePreviewManager {
    private static final Map<UUID, PreviewSession> SESSIONS = new HashMap<>();

    private DungeonRulePreviewManager() {
    }

    public static boolean toggleParent(ServerPlayer player, BlockPos sourcePos) {
        return toggle(
                player,
                sourcePos,
                sourcePos,
                sourcePos
        );
    }

    public static boolean toggleZone(
            ServerPlayer player,
            BlockPos sourcePos,
            BlockPos minPos,
            BlockPos maxPos
    ) {
        return toggle(
                player,
                sourcePos,
                minPos,
                maxPos
        );
    }

    private static boolean toggle(
            ServerPlayer player,
            BlockPos sourcePos,
            BlockPos minPos,
            BlockPos maxPos
    ) {
        UUID playerId = player.getUUID();
        ResourceKey<Level> dimension = player.serverLevel().dimension();
        PreviewSession existing = SESSIONS.get(playerId);

        if (existing != null
                && existing.dimension().equals(dimension)
                && existing.sourcePos().equals(sourcePos)) {
            SESSIONS.remove(playerId);
            return false;
        }

        SESSIONS.put(
                playerId,
                new PreviewSession(
                        dimension,
                        sourcePos.immutable(),
                        minPos.immutable(),
                        maxPos.immutable()
                )
        );

        return true;
    }

    public static boolean isPreviewing(ServerPlayer player, BlockPos sourcePos) {
        PreviewSession session = SESSIONS.get(player.getUUID());

        return session != null
                && session.dimension().equals(player.serverLevel().dimension())
                && session.sourcePos().equals(sourcePos);
    }

    public static void tickPlayer(ServerLevel level, ServerPlayer player) {
        PreviewSession session = SESSIONS.get(player.getUUID());

        if (session == null || !session.dimension().equals(level.dimension())) {
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

    private static void send(
            ServerLevel level,
            ServerPlayer player,
            int x,
            int y,
            int z
    ) {
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
            BlockPos sourcePos,
            BlockPos minPos,
            BlockPos maxPos
    ) {
    }
}
