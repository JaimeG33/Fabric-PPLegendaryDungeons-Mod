package porker.pp_legendarydungeons.setup;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.worldgen.structure.FixedJigsawStructure;

/**
 * Shared Architectury registration for custom world-generation structure types.
 */
public final class ModStructureTypes {
    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(
                    LegendaryDungeons.MOD_ID,
                    Registries.STRUCTURE_TYPE
            );

    public static final RegistrySupplier<StructureType<FixedJigsawStructure>>
            FIXED_JIGSAW = STRUCTURE_TYPES.register(
                    "fixed_jigsaw",
                    () -> () -> FixedJigsawStructure.CODEC
            );

    private static boolean registered = false;

    private ModStructureTypes() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        STRUCTURE_TYPES.register();
    }
}
