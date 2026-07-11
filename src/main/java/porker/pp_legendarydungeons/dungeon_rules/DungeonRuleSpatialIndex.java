
package porker.pp_legendarydungeons.dungeon_rules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chunk-indexed lookup for loaded dungeon rule zones.
 *
 * No block/entity scan is needed when an action occurs. The action position selects
 * one chunk bucket, then only the few zones touching that chunk are checked.
 */
public final class DungeonRuleSpatialIndex {
    private final Map<Long, List<DungeonRuleZone>> zonesByChunk = new HashMap<>();

    public void add(DungeonRuleZone zone) {
        for (int chunkX = zone.minChunkX(); chunkX <= zone.maxChunkX(); chunkX++) {
            for (int chunkZ = zone.minChunkZ(); chunkZ <= zone.maxChunkZ(); chunkZ++) {
                zonesByChunk
                        .computeIfAbsent(ChunkPos.asLong(chunkX, chunkZ), ignored -> new ArrayList<>())
                        .add(zone);
            }
        }
    }

    public void remove(DungeonRuleZone zone) {
        for (int chunkX = zone.minChunkX(); chunkX <= zone.maxChunkX(); chunkX++) {
            for (int chunkZ = zone.minChunkZ(); chunkZ <= zone.maxChunkZ(); chunkZ++) {
                long key = ChunkPos.asLong(chunkX, chunkZ);
                List<DungeonRuleZone> bucket = zonesByChunk.get(key);

                if (bucket == null) {
                    continue;
                }

                bucket.removeIf(existing -> existing.zoneKey().equals(zone.zoneKey()));

                if (bucket.isEmpty()) {
                    zonesByChunk.remove(key);
                }
            }
        }
    }

    public List<DungeonRuleZone> zonesAt(BlockPos pos) {
        long key = ChunkPos.asLong(Math.floorDiv(pos.getX(), 16), Math.floorDiv(pos.getZ(), 16));
        List<DungeonRuleZone> bucket = zonesByChunk.get(key);
        return bucket == null ? List.of() : bucket;
    }

    public void clear() {
        zonesByChunk.clear();
    }
}
