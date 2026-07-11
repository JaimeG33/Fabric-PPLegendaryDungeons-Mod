
package porker.pp_legendarydungeons.setup;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleParentBlockEntity;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleZoneBlockEntity;

public final class ModBlockEntities {
    public static BlockEntityType<DungeonRuleParentBlockEntity> DUNGEON_RULE_PARENT;
    public static BlockEntityType<DungeonRuleZoneBlockEntity> DUNGEON_RULE_ZONE;

    private ModBlockEntities() {
    }

    public static void register() {
        DUNGEON_RULE_PARENT = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id("dungeon_rule_parent"),
                BlockEntityType.Builder
                        .of(DungeonRuleParentBlockEntity::new, ModBlocks.DUNGEON_RULE_PARENT)
                        .build(null)
        );

        DUNGEON_RULE_ZONE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id("dungeon_rule_zone"),
                BlockEntityType.Builder
                        .of(DungeonRuleZoneBlockEntity::new, ModBlocks.DUNGEON_RULE_ZONE)
                        .build(null)
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                ProfessorPorkersLegendaryDungeons.MOD_ID,
                path
        );
    }
}
