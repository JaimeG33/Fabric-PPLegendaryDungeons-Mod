package porker.pp_legendarydungeons.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import porker.pp_legendarydungeons.items.maps.MapGimmickData;
import porker.pp_legendarydungeons.items.maps.MapLoreGimmick;

public final class ItemGimmickService {
    private ItemGimmickService() {
    }

    public static boolean process(ItemGimmickContext context) {
        ItemStack stack = context.stack();

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

        if (customData == null || customData.isEmpty()) {
            return false;
        }

        CompoundTag tag = customData.copyTag();

        if (!tag.contains(MapGimmickData.GIMMICK_KEY)) {
            return false;
        }

        String gimmick = tag.getString(MapGimmickData.GIMMICK_KEY);

        if (gimmick == null || gimmick.isBlank()) {
            return false;
        }

        return switch (gimmick) {
            case MapGimmickData.MAP_LORE_GIMMICK -> MapLoreGimmick.process(context, customData);
            default -> false;
        };
    }
}
