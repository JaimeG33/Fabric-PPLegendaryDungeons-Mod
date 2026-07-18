package porker.pp_legendarydungeons.pokemon.spawn;

import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.List;
import java.util.Set;

/**
 * Validates profile data independently so one malformed datapack entry does not
 * invalidate every other Pokémon profile.
 */
public final class PokemonSpawnProfileValidator {
    private static final Set<String> NATURE_MODES =
            Set.of("default", "fixed", "pool", "weighted_pool");
    private static final Set<String> SHINY_MODES =
            Set.of("default", "never", "always", "one_in");
    private static final Set<String> IV_MODES =
            Set.of("default", "fixed", "range", "weighted_ranges");

    private PokemonSpawnProfileValidator() {
    }

    public static boolean validate(
            ResourceLocation fileId,
            ResourceLocation profileId,
            PokemonSpawnProfileJson profile
    ) {
        if (profile == null) {
            return false;
        }

        if (profile.base_properties == null || profile.base_properties.isBlank()) {
            return invalid(fileId, profileId, "base_properties must not be blank.");
        }

        PokemonSpawnProfileJson.Generation generation =
                profile.generation == null
                        ? new PokemonSpawnProfileJson.Generation()
                        : profile.generation;

        if (!validateLevel(fileId, profileId, generation.level)) {
            return false;
        }

        if (!validateNature(fileId, profileId, generation.nature)) {
            return false;
        }

        if (!validateShiny(fileId, profileId, generation.shiny)) {
            return false;
        }

        return validateIvs(fileId, profileId, generation.ivs);
    }

    private static boolean validateLevel(
            ResourceLocation fileId,
            ResourceLocation profileId,
            PokemonSpawnProfileJson.IntRange level
    ) {
        if (level == null) {
            return true;
        }

        if (level.min < 1 || level.max < 1) {
            return invalid(fileId, profileId, "level min and max must both be at least 1.");
        }

        return true;
    }

    private static boolean validateNature(
            ResourceLocation fileId,
            ResourceLocation profileId,
            PokemonSpawnProfileJson.NatureSettings nature
    ) {
        if (nature == null) {
            return true;
        }

        String mode = normalized(nature.mode, "default");

        if (!NATURE_MODES.contains(mode)) {
            return invalid(fileId, profileId, "unknown nature mode: " + mode);
        }

        if (mode.equals("fixed") && (nature.id == null || nature.id.isBlank())) {
            return invalid(fileId, profileId, "fixed nature mode requires an id.");
        }

        if ((mode.equals("pool") || mode.equals("weighted_pool"))
                && (nature.entries == null || nature.entries.isEmpty())) {
            return invalid(fileId, profileId, mode + " nature mode requires entries.");
        }

        if (nature.entries != null) {
            for (PokemonSpawnProfileJson.WeightedId entry : nature.entries) {
                if (entry == null || entry.id == null || entry.id.isBlank()) {
                    return invalid(fileId, profileId, "nature entries require non-blank ids.");
                }

                if (mode.equals("weighted_pool") && entry.weight <= 0) {
                    return invalid(fileId, profileId, "weighted nature entries require weight > 0.");
                }
            }
        }

        return true;
    }

    private static boolean validateShiny(
            ResourceLocation fileId,
            ResourceLocation profileId,
            PokemonSpawnProfileJson.ShinySettings shiny
    ) {
        if (shiny == null) {
            return true;
        }

        String mode = normalized(shiny.mode, "default");

        if (!SHINY_MODES.contains(mode)) {
            return invalid(fileId, profileId, "unknown shiny mode: " + mode);
        }

        if (mode.equals("one_in") && shiny.denominator < 1) {
            return invalid(fileId, profileId, "one_in shiny denominator must be at least 1.");
        }

        return true;
    }

    private static boolean validateIvs(
            ResourceLocation fileId,
            ResourceLocation profileId,
            PokemonSpawnProfileJson.IvSettings ivs
    ) {
        if (ivs == null) {
            return true;
        }

        String mode = normalized(ivs.mode, "default");

        if (!IV_MODES.contains(mode)) {
            return invalid(fileId, profileId, "unknown IV mode: " + mode);
        }

        if (ivs.minimum_perfect < 0 || ivs.minimum_perfect > 6) {
            return invalid(fileId, profileId, "minimum_perfect must be between 0 and 6.");
        }

        if (mode.equals("fixed")) {
            PokemonSpawnProfileJson.IvValues values = ivs.values;

            if (values == null
                    || values.hp == null
                    || values.attack == null
                    || values.defence == null
                    || values.special_attack == null
                    || values.special_defence == null
                    || values.speed == null) {
                return invalid(fileId, profileId, "fixed IV mode requires all six values.");
            }

            List<Integer> fixedValues = List.of(
                    values.hp,
                    values.attack,
                    values.defence,
                    values.special_attack,
                    values.special_defence,
                    values.speed
            );

            if (fixedValues.stream().anyMatch(value -> value < 0 || value > 31)) {
                return invalid(fileId, profileId, "fixed IV values must be between 0 and 31.");
            }
        }

        if (mode.equals("range")
                && (!validIv(ivs.min) || !validIv(ivs.max))) {
            return invalid(fileId, profileId, "IV range values must be between 0 and 31.");
        }

        if (mode.equals("weighted_ranges")) {
            if (ivs.ranges == null || ivs.ranges.isEmpty()) {
                return invalid(fileId, profileId, "weighted_ranges IV mode requires ranges.");
            }

            for (PokemonSpawnProfileJson.WeightedRange range : ivs.ranges) {
                if (range == null
                        || !validIv(range.min)
                        || !validIv(range.max)
                        || range.weight <= 0) {
                    return invalid(
                            fileId,
                            profileId,
                            "weighted IV ranges require 0-31 bounds and weight > 0."
                    );
                }
            }
        }

        return true;
    }

    private static boolean validIv(int value) {
        return value >= 0 && value <= 31;
    }

    private static String normalized(String value, String fallback) {
        return value == null || value.isBlank()
                ? fallback
                : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static boolean invalid(
            ResourceLocation fileId,
            ResourceLocation profileId,
            String reason
    ) {
        LegendaryDungeons.LOGGER.warn(
                "[Pokemon Spawn Profiles] Ignoring {} as profile {}: {}",
                fileId,
                profileId,
                reason
        );
        return false;
    }
}
