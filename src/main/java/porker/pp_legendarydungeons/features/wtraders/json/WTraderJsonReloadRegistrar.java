package porker.pp_legendarydungeons.features.wtraders.json;

import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import porker.pp_legendarydungeons.LegendaryDungeons;

/**
 * Registers the server-data reload listener for custom wandering-trader JSON
 * through Architectury's loader-neutral reload-listener registry.
 */
public final class WTraderJsonReloadRegistrar {
    private static final ResourceLocation LISTENER_ID =
            ResourceLocation.fromNamespaceAndPath(
                    LegendaryDungeons.MOD_ID,
                    "wtrader_json"
            );

    private static boolean registered = false;

    private WTraderJsonReloadRegistrar() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ReloadListenerRegistry.register(
                PackType.SERVER_DATA,
                WTraderJsonReloadListener.INSTANCE,
                LISTENER_ID
        );

        LegendaryDungeons.LOGGER.info(
                "[WTrader JSON] Registered Architectury server-data reload listener."
        );
    }
}
