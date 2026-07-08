package porker.pp_legendarydungeons.items.maps_findrandom;

import net.minecraft.core.BlockPos;

/**
 * Result of matching a random map's center to one of the possible structures.
 */
public record RandomMapResolution(
        RandomMapTarget target,
        BlockPos locatedStructurePos,
        double distanceSquared
) {
}
