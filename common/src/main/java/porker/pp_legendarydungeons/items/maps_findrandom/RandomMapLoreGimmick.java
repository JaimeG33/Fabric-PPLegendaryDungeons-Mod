package porker.pp_legendarydungeons.items.maps_findrandom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import porker.pp_legendarydungeons.items.ItemGimmickContext;
import porker.pp_legendarydungeons.items.maps.MapCoordinateHelper;
import porker.pp_legendarydungeons.items.maps.MapCoordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adds post-purchase lore to broad random maps.
 *
 * Required custom data:
 * - pp_gimmick = "random_map_lore"
 * - pp_random_map_group = "dungeons", "misc", or "research_outposts"
 * - pp_coords_revealed = false
 *
 * The handler tries to identify the exact selected structure by comparing the
 * generated map center to each possible target in the random group.
 */
public final class RandomMapLoreGimmick {
    private RandomMapLoreGimmick() {
    }

    public static boolean process(ItemGimmickContext context, CustomData customData) {
        ItemStack stack = context.stack();

        if (!stack.is(Items.FILLED_MAP)) {
            return false;
        }

        CompoundTag tag = customData.copyTag();

        if (tag.getBoolean(RandomMapGimmickData.COORDS_REVEALED_KEY)) {
            return false;
        }

        String groupId = tag.getString(RandomMapGimmickData.GROUP_KEY);

        if (groupId == null || groupId.isBlank()) {
            return false;
        }

        Optional<RandomMapGroup> group = RandomMapRegistry.getGroup(groupId);

        if (group.isEmpty()) {
            return false;
        }

        Optional<MapCoordinates> coordinates = MapCoordinateHelper.getStoredCoordinates(tag)
                .or(() -> MapCoordinateHelper.getMapCenter(stack, context.player().serverLevel()));

        Optional<RandomMapTarget> resolvedTarget = resolveTarget(context, tag, group.get(), coordinates);

        addLore(stack, group.get(), resolvedTarget, coordinates);

        CustomData.update(DataComponents.CUSTOM_DATA, stack, updatedTag -> {
            updatedTag.putBoolean(RandomMapGimmickData.COORDS_REVEALED_KEY, true);

            coordinates.ifPresent(coords -> {
                updatedTag.putInt(RandomMapGimmickData.TARGET_X_KEY, coords.x());
                updatedTag.putInt(RandomMapGimmickData.TARGET_Z_KEY, coords.z());
            });

            if (resolvedTarget.isPresent()) {
                updatedTag.putBoolean(RandomMapGimmickData.RESOLVED_KEY, true);
                updatedTag.putString(RandomMapGimmickData.RESOLVED_TARGET_KEY, resolvedTarget.get().id());
            } else {
                updatedTag.putBoolean(RandomMapGimmickData.RESOLVED_KEY, false);
            }
        });

        return true;
    }

    private static Optional<RandomMapTarget> resolveTarget(
            ItemGimmickContext context,
            CompoundTag tag,
            RandomMapGroup group,
            Optional<MapCoordinates> coordinates
    ) {
        if (tag.contains(RandomMapGimmickData.RESOLVED_TARGET_KEY)) {
            String resolvedTargetId = tag.getString(RandomMapGimmickData.RESOLVED_TARGET_KEY);

            Optional<RandomMapTarget> existingTarget = group.getTarget(resolvedTargetId);

            if (existingTarget.isPresent()) {
                return existingTarget;
            }
        }

        if (coordinates.isEmpty()) {
            return Optional.empty();
        }

        MapCoordinates coords = coordinates.get();
        BlockPos mapCenter = new BlockPos(
                coords.x(),
                context.player().blockPosition().getY(),
                coords.z()
        );

        return RandomMapResolver.resolve(
                        context.player().serverLevel(),
                        mapCenter,
                        group
                )
                .map(RandomMapResolution::target);
    }

    private static void addLore(
            ItemStack stack,
            RandomMapGroup group,
            Optional<RandomMapTarget> resolvedTarget,
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

        lines.add(Component.literal(group.displayName())
                .withStyle(ChatFormatting.GOLD));
        lines.addAll(group.fallbackDescriptionLines());

        if (resolvedTarget.isPresent()) {
            RandomMapTarget target = resolvedTarget.get();

            lines.add(Component.empty());
            lines.add(Component.literal("Identified destination:")
                    .withStyle(ChatFormatting.DARK_AQUA));
            lines.add(Component.literal(target.displayName())
                    .withStyle(ChatFormatting.AQUA));
            lines.addAll(target.descriptionLines());
        } else {
            lines.add(Component.empty());
            lines.add(Component.literal("Specific destination could not be identified.")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

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
