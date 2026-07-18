package porker.pp_legendarydungeons.features.dungeon_pokemon;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Atomic registry of loaded dungeon Pokémon behavior profiles.
 */
public final class DungeonPokemonProfileRegistry {
    private static Map<ResourceLocation, DungeonPokemonProfileJson> profiles = Map.of();

    private DungeonPokemonProfileRegistry() {
    }

    public static void replaceAll(Map<ResourceLocation, DungeonPokemonProfileJson> newProfiles) {
        profiles = Collections.unmodifiableMap(new LinkedHashMap<>(newProfiles));
    }

    public static Optional<DungeonPokemonProfileJson> get(ResourceLocation id) {
        return Optional.ofNullable(profiles.get(id));
    }

    public static Map<ResourceLocation, DungeonPokemonProfileJson> all() {
        return profiles;
    }

    public static int size() {
        return profiles.size();
    }
}
