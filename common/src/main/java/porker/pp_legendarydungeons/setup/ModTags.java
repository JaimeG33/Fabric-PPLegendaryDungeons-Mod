package porker.pp_legendarydungeons.setup;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import porker.pp_legendarydungeons.LegendaryDungeons;

public final class ModTags {
    public static final TagKey<Block> DUNGEON_PC_BLOCKS =
            blockTag("dungeon_pc_blocks");

    public static final TagKey<Block> DUNGEON_HEALER_BLOCKS =
            blockTag("dungeon_healer_blocks");

    /**
     * Blocks that may still be broken when the BLOCK_BREAKING dungeon rule
     * would normally deny the action.
     */
    public static final TagKey<Block> DUNGEON_ALLOWED_BREAKS =
            blockTag("dungeon_allowed_breaks");

    /**
     * Blocks that may still be placed when the BLOCK_PLACEMENT dungeon rule
     * would normally deny the action.
     */
    public static final TagKey<Block> DUNGEON_ALLOWED_PLACES =
            blockTag("dungeon_allowed_places");

    public static final TagKey<Item> DUNGEON_PORTABLE_PC_ITEMS =
            itemTag("dungeon_portable_pc_items");

    public static final TagKey<Item> DUNGEON_PORTABLE_HEALER_ITEMS =
            itemTag("dungeon_portable_healer_items");

    private ModTags() {
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(
                        LegendaryDungeons.MOD_ID,
                        path
                )
        );
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(
                        LegendaryDungeons.MOD_ID,
                        path
                )
        );
    }
}
