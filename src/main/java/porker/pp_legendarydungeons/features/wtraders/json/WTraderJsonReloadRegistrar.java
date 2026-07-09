package porker.pp_legendarydungeons.features.wtraders.json;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

/**
 * Registers the server-data reload listener for custom wandering-trader JSON.
 *
 * Requires the Fabric API resource loader module:
 * fabric-resource-loader-v0
 */
public final class WTraderJsonReloadRegistrar {
    private static boolean registered = false;

    private WTraderJsonReloadRegistrar() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(
                        ProfessorPorkersLegendaryDungeons.MOD_ID,
                        "wtrader_json"
                );
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                WTraderJsonReloadListener.reload(resourceManager);
            }
        });

        ProfessorPorkersLegendaryDungeons.LOGGER.info(
                "[WTrader JSON] Registered server data reload listener."
        );
    }
}
