package porker.pp_legendarydungeons.setup;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import porker.pp_legendarydungeons.LegendaryDungeons;

/**
 * Shared Architectury registration for the mod's creative-mode tab.
 */
public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(
                    LegendaryDungeons.MOD_ID,
                    Registries.CREATIVE_MODE_TAB
            );

    public static final RegistrySupplier<CreativeModeTab> MAIN =
            TABS.register(
                    "main",
                    () -> CreativeTabRegistry.create(builder -> {
                        builder.title(Component.translatable(
                                "itemGroup.pp_legendarydungeons.main"
                        ));
                        builder.icon(() -> new ItemStack(
                                ModBlocks.CRYSTAL_HEART_ITEM.get()
                        ));
                        builder.displayItems((parameters, output) -> {
                            output.accept(ModBlocks.CRYSTAL_HEART_ITEM.get());
                            output.accept(ModBlocks.CRYSTAL_HEART_B_ITEM.get());
                            output.accept(ModBlocks.CRYSTAL_HEART_C_ITEM.get());
                            output.accept(ModBlocks.DUNGEON_RULE_PARENT_ITEM.get());
                            output.accept(ModBlocks.DUNGEON_RULE_ZONE_ITEM.get());
                        });
                    })
            );

    private static boolean registered = false;

    private ModCreativeTabs() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        TABS.register();
    }
}
