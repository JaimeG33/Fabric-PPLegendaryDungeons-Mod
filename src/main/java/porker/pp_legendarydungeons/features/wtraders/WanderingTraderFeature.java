package porker.pp_legendarydungeons.features.wtraders;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.features.FeatureContext;

import java.util.List;
import java.util.Set;

/**
 * Handles structure-spawned wandering trader markers.
 *
 * Supported marker tags:
 * - pp_wtraders
 *   Uses the normal rolled behavior: vanilla/custom_no_map/custom_map.
 *
 * - pp_wtrader_profiletype
 *   Requires one additional marker tag:
 *   - custom_no_map
 *   - custom_map
 *   Rolls a random JSON profile of that type.
 *   If the type tag is missing or ambiguous, a vanilla wandering trader is spawned.
 *
 * - pp_wtrader_specificprofile
 *   Requires one additional marker tag:
 *   - custom_no_map
 *   - custom_map
 *   Also requires the armor stand CustomName to be a loaded JSON profile id, such as:
 *   pp_legendarydungeons:wtraders_nomap/random_cheap
 *   If the type tag or profile id is missing/invalid, a vanilla wandering trader is spawned.
 */
public final class WanderingTraderFeature {
    public static final String WTRADER_MARKER_TAG = "pp_wtraders";
    public static final String WTRADER_PROFILE_TYPE_MARKER_TAG = "pp_wtrader_profiletype";
    public static final String WTRADER_SPECIFIC_PROFILE_MARKER_TAG = "pp_wtrader_specificprofile";

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
                                && isWTraderMarker(armorStand)
                                && armorStand.distanceToSqr(context.featureMarker()) <= WTRADER_MARKER_DISTANCE_SQUARED
        );

        for (ArmorStand traderMarker : traderMarkers) {
            boolean spawned = spawnForMarkerTags(context, traderMarker);

            if (spawned) {
                traderMarker.discard();
            }
        }
    }

    private static boolean isWTraderMarker(ArmorStand armorStand) {
        Set<String> tags = armorStand.getTags();

        return tags.contains(WTRADER_MARKER_TAG)
                || tags.contains(WTRADER_PROFILE_TYPE_MARKER_TAG)
                || tags.contains(WTRADER_SPECIFIC_PROFILE_MARKER_TAG);
    }

    private static boolean spawnForMarkerTags(FeatureContext context, ArmorStand traderMarker) {
        Set<String> tags = traderMarker.getTags();

        if (tags.contains(WTRADER_SPECIFIC_PROFILE_MARKER_TAG)) {
            return WanderingTraderCommandSpawner.spawnSpecificJsonProfileTrader(context, traderMarker);
        }

        if (tags.contains(WTRADER_PROFILE_TYPE_MARKER_TAG)) {
            return WanderingTraderCommandSpawner.spawnProfileTypeTrader(context, traderMarker);
        }

        return WanderingTraderCommandSpawner.spawnRolledTrader(context, traderMarker);
    }
}
