package porker.pp_legendarydungeons.secrets;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class SecretTicker {
    private static final String SECRET_STARTER_TAG = "scs_secret_starter";
    private static final double PLAYER_TRIGGER_DISTANCE = 20.0D;

    private static int ticks = 0;

    private SecretTicker() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ticks++;

            if (ticks % 20 != 0) {
                return;
            }

            for (ServerLevel level : server.getAllLevels()) {
                checkLevel(level);
            }
        });
    }

    private static void checkLevel(ServerLevel level) {
        Set<UUID> processedStarters = new HashSet<>();

        for (ServerPlayer player : level.players()) {
            AABB searchBox = player.getBoundingBox().inflate(PLAYER_TRIGGER_DISTANCE);

            List<ArmorStand> starters = level.getEntitiesOfClass(
                    ArmorStand.class,
                    searchBox,
                    armorStand ->
                            armorStand.isAlive()
                                    && armorStand.getTags().contains(SECRET_STARTER_TAG)
                                    && armorStand.distanceToSqr(player)
                                    <= PLAYER_TRIGGER_DISTANCE * PLAYER_TRIGGER_DISTANCE
            );

            for (ArmorStand starter : starters) {
                if (!processedStarters.add(starter.getUUID())) {
                    continue;
                }

                SecretContext context = new SecretContext(level, player, starter);
                SecretService.checkSecrets(context);
            }
        }
    }
}