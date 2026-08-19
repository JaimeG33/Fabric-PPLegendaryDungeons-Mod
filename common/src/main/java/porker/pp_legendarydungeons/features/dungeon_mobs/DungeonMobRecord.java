package porker.pp_legendarydungeons.features.dungeon_mobs;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

/** Runtime index entry for one currently loaded managed vanilla mob. */
public record DungeonMobRecord(
        UUID entityUuid,
        ResourceKey<Level> dimension,
        DungeonMobPersistentData data
) {
}
