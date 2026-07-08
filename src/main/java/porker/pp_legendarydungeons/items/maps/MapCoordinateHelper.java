package porker.pp_legendarydungeons.items.maps;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Optional;

public final class MapCoordinateHelper {
    private MapCoordinateHelper() {
    }

    public static Optional<MapCoordinates> getStoredCoordinates(CompoundTag tag) {
        if (!tag.contains(MapGimmickData.TARGET_X_KEY) || !tag.contains(MapGimmickData.TARGET_Z_KEY)) {
            return Optional.empty();
        }

        return Optional.of(new MapCoordinates(
                tag.getInt(MapGimmickData.TARGET_X_KEY),
                tag.getInt(MapGimmickData.TARGET_Z_KEY)
        ));
    }

    public static Optional<MapCoordinates> getMapCenter(ItemStack stack, ServerLevel level) {
        MapId mapId = stack.get(DataComponents.MAP_ID);

        if (mapId == null) {
            return Optional.empty();
        }

        MapItemSavedData mapData = level.getMapData(mapId);

        if (mapData == null) {
            return Optional.empty();
        }

        return Optional.of(new MapCoordinates(mapData.centerX, mapData.centerZ));
    }
}
