package porker.pp_legendarydungeons.dungeon_rules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DungeonRuleZoneBoundsTest {
    private static final BlockPos ANCHOR = new BlockPos(100, 64, 200);

    @Test
    void preservesAuthoredBoundsWhenFacingNorth() {
        DungeonRuleZoneBounds bounds = skyPillarBounds(Direction.NORTH);

        assertEquals(new BlockPos(28, 41, 158), bounds.minPos());
        assertEquals(new BlockPos(139, 175, 269), bounds.maxPos());
    }

    @Test
    void rotatesSkyPillarBoundsClockwiseWhenFacingEast() {
        DungeonRuleZoneBounds bounds = skyPillarBounds(Direction.EAST);

        assertEquals(new BlockPos(31, 41, 128), bounds.minPos());
        assertEquals(new BlockPos(142, 175, 239), bounds.maxPos());
    }

    @Test
    void rotatesSkyPillarBoundsOneHundredEightyDegreesWhenFacingSouth() {
        DungeonRuleZoneBounds bounds = skyPillarBounds(Direction.SOUTH);

        assertEquals(new BlockPos(61, 41, 131), bounds.minPos());
        assertEquals(new BlockPos(172, 175, 242), bounds.maxPos());
    }

    @Test
    void rotatesSkyPillarBoundsCounterClockwiseWhenFacingWest() {
        DungeonRuleZoneBounds bounds = skyPillarBounds(Direction.WEST);

        assertEquals(new BlockPos(58, 41, 161), bounds.minPos());
        assertEquals(new BlockPos(169, 175, 272), bounds.maxPos());
    }

    @Test
    void swapsHorizontalDimensionsForQuarterTurnOfNonSquareBox() {
        DungeonRuleZoneBounds bounds = DungeonRuleZoneBounds.fromLocalBox(
                BlockPos.ZERO,
                Direction.EAST,
                -2,
                -1,
                -4,
                5,
                3,
                7
        );

        assertEquals(new BlockPos(-2, -1, -2), bounds.minPos());
        assertEquals(new BlockPos(4, 1, 2), bounds.maxPos());
    }

    private static DungeonRuleZoneBounds skyPillarBounds(Direction facing) {
        return DungeonRuleZoneBounds.fromLocalBox(
                ANCHOR,
                facing,
                -72,
                -23,
                -42,
                112,
                135,
                112
        );
    }
}
