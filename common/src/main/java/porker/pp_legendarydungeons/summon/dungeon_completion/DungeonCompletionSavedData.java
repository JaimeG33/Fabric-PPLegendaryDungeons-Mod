
package porker.pp_legendarydungeons.summon.dungeon_completion;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class DungeonCompletionSavedData extends SavedData {
    private static final String DATA_NAME = "pp_legendarydungeons_completion_watches";
    private static final Factory<DungeonCompletionSavedData> FACTORY =
            new Factory<>(
                    DungeonCompletionSavedData::new,
                    DungeonCompletionSavedData::load,
                    null
            );

    private final Map<UUID, DungeonCompletionWatch> watches = new LinkedHashMap<>();

    public static DungeonCompletionSavedData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(FACTORY, DATA_NAME);
    }

    public Collection<DungeonCompletionWatch> snapshot() {
        return new ArrayList<>(watches.values());
    }

    public void add(DungeonCompletionWatch watch) {
        watches.put(watch.watchId(), watch);
        setDirty();
    }

    public void remove(UUID watchId) {
        if (watches.remove(watchId) != null) {
            setDirty();
        }
    }

    public void markChanged() {
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();

        for (DungeonCompletionWatch watch : watches.values()) {
            list.add(watch.save());
        }

        tag.put("Watches", list);
        return tag;
    }

    private static DungeonCompletionSavedData load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        DungeonCompletionSavedData data = new DungeonCompletionSavedData();
        ListTag list = tag.getList("Watches", Tag.TAG_COMPOUND);

        for (int index = 0; index < list.size(); index++) {
            CompoundTag watchTag = list.getCompound(index);

            if (!watchTag.hasUUID("TrackedEntityId")) {
                continue;
            }

            DungeonCompletionWatch watch = DungeonCompletionWatch.load(watchTag);
            data.watches.put(watch.watchId(), watch);
        }

        return data;
    }
}
