package porker.pp_legendarydungeons.features.custom_mobs.mob_registry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for pre-defined custom dungeon mobs.
 *
 * Add future mobs here after creating another definition class in this folder.
 */
public final class CustomMobRegistry {
    private static final Map<String, CustomMobDefinition> DEFINITIONS = new HashMap<>();

    static {
        register(BoggedSentryDefinition.create());
    }

    private CustomMobRegistry() {
    }

    public static Optional<CustomMobDefinition> get(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(DEFINITIONS.get(normalizeId(id)));
    }

    private static void register(CustomMobDefinition definition) {
        DEFINITIONS.put(normalizeId(definition.id()), definition);
    }

    private static String normalizeId(String id) {
        return id.trim().toLowerCase(Locale.ROOT);
    }
}
