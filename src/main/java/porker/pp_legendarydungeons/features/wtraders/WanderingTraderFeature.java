package porker.pp_legendarydungeons.features.wtraders;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.features.FeatureContext;

import java.util.List;

/**
 * Handles structure-spawned wandering trader markers.
 *
 * Initial test behavior:
 * - Find a pp_wtraders armor stand near a pp_feature marker.
 * - Spawn the Sky Pillar map trader.
 * - Remove the pp_wtraders marker so the trader cannot spawn repeatedly.
 *
 * Later, this class can roll:
 * - 20% regular wandering trader
 * - 40% custom wandering trader without map
 * - 40% custom wandering trader with map
 */
public final class WanderingTraderFeature {
    public static final String WTRADER_MARKER_TAG = "pp_wtraders";

    /**
     * Old datapack checked around the static feature marker for trader markers within 80 blocks.
     */
    private static final double WTRADER_MARKER_DISTANCE = 80.0D;
    private static final double WTRADER_MARKER_DISTANCE_SQUARED =
            WTRADER_MARKER_DISTANCE * WTRADER_MARKER_DISTANCE;

    private WanderingTraderFeature() {
    }

    public static void check(FeatureContext context) {
        AABB searchBox = context.featureMarker().getBoundingBox().inflate(WTRADER_MARKER_DISTANCE);

        List<ArmorStand> traderMarkers = context.level().getEntitiesOfClass(
                ArmorStand.class,
                searchBox,
                armorStand ->
                        armorStand.isAlive()
                                && armorStand.getTags().contains(WTRADER_MARKER_TAG)
                                && armorStand.distanceToSqr(context.featureMarker()) <= WTRADER_MARKER_DISTANCE_SQUARED
        );

        for (ArmorStand traderMarker : traderMarkers) {
            boolean spawned = WanderingTraderCommandSpawner.spawnSkyPillarTrader(context, traderMarker);

            if (spawned) {
                // This is the Java equivalent of killing the marker after the summon command.
                // It prevents duplicate traders from spawning every scan cycle.
                traderMarker.discard();
            }
        }
    }
}
