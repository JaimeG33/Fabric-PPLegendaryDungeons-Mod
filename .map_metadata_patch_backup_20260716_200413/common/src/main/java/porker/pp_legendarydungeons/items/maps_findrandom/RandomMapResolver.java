package porker.pp_legendarydungeons.items.maps_findrandom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.Optional;

/**
 * Attempts to identify which specific structure a broad random map selected.
 *
 * The generated explorer map already has a saved map center. We compare that map center
 * against the nearest structure for each possible target in the random group.
 */
public final class RandomMapResolver {
    /**
     * Radius is in chunks, not blocks.
     *
     * This is only run once per random map when the player owns it, so a moderate
     * radius is acceptable.
     */
    private static final int LOCATE_RADIUS_CHUNKS = 32;

    /**
     * Maximum allowed distance between the generated map center and a candidate
     * structure location for us to consider it the selected destination.
     */
    private static final double MAX_MATCH_DISTANCE_BLOCKS = 512.0D;
    private static final double MAX_MATCH_DISTANCE_SQUARED =
            MAX_MATCH_DISTANCE_BLOCKS * MAX_MATCH_DISTANCE_BLOCKS;

    private RandomMapResolver() {
    }

    public static Optional<RandomMapResolution> resolve(
            ServerLevel level,
            BlockPos mapCenter,
            RandomMapGroup group
    ) {
        RandomMapResolution bestResolution = null;

        for (RandomMapTarget target : group.targets()) {
            try {
                BlockPos nearest = level.findNearestMapStructure(
                        target.structureTag(),
                        mapCenter,
                        LOCATE_RADIUS_CHUNKS,
                        false
                );

                if (nearest == null) {
                    continue;
                }

                double distanceSquared = nearest.distSqr(mapCenter);

                if (distanceSquared > MAX_MATCH_DISTANCE_SQUARED) {
                    continue;
                }

                if (bestResolution == null || distanceSquared < bestResolution.distanceSquared()) {
                    bestResolution = new RandomMapResolution(target, nearest, distanceSquared);
                }
            } catch (Exception exception) {
                LegendaryDungeons.LOGGER.warn(
                        "Could not check random map target {} using structure tag {}.",
                        target.id(),
                        target.structureTagId(),
                        exception
                );
            }
        }

        return Optional.ofNullable(bestResolution);
    }
}
