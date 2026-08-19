package porker.pp_legendarydungeons.features.dungeon_factions;

import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import porker.pp_legendarydungeons.LegendaryDungeons;

/**
 * Loader-neutral server-data registration for dungeon faction definitions.
 */
public final class DungeonFactionReloadRegistrar {
    private static final ResourceLocation LISTENER_ID =
            ResourceLocation.fromNamespaceAndPath(
                    LegendaryDungeons.MOD_ID,
                    "dungeon_factions"
            );

    private static boolean registered = false;

    private DungeonFactionReloadRegistrar() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ReloadListenerRegistry.register(
                PackType.SERVER_DATA,
                DungeonFactionReloadListener.INSTANCE,
                LISTENER_ID
        );

        LegendaryDungeons.LOGGER.info(
                "[Dungeon Factions] Registered Architectury server-data reload listener."
        );
    }
}
