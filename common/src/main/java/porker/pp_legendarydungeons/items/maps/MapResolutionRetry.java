package porker.pp_legendarydungeons.items.maps;

import net.minecraft.nbt.CompoundTag;

/**
 * Persistent retry state shared by the dedicated-map and random-map gimmicks.
 */
public final class MapResolutionRetry {
    public static final String ATTEMPTS_KEY = "pp_map_resolution_attempts";
    public static final String RETRY_AT_KEY = "pp_map_resolution_retry_at";

    public static final int MAX_ATTEMPTS = 3;
    public static final long RETRY_DELAY_TICKS = 120L;

    private MapResolutionRetry() {
    }

    public static boolean isWaiting(CompoundTag tag, long gameTime) {
        return tag.contains(RETRY_AT_KEY)
                && gameTime < tag.getLong(RETRY_AT_KEY);
    }

    public static int nextAttempt(CompoundTag tag) {
        int previous = Math.max(0, tag.getInt(ATTEMPTS_KEY));

        if (previous >= MAX_ATTEMPTS) {
            return MAX_ATTEMPTS;
        }

        return previous + 1;
    }

    public static boolean hasAnotherAttempt(int currentAttempt) {
        return currentAttempt < MAX_ATTEMPTS;
    }

    public static void markPending(
            CompoundTag tag,
            int currentAttempt,
            long gameTime
    ) {
        tag.putInt(ATTEMPTS_KEY, currentAttempt);
        tag.putLong(RETRY_AT_KEY, saturatingAdd(gameTime, RETRY_DELAY_TICKS));
    }

    public static void clear(CompoundTag tag) {
        tag.remove(ATTEMPTS_KEY);
        tag.remove(RETRY_AT_KEY);
    }

    private static long saturatingAdd(long value, long increment) {
        if (increment > 0L && value > Long.MAX_VALUE - increment) {
            return Long.MAX_VALUE;
        }

        if (increment < 0L && value < Long.MIN_VALUE - increment) {
            return Long.MIN_VALUE;
        }

        return value + increment;
    }
}
