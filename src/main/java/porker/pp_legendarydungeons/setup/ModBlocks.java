
package porker.pp_legendarydungeons.setup;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;
import porker.pp_legendarydungeons.blocks.dungeon_rules.DungeonRuleParentBlock;
import porker.pp_legendarydungeons.blocks.dungeon_rules.DungeonRuleZoneBlock;

public final class ModBlocks {
    public static final Block DUNGEON_RULE_PARENT = new DungeonRuleParentBlock(
            BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3_600_000.0F)
                    .noCollission()
                    .noOcclusion()
                    .noLootTable()
    );

    public static final Block DUNGEON_RULE_ZONE = new DungeonRuleZoneBlock(
            BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3_600_000.0F)
                    .noCollission()
                    .noOcclusion()
                    .noLootTable()
    );

    private ModBlocks() {
    }

    public static void register() {
        registerBlockWithItem("dungeon_rule_parent", DUNGEON_RULE_PARENT);
        registerBlockWithItem("dungeon_rule_zone", DUNGEON_RULE_ZONE);
    }

    private static void registerBlockWithItem(String path, Block block) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                ProfessorPorkersLegendaryDungeons.MOD_ID,
                path
        );

        Registry.register(BuiltInRegistries.BLOCK, id, block);
        Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new BlockItem(block, new Item.Properties())
        );
    }
}
