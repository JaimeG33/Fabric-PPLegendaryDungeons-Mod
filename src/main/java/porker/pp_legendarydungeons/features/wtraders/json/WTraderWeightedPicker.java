package porker.pp_legendarydungeons.features.wtraders.json;

import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

/**
 * Generic weighted picker for JSON-defined rolls.
 *
 * Entries with weight <= 0 are ignored.
 */
public final class WTraderWeightedPicker {
    private WTraderWeightedPicker() {
    }

    public static <T> Optional<T> pick(RandomSource random, List<T> entries, ToIntFunction<T> weightFunction) {
        int totalWeight = 0;

        for (T entry : entries) {
            if (entry == null) {
                continue;
            }

            int weight = weightFunction.applyAsInt(entry);

            if (weight > 0) {
                totalWeight += weight;
            }
        }

        if (totalWeight <= 0) {
            return Optional.empty();
        }

        int roll = random.nextInt(totalWeight);

        for (T entry : entries) {
            if (entry == null) {
                continue;
            }

            int weight = weightFunction.applyAsInt(entry);

            if (weight <= 0) {
                continue;
            }

            if (roll < weight) {
                return Optional.of(entry);
            }

            roll -= weight;
        }

        return Optional.empty();
    }
}
