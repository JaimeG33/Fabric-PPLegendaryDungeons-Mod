package porker.pp_legendarydungeons.features;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Central scanner for regular/static structure features.
 *
 * This is the Java replacement for the old datapack static_features.mcfunction idea.
 *
 * Structures should include an armor stand marker tagged:
 * - pp_feature
 *
 * Then nearby feature-specific markers can be used, for example:
 * - pp_wtraders
 *
 * This ticker intentionally checks near players instead of scanning the whole world.
 */
public final class FeatureTicker {
    public static final String FEATURE_MARKER_TAG = "pp_feature";

    /**
     * Similar to the old datapack static feature scan distance.
     */
    private static final double PLAYER_TRIGGER_DISTANCE = 128.0D;
    private static final double PLAYER_TRIGGER_DISTANCE_SQUARED =
            PLAYER_TRIGGER_DISTANCE * PLAYER_TRIGGER_DISTANCE;

    /**
     * Old datapack scanner ran every 30 ticks.
     * Keeping that cadence here avoids unnecessary entity scans.
     */
    private static final int SCAN_INTERVAL_TICKS = 30;

    private static int ticks = 0;

    private FeatureTicker() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ticks++;

            if (ticks % SCAN_INTERVAL_TICKS != 0) {
                return;
            }

            for (ServerLevel level : server.getAllLevels()) {
                checkLevel(level);
            }
        });
    }

    private static void checkLevel(ServerLevel level) {
        Set<UUID> processedFeatureMarkers = new HashSet<>();

        for (ServerPlayer player : level.players()) {
            AABB searchBox = player.getBoundingBox().inflate(PLAYER_TRIGGER_DISTANCE);

            List<ArmorStand> featureMarkers = level.getEntitiesOfClass(
                    ArmorStand.class,
                    searchBox,
                    armorStand ->
                            armorStand.isAlive()
                                    && armorStand.getTags().contains(FEATURE_MARKER_TAG)
                                    && armorStand.distanceToSqr(player) <= PLAYER_TRIGGER_DISTANCE_SQUARED
            );

            for (ArmorStand featureMarker : featureMarkers) {
                if (!processedFeatureMarkers.add(featureMarker.getUUID())) {
                    continue;
                }

                FeatureContext context = new FeatureContext(level, player, featureMarker);
                FeatureService.checkFeatures(context);
            }
        }
    }
}
