package porker.pp_legendarydungeons.features.dungeon_pokemon;

import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.pokemon.spawn.PokemonSpawnProfileRegistry;

import java.util.Locale;
import java.util.Set;

/**
 * Validation and clamping boundary for dungeon Pokémon datapack profiles.
 */
public final class DungeonPokemonProfileValidator {
    private static final Set<String> TARGET_TYPES = Set.of(
            "player",
            "entity_tag",
            "scoreboard_team",
            "entity_type_tag"
    );
    private static final Set<String> LOOT_MODES = Set.of(
            "default",
            "additional",
            "replace"
    );

    private DungeonPokemonProfileValidator() {
    }

    public static boolean validate(
            ResourceLocation fileId,
            ResourceLocation profileId,
            DungeonPokemonProfileJson profile
    ) {
        if (profile == null) {
            return false;
        }

        ResourceLocation spawnProfile;

        try {
            spawnProfile = ResourceLocation.parse(profile.spawn_profile);
        } catch (Exception exception) {
            return invalid(fileId, profileId, "spawn_profile is not a valid resource ID.");
        }

        if (PokemonSpawnProfileRegistry.get(spawnProfile).isEmpty()) {
            return invalid(
                    fileId,
                    profileId,
                    "spawn_profile " + spawnProfile + " is not loaded."
            );
        }

        DungeonPokemonProfileJson.Aggression aggression =
                profile.aggression == null
                        ? new DungeonPokemonProfileJson.Aggression()
                        : profile.aggression;

        if (aggression.detection_range < 0.0D
                || aggression.chase_range < 0.0D
                || aggression.home_radius < 0.0D) {
            return invalid(fileId, profileId, "aggression ranges cannot be negative.");
        }

        if (aggression.update_interval_ticks < 1) {
            return invalid(fileId, profileId, "update_interval_ticks must be at least 1.");
        }

        if (aggression.return_speed <= 0.0D) {
            return invalid(fileId, profileId, "return_speed must be greater than 0.");
        }

        if (profile.combat_effects != null) {
            for (DungeonPokemonProfileJson.CombatEffect effect : profile.combat_effects) {
                if (effect == null || effect.effect == null || effect.effect.isBlank()) {
                    return invalid(fileId, profileId, "combat effects require an effect ID.");
                }

                try {
                    ResourceLocation.parse(effect.effect);
                } catch (Exception exception) {
                    return invalid(
                            fileId,
                            profileId,
                            "invalid combat effect ID: " + effect.effect
                    );
                }

                if (effect.amplifier < 0
                        || effect.duration_ticks < 1
                        || effect.refresh_interval_ticks < 1) {
                    return invalid(
                            fileId,
                            profileId,
                            "combat effects require amplifier >= 0 and positive durations."
                    );
                }
            }
        }

        if (!validateLoot(fileId, profileId, profile.loot)) {
            return false;
        }

        if (profile.targets != null) {
            for (DungeonPokemonProfileJson.TargetRule target : profile.targets) {
                if (target == null) {
                    return invalid(fileId, profileId, "target entries cannot be null.");
                }

                String type = normalized(target.type);

                if (!TARGET_TYPES.contains(type)) {
                    return invalid(fileId, profileId, "unknown target type: " + type);
                }

                if (!type.equals("player")
                        && (target.value == null || target.value.isBlank())) {
                    return invalid(
                            fileId,
                            profileId,
                            type + " target requires a value."
                    );
                }

                if (type.equals("entity_type_tag")) {
                    try {
                        ResourceLocation.parse(target.value);
                    } catch (Exception exception) {
                        return invalid(
                                fileId,
                                profileId,
                                "invalid entity_type_tag ID: " + target.value
                        );
                    }
                }
            }
        }

        return true;
    }

    private static boolean validateLoot(
            ResourceLocation fileId,
            ResourceLocation profileId,
            DungeonPokemonProfileJson.Loot loot
    ) {
        if (loot == null) {
            return true;
        }

        String mode = normalized(loot.mode);

        if (mode.isBlank()) {
            mode = "default";
        }

        if (!LOOT_MODES.contains(mode)) {
            return invalid(fileId, profileId, "unknown loot mode: " + mode);
        }

        String table = loot.table == null ? "" : loot.table.trim();

        if (table.isBlank() || table.equalsIgnoreCase("default")) {
            return true;
        }

        if (mode.equals("default")) {
            return invalid(
                    fileId,
                    profileId,
                    "an explicit loot table requires mode additional or replace."
            );
        }

        try {
            ResourceLocation.parse(table);
        } catch (Exception exception) {
            return invalid(fileId, profileId, "invalid loot table ID: " + table);
        }

        return true;
    }

    private static String normalized(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean invalid(
            ResourceLocation fileId,
            ResourceLocation profileId,
            String reason
    ) {
        LegendaryDungeons.LOGGER.warn(
                "[Dungeon Pokemon Profiles] Ignoring {} as profile {}: {}",
                fileId,
                profileId,
                reason
        );
        return false;
    }
}
