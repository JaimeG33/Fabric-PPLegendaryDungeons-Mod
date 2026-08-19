package porker.pp_legendarydungeons.features.dungeon_factions;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Atomic registry of loaded dungeon faction relationship definitions.
 */
public final class DungeonFactionProfileRegistry {
    private static Map<ResourceLocation, DungeonFactionProfileJson> factions = Map.of();

    private DungeonFactionProfileRegistry() {
    }

    public static void replaceAll(Map<ResourceLocation, DungeonFactionProfileJson> newFactions) {
        factions = Collections.unmodifiableMap(new LinkedHashMap<>(newFactions));
    }

    public static Optional<DungeonFactionProfileJson> get(ResourceLocation id) {
        return Optional.ofNullable(factions.get(id));
    }

    public static Map<ResourceLocation, DungeonFactionProfileJson> all() {
        return factions;
    }

    public static int size() {
        return factions.size();
    }
}
