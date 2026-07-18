package porker.pp_legendarydungeons.pokemon.spawn;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Resolves one reusable profile into a concrete Cobblemon property string.
 *
 * Generated properties are appended after base_properties. Cobblemon's parser
 * uses the final occurrence of ordinary properties, so structured JSON values
 * intentionally override conflicting values in the base string.
 */
public final class PokemonGenerationResolver {
    private static final List<String> IV_STATS = List.of(
            "hp",
            "attack",
            "defence",
            "special_attack",
            "special_defence",
            "speed"
    );

    private PokemonGenerationResolver() {
    }

    public static String resolve(PokemonSpawnProfileJson profile, RandomSource random) {
        List<String> properties = new ArrayList<>();

        if (profile.base_properties != null && !profile.base_properties.isBlank()) {
            properties.add(profile.base_properties.trim());
        }

        PokemonSpawnProfileJson.Generation generation =
                profile.generation == null
                        ? new PokemonSpawnProfileJson.Generation()
                        : profile.generation;

        appendLevel(properties, generation.level, random);
        appendNature(properties, generation.nature, random);
        appendShiny(properties, generation.shiny, random);
        appendIvs(properties, generation.ivs, random);

        return String.join(" ", properties).trim();
    }

    private static void appendLevel(
            List<String> properties,
            PokemonSpawnProfileJson.IntRange level,
            RandomSource random
    ) {
        if (level == null) {
            return;
        }

        int min = Math.max(1, Math.min(level.min, level.max));
        int max = Math.max(min, Math.max(level.min, level.max));
        int resolved = min == max
                ? min
                : random.nextIntBetweenInclusive(min, max);

        properties.add("level=" + resolved);
    }

    private static void appendNature(
            List<String> properties,
            PokemonSpawnProfileJson.NatureSettings nature,
            RandomSource random
    ) {
        if (nature == null) {
            return;
        }

        String mode = normalized(nature.mode, "default");

        switch (mode) {
            case "fixed" -> {
                if (nature.id != null && !nature.id.isBlank()) {
                    properties.add("nature=" + nature.id.trim());
                }
            }
            case "pool", "weighted_pool" -> {
                PokemonSpawnProfileJson.WeightedId selected =
                        selectWeightedId(nature.entries, mode.equals("weighted_pool"), random);

                if (selected != null && selected.id != null && !selected.id.isBlank()) {
                    properties.add("nature=" + selected.id.trim());
                }
            }
            default -> {
                // "default" leaves nature unset so Cobblemon chooses normally.
            }
        }
    }

    private static PokemonSpawnProfileJson.WeightedId selectWeightedId(
            List<PokemonSpawnProfileJson.WeightedId> entries,
            boolean weighted,
            RandomSource random
    ) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }

        if (!weighted) {
            return entries.get(random.nextInt(entries.size()));
        }

        int total = entries.stream()
                .filter(entry -> entry != null && entry.weight > 0)
                .mapToInt(entry -> entry.weight)
                .sum();

        if (total <= 0) {
            return null;
        }

        int roll = random.nextInt(total);

        for (PokemonSpawnProfileJson.WeightedId entry : entries) {
            if (entry == null || entry.weight <= 0) {
                continue;
            }

            roll -= entry.weight;

            if (roll < 0) {
                return entry;
            }
        }

        return null;
    }

    private static void appendShiny(
            List<String> properties,
            PokemonSpawnProfileJson.ShinySettings shiny,
            RandomSource random
    ) {
        if (shiny == null) {
            return;
        }

        switch (normalized(shiny.mode, "default")) {
            case "never" -> properties.add("shiny=false");
            case "always" -> properties.add("shiny=true");
            case "one_in" -> {
                int denominator = Math.max(1, shiny.denominator);
                properties.add("shiny=" + (random.nextInt(denominator) == 0));
            }
            default -> {
                // "default" leaves shiny unset so Cobblemon and its events decide.
            }
        }
    }

    private static void appendIvs(
            List<String> properties,
            PokemonSpawnProfileJson.IvSettings ivs,
            RandomSource random
    ) {
        if (ivs == null) {
            return;
        }

        String mode = normalized(ivs.mode, "default");
        int[] resolved = null;

        switch (mode) {
            case "fixed" -> resolved = fixedIvs(ivs.values);
            case "range" -> resolved = rangeIvs(ivs.min, ivs.max, random);
            case "weighted_ranges" -> resolved = weightedRangeIvs(ivs.ranges, random);
            default -> {
                if (ivs.minimum_perfect > 0) {
                    properties.add("min_perfect_ivs=" + clamp(ivs.minimum_perfect, 0, 6));
                }
            }
        }

        if (resolved == null) {
            return;
        }

        guaranteePerfectIvs(resolved, ivs.minimum_perfect, random);

        for (int index = 0; index < IV_STATS.size(); index++) {
            properties.add(IV_STATS.get(index) + "_iv=" + clamp(resolved[index], 0, 31));
        }
    }

    private static int[] fixedIvs(PokemonSpawnProfileJson.IvValues values) {
        if (values == null) {
            return null;
        }

        return new int[] {
                valueOrDefault(values.hp),
                valueOrDefault(values.attack),
                valueOrDefault(values.defence),
                valueOrDefault(values.special_attack),
                valueOrDefault(values.special_defence),
                valueOrDefault(values.speed)
        };
    }

    private static int valueOrDefault(Integer value) {
        return value == null ? 0 : clamp(value, 0, 31);
    }

    private static int[] rangeIvs(int configuredMin, int configuredMax, RandomSource random) {
        int min = clamp(Math.min(configuredMin, configuredMax), 0, 31);
        int max = clamp(Math.max(configuredMin, configuredMax), min, 31);
        int[] values = new int[IV_STATS.size()];

        for (int index = 0; index < values.length; index++) {
            values[index] = min == max
                    ? min
                    : random.nextIntBetweenInclusive(min, max);
        }

        return values;
    }

    private static int[] weightedRangeIvs(
            List<PokemonSpawnProfileJson.WeightedRange> ranges,
            RandomSource random
    ) {
        if (ranges == null || ranges.isEmpty()) {
            return null;
        }

        int[] values = new int[IV_STATS.size()];

        for (int index = 0; index < values.length; index++) {
            PokemonSpawnProfileJson.WeightedRange selected =
                    selectWeightedRange(ranges, random);

            if (selected == null) {
                return null;
            }

            int min = clamp(Math.min(selected.min, selected.max), 0, 31);
            int max = clamp(Math.max(selected.min, selected.max), min, 31);

            values[index] = min == max
                    ? min
                    : random.nextIntBetweenInclusive(min, max);
        }

        return values;
    }

    private static PokemonSpawnProfileJson.WeightedRange selectWeightedRange(
            List<PokemonSpawnProfileJson.WeightedRange> ranges,
            RandomSource random
    ) {
        int total = ranges.stream()
                .filter(range -> range != null && range.weight > 0)
                .mapToInt(range -> range.weight)
                .sum();

        if (total <= 0) {
            return null;
        }

        int roll = random.nextInt(total);

        for (PokemonSpawnProfileJson.WeightedRange range : ranges) {
            if (range == null || range.weight <= 0) {
                continue;
            }

            roll -= range.weight;

            if (roll < 0) {
                return range;
            }
        }

        return null;
    }

    private static void guaranteePerfectIvs(
            int[] values,
            int minimumPerfect,
            RandomSource random
    ) {
        int required = clamp(minimumPerfect, 0, values.length);
        int current = 0;

        for (int value : values) {
            if (value == 31) {
                current++;
            }
        }

        if (current >= required) {
            return;
        }

        List<Integer> candidates = new ArrayList<>();

        for (int index = 0; index < values.length; index++) {
            if (values[index] != 31) {
                candidates.add(index);
            }
        }

        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));

        for (int index : candidates) {
            values[index] = 31;
            current++;

            if (current >= required) {
                break;
            }
        }
    }

    private static String normalized(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
