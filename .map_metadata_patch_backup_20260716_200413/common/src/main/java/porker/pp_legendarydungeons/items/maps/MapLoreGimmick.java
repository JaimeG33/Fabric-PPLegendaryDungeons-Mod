package porker.pp_legendarydungeons.items.maps;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import porker.pp_legendarydungeons.items.ItemGimmickContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MapLoreGimmick {
    private MapLoreGimmick() {
    }

    public static boolean process(ItemGimmickContext context, CustomData customData) {
        ItemStack stack = context.stack();

        if (!stack.is(Items.FILLED_MAP)) {
            return false;
        }

        CompoundTag tag = customData.copyTag();

        if (tag.getBoolean(MapGimmickData.COORDS_REVEALED_KEY)) {
            return false;
        }

        String targetId = tag.getString(MapGimmickData.MAP_TARGET_KEY);

        if (targetId == null || targetId.isBlank()) {
            return false;
        }

        Optional<MapLoreEntry> loreEntry = MapLoreRegistry.get(targetId);

        if (loreEntry.isEmpty()) {
            return false;
        }

        Optional<MapCoordinates> coordinates = MapCoordinateHelper.getStoredCoordinates(tag)
                .or(() -> MapCoordinateHelper.getMapCenter(stack, context.player().serverLevel()));

        addLore(stack, loreEntry.get(), coordinates);

        CustomData.update(DataComponents.CUSTOM_DATA, stack, updatedTag -> {
            updatedTag.putBoolean(MapGimmickData.COORDS_REVEALED_KEY, true);

            coordinates.ifPresent(coords -> {
                updatedTag.putInt(MapGimmickData.TARGET_X_KEY, coords.x());
                updatedTag.putInt(MapGimmickData.TARGET_Z_KEY, coords.z());
            });
        });

        return true;
    }

    private static void addLore(
            ItemStack stack,
            MapLoreEntry entry,
            Optional<MapCoordinates> coordinates
    ) {
        List<Component> lines = new ArrayList<>();

        ItemLore existingLore = stack.get(DataComponents.LORE);

        if (existingLore != null) {
            lines.addAll(existingLore.lines());
        }

        if (!lines.isEmpty()) {
            lines.add(Component.empty());
        }

        lines.addAll(entry.descriptionLines());

        if (coordinates.isPresent()) {
            MapCoordinates coords = coordinates.get();

            lines.add(Component.empty());
            lines.add(Component.literal("Approximate destination:")
                    .withStyle(ChatFormatting.DARK_AQUA));
            lines.add(Component.literal("X: " + coords.x() + ", Z: " + coords.z())
                    .withStyle(ChatFormatting.AQUA));
        } else {
            lines.add(Component.empty());
            lines.add(Component.literal("Approximate destination: Unknown")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        stack.set(DataComponents.LORE, new ItemLore(lines));
    }
}
