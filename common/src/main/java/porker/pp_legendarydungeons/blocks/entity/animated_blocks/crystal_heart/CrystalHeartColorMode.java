package porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart;

import java.util.Locale;

/**
 * Selects whether a Crystal Heart uses an authored color preset or the neutral
 * texture plus an arbitrary RGB tint.
 */
public enum CrystalHeartColorMode {
    PRESET("preset"),
    CUSTOM("custom");

    private final String id;

    CrystalHeartColorMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static CrystalHeartColorMode fromId(String id) {
        if (id == null || id.isBlank()) {
            return PRESET;
        }

        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (CrystalHeartColorMode mode : values()) {
            if (mode.id.equals(normalized)) {
                return mode;
            }
        }

        return PRESET;
    }
}
