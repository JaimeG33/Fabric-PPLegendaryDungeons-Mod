package porker.pp_legendarydungeons.pokemon.spawn;

import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import porker.pp_legendarydungeons.LegendaryDungeons;

/**
 * Loader-neutral registration for Pokémon profile server-data resources.
 */
public final class PokemonProfileReloadRegistrar {
    private static final ResourceLocation LISTENER_ID =
            ResourceLocation.fromNamespaceAndPath(
                    LegendaryDungeons.MOD_ID,
                    "pokemon_profiles"
            );

    private static boolean registered = false;

    private PokemonProfileReloadRegistrar() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ReloadListenerRegistry.register(
                PackType.SERVER_DATA,
                PokemonProfileReloadListener.INSTANCE,
                LISTENER_ID
        );

        LegendaryDungeons.LOGGER.info(
                "[Pokemon Profiles] Registered Architectury server-data reload listener."
        );
    }
}
