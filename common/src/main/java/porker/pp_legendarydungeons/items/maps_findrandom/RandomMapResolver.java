package porker.pp_legendarydungeons.items.maps_findrandom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.Optional;

/**
 * Attempts to identify which specific structure a broad random map selected.
 *
 * <p>The generated explorer map already has a saved map center. We compare that
 * map center against the nearest structure for each possible target in the
 * random group.</p>
 */
public final class RandomMapResolver {
    /** Radius is measured in chunks, not blocks. */
    private static final int BASE_LOCATE_RADIUS_CHUNKS = 32;

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
            RandomMapGroup group,
            int attemptNumber
    ) {
        int locateRadiusChunks = locateRadiusForAttempt(attemptNumber);
        RandomMapResolution bestResolution = null;

        for (RandomMapTarget target : group.targets()) {
            try {
                BlockPos nearest = level.findNearestMapStructure(
                        target.structureTag(),
                        mapCenter,
                        locateRadiusChunks,
                        false
                );

                if (nearest == null) {
                    LegendaryDungeons.LOGGER.debug(
                            "[Random Map] Attempt {} found no structure for target {} using tag {} within {} chunks.",
                            attemptNumber,
                            target.id(),
                            target.structureTagId(),
                            locateRadiusChunks
                    );
                    continue;
                }

                double distanceSquared = nearest.distSqr(mapCenter);

                if (distanceSquared > MAX_MATCH_DISTANCE_SQUARED) {
                    LegendaryDungeons.LOGGER.debug(
                            "[Random Map] Attempt {} found target {} at {}, but it was {} blocks from map center {} and exceeded the {} block match limit.",
                            attemptNumber,
                            target.id(),
                            nearest,
                            Math.sqrt(distanceSquared),
                            mapCenter,
                            MAX_MATCH_DISTANCE_BLOCKS
                    );
                    continue;
                }

                if (bestResolution == null
                        || distanceSquared < bestResolution.distanceSquared()) {
                    bestResolution = new RandomMapResolution(
                            target,
                            nearest,
                            distanceSquared
                    );
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

    /**
     * The first attempt uses the normal radius. The second and third attempts
     * use one saturated doubling of that radius; the doubled value is never
     * doubled again.
     */
    public static int locateRadiusForAttempt(int attemptNumber) {
        if (attemptNumber <= 1) {
            return BASE_LOCATE_RADIUS_CHUNKS;
        }

        return saturatingDouble(BASE_LOCATE_RADIUS_CHUNKS);
    }

    private static int saturatingDouble(int value) {
        if (value > Integer.MAX_VALUE / 2) {
            return Integer.MAX_VALUE;
        }

        return value * 2;
    }
}
