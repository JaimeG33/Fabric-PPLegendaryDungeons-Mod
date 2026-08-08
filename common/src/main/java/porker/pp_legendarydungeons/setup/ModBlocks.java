package porker.pp_legendarydungeons.setup;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.blocks.animated_blocks.crystal_heart.CrystalHeartBlock;
import porker.pp_legendarydungeons.blocks.dungeon_rules.DungeonRuleParentBlock;
import porker.pp_legendarydungeons.blocks.dungeon_rules.DungeonRuleZoneBlock;

/**
 * Shared Architectury registration for the dungeon controller blocks, animated
 * world objects, and their corresponding block items.
 *
 * <p>The public fields are registry suppliers rather than eagerly constructed
 * instances. Call sites must use {@link RegistrySupplier#get()} only after the
 * registries have been initialized.</p>
 */
public final class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(LegendaryDungeons.MOD_ID, Registries.BLOCK);

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(LegendaryDungeons.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<DungeonRuleParentBlock> DUNGEON_RULE_PARENT =
            BLOCKS.register(
                    "dungeon_rule_parent",
                    () -> new DungeonRuleParentBlock(controllerProperties())
            );

    public static final RegistrySupplier<DungeonRuleZoneBlock> DUNGEON_RULE_ZONE =
            BLOCKS.register(
                    "dungeon_rule_zone",
                    () -> new DungeonRuleZoneBlock(controllerProperties())
            );

    public static final RegistrySupplier<CrystalHeartBlock> CRYSTAL_HEART =
            BLOCKS.register(
                    "crystal_heart",
                    () -> new CrystalHeartBlock(animatedDecorationProperties())
            );

    public static final RegistrySupplier<CrystalHeartBlock> CRYSTAL_HEART_B =
            BLOCKS.register(
                    "crystal_heart_b",
                    () -> new CrystalHeartBlock(animatedDecorationProperties())
            );

    public static final RegistrySupplier<CrystalHeartBlock> CRYSTAL_HEART_C =
            BLOCKS.register(
                    "crystal_heart_c",
                    () -> new CrystalHeartBlock(animatedDecorationProperties())
            );

    public static final RegistrySupplier<BlockItem> DUNGEON_RULE_PARENT_ITEM =
            ITEMS.register(
                    "dungeon_rule_parent",
                    () -> new BlockItem(
                            DUNGEON_RULE_PARENT.get(),
                            new Item.Properties()
                    )
            );

    public static final RegistrySupplier<BlockItem> DUNGEON_RULE_ZONE_ITEM =
            ITEMS.register(
                    "dungeon_rule_zone",
                    () -> new BlockItem(
                            DUNGEON_RULE_ZONE.get(),
                            new Item.Properties()
                    )
            );

    public static final RegistrySupplier<BlockItem> CRYSTAL_HEART_ITEM =
            ITEMS.register(
                    "crystal_heart",
                    () -> new BlockItem(
                            CRYSTAL_HEART.get(),
                            new Item.Properties()
                    )
            );

    public static final RegistrySupplier<BlockItem> CRYSTAL_HEART_B_ITEM =
            ITEMS.register(
                    "crystal_heart_b",
                    () -> new BlockItem(
                            CRYSTAL_HEART_B.get(),
                            new Item.Properties()
                    )
            );

    public static final RegistrySupplier<BlockItem> CRYSTAL_HEART_C_ITEM =
            ITEMS.register(
                    "crystal_heart_c",
                    () -> new BlockItem(
                            CRYSTAL_HEART_C.get(),
                            new Item.Properties()
                    )
            );

    private static boolean registered = false;

    private ModBlocks() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        /*
         * Blocks register before block items so the item suppliers can safely
         * resolve their corresponding block on both supported loaders.
         */
        BLOCKS.register();
        ITEMS.register();
    }

    private static BlockBehaviour.Properties controllerProperties() {
        return BlockBehaviour.Properties.of()
                .strength(-1.0F, 3_600_000.0F)
                .noCollission()
                .noOcclusion()
                .noLootTable();
    }

    private static BlockBehaviour.Properties animatedDecorationProperties() {
        return BlockBehaviour.Properties.of()
                .strength(-1.0F, 3_600_000.0F)
                .noCollission()
                .noOcclusion()
                .noLootTable();
    }
}
