package porker.pp_legendarydungeons.items.maps;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import porker.pp_legendarydungeons.maps.LegendaryMapTarget;

public final class MapGimmickData {
    public static final String GIMMICK_KEY = "pp_gimmick";
    public static final String MAP_LORE_GIMMICK = "map_lore";

    public static final String MAP_TARGET_KEY = "pp_map_target";
    public static final String COORDS_REVEALED_KEY = "pp_coords_revealed";

    public static final String TARGET_X_KEY = "pp_target_x";
    public static final String TARGET_Z_KEY = "pp_target_z";

    private MapGimmickData() {
    }

    public static void markMapForLore(ItemStack stack, LegendaryMapTarget target, ServerLevel level) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(GIMMICK_KEY, MAP_LORE_GIMMICK);
            tag.putString(MAP_TARGET_KEY, target.id());
            tag.putBoolean(COORDS_REVEALED_KEY, false);

            MapCoordinateHelper.getMapCenter(stack, level).ifPresent(coords -> {
                tag.putInt(TARGET_X_KEY, coords.x());
                tag.putInt(TARGET_Z_KEY, coords.z());
            });
        });
    }
}
