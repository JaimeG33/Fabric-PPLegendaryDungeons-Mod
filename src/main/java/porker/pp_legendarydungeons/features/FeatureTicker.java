package porker.pp_legendarydungeons.features;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRulePlayerTracker;

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
 * The same low-frequency player pass now also updates dungeon-zone player state.
 * Dungeon rule action checks remain event/mixin driven and do not scan entities.
 */
public final class FeatureTicker {
    public static final String FEATURE_MARKER_TAG = "pp_feature";

    private static final double PLAYER_TRIGGER_DISTANCE = 128.0D;
    private static final double PLAYER_TRIGGER_DISTANCE_SQUARED =
            PLAYER_TRIGGER_DISTANCE * PLAYER_TRIGGER_DISTANCE;

    private static final int SCAN_INTERVAL_TICKS = 30;

    private FeatureTicker() {
    }

    /**
     * Called once per server tick by the shared Architectury scheduler.
     */
    public static void tick(MinecraftServer server, long tickCount) {
        if (tickCount % SCAN_INTERVAL_TICKS != 0L) {
            return;
        }

        for (ServerLevel level : server.getAllLevels()) {
            checkLevel(level);
        }
    }

    private static void checkLevel(ServerLevel level) {
        Set<UUID> processedFeatureMarkers = new HashSet<>();

        for (ServerPlayer player : level.players()) {
            /*
             * This is a chunk-indexed lookup, not a block/entity scan.
             * It handles mining fatigue, pp_in_dungeon_zone, and builder previews.
             */
            DungeonRulePlayerTracker.checkPlayer(level, player);

            AABB searchBox = player.getBoundingBox().inflate(PLAYER_TRIGGER_DISTANCE);

            List<ArmorStand> featureMarkers = level.getEntitiesOfClass(
                    ArmorStand.class,
                    searchBox,
                    armorStand ->
                            armorStand.isAlive()
                                    && armorStand.getTags().contains(FEATURE_MARKER_TAG)
                                    && armorStand.distanceToSqr(player)
                                    <= PLAYER_TRIGGER_DISTANCE_SQUARED
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
