package porker.pp_legendarydungeons.setup;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleParentBlockEntity;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleZoneBlockEntity;
import porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart.CrystalHeartBlockEntity;

/**
 * Shared Architectury registration for block-entity types.
 */
public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(
                    LegendaryDungeons.MOD_ID,
                    Registries.BLOCK_ENTITY_TYPE
            );

    public static final RegistrySupplier<BlockEntityType<DungeonRuleParentBlockEntity>>
            DUNGEON_RULE_PARENT = BLOCK_ENTITY_TYPES.register(
                    "dungeon_rule_parent",
                    () -> BlockEntityType.Builder
                            .of(
                                    DungeonRuleParentBlockEntity::new,
                                    ModBlocks.DUNGEON_RULE_PARENT.get()
                            )
                            .build(null)
            );

    public static final RegistrySupplier<BlockEntityType<DungeonRuleZoneBlockEntity>>
            DUNGEON_RULE_ZONE = BLOCK_ENTITY_TYPES.register(
                    "dungeon_rule_zone",
                    () -> BlockEntityType.Builder
                            .of(
                                    DungeonRuleZoneBlockEntity::new,
                                    ModBlocks.DUNGEON_RULE_ZONE.get()
                            )
                            .build(null)
            );

    public static final RegistrySupplier<BlockEntityType<CrystalHeartBlockEntity>>
            CRYSTAL_HEART = BLOCK_ENTITY_TYPES.register(
                    "crystal_heart",
                    () -> BlockEntityType.Builder
                            .of(
                                    CrystalHeartBlockEntity::new,
                                    ModBlocks.CRYSTAL_HEART.get(),
                                    ModBlocks.CRYSTAL_HEART_B.get(),
                                    ModBlocks.CRYSTAL_HEART_C.get()
                            )
                            .build(null)
            );

    private static boolean registered = false;

    private ModBlockEntities() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        BLOCK_ENTITY_TYPES.register();
    }
}
