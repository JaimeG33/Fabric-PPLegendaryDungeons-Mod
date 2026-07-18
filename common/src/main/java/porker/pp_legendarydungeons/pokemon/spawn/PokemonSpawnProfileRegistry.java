package porker.pp_legendarydungeons.pokemon.spawn;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Atomic runtime registry for datapack-backed Pokémon spawn profiles.
 */
public final class PokemonSpawnProfileRegistry {
    private static Map<ResourceLocation, PokemonSpawnProfileJson> profiles = Map.of();

    private PokemonSpawnProfileRegistry() {
    }

    public static void replaceAll(Map<ResourceLocation, PokemonSpawnProfileJson> newProfiles) {
        profiles = Collections.unmodifiableMap(new LinkedHashMap<>(newProfiles));
    }

    public static Optional<PokemonSpawnProfileJson> get(ResourceLocation id) {
        return Optional.ofNullable(profiles.get(id));
    }

    public static Map<ResourceLocation, PokemonSpawnProfileJson> all() {
        return profiles;
    }

    public static int size() {
        return profiles.size();
    }
}
