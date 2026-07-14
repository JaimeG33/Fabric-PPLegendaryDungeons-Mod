
package porker.pp_legendarydungeons.dungeon_rules;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

/**
 * Immutable runtime representation of one loaded child rule box.
 */
public record DungeonRuleZone(
        String zoneKey,
        String instanceId,
        ResourceKey<Level> dimension,
        BlockPos anchorPos,
        BlockPos minPos,
        BlockPos maxPos,
        int priority,
        EnumSet<DungeonRule> enforcedRules
) {
    public boolean contains(BlockPos pos) {
        return pos.getX() >= minPos.getX()
                && pos.getX() <= maxPos.getX()
                && pos.getY() >= minPos.getY()
                && pos.getY() <= maxPos.getY()
                && pos.getZ() >= minPos.getZ()
                && pos.getZ() <= maxPos.getZ();
    }

    public boolean enforces(DungeonRule rule) {
        return enforcedRules.contains(rule);
    }

    public int minChunkX() {
        return SectionMath.blockToChunk(minPos.getX());
    }

    public int maxChunkX() {
        return SectionMath.blockToChunk(maxPos.getX());
    }

    public int minChunkZ() {
        return SectionMath.blockToChunk(minPos.getZ());
    }

    public int maxChunkZ() {
        return SectionMath.blockToChunk(maxPos.getZ());
    }

    /**
     * Avoids depending on internal SectionPos helper names across mappings.
     */
    private static final class SectionMath {
        private SectionMath() {
        }

        private static int blockToChunk(int coordinate) {
            return Math.floorDiv(coordinate, 16);
        }
    }
}
