package porker.pp_legendarydungeons.dungeon_rules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Converts one dungeon-rule box from controller-local coordinates into an
 * axis-aligned world-space box.
 *
 * <p>The authored orientation is {@link Direction#NORTH}. A structure template
 * rotation changes the controller block's facing, and the same rotation is then
 * applied to every horizontal corner of the local box.</p>
 */
public record DungeonRuleZoneBounds(
        BlockPos minPos,
        BlockPos maxPos
) {
    public static DungeonRuleZoneBounds fromLocalBox(
            BlockPos anchorPos,
            Direction facing,
            int offsetX,
            int offsetY,
            int offsetZ,
            int sizeX,
            int sizeY,
            int sizeZ
    ) {
        int safeSizeX = Math.max(1, sizeX);
        int safeSizeY = Math.max(1, sizeY);
        int safeSizeZ = Math.max(1, sizeZ);

        int localMinX = offsetX;
        int localMaxX = offsetX + safeSizeX - 1;
        int localMinZ = offsetZ;
        int localMaxZ = offsetZ + safeSizeZ - 1;

        int rotatedMinX = Integer.MAX_VALUE;
        int rotatedMaxX = Integer.MIN_VALUE;
        int rotatedMinZ = Integer.MAX_VALUE;
        int rotatedMaxZ = Integer.MIN_VALUE;

        int[] xCorners = {localMinX, localMaxX};
        int[] zCorners = {localMinZ, localMaxZ};

        for (int x : xCorners) {
            for (int z : zCorners) {
                RelativeXZ rotated = rotateHorizontal(x, z, facing);
                rotatedMinX = Math.min(rotatedMinX, rotated.x());
                rotatedMaxX = Math.max(rotatedMaxX, rotated.x());
                rotatedMinZ = Math.min(rotatedMinZ, rotated.z());
                rotatedMaxZ = Math.max(rotatedMaxZ, rotated.z());
            }
        }

        BlockPos minPos = anchorPos.offset(
                rotatedMinX,
                offsetY,
                rotatedMinZ
        );

        BlockPos maxPos = anchorPos.offset(
                rotatedMaxX,
                offsetY + safeSizeY - 1,
                rotatedMaxZ
        );

        return new DungeonRuleZoneBounds(minPos, maxPos);
    }

    private static RelativeXZ rotateHorizontal(
            int x,
            int z,
            Direction facing
    ) {
        Direction safeFacing = facing == null ? Direction.NORTH : facing;

        return switch (safeFacing) {
            // Clockwise 90 degrees: north becomes east.
            case EAST -> new RelativeXZ(-z, x);

            // Clockwise 180 degrees.
            case SOUTH -> new RelativeXZ(-x, -z);

            // Counter-clockwise 90 degrees: north becomes west.
            case WEST -> new RelativeXZ(z, -x);

            // NORTH is the authored orientation. Vertical values are treated as
            // NORTH defensively, although the block property only permits
            // horizontal directions.
            default -> new RelativeXZ(x, z);
        };
    }

    private record RelativeXZ(int x, int z) {
    }
}
