package porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart;

import java.util.Locale;

/**
 * Authored Crystal Heart color/texture presets.
 *
 * <p>Only the original green artwork exists today. Keeping this separate from
 * {@link CrystalHeartPreset} lets geometry presets and color presets evolve
 * independently.</p>
 */
public enum CrystalHeartColorPreset {
    GREEN("green");

    private final String id;

    CrystalHeartColorPreset(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static CrystalHeartColorPreset fromId(String id) {
        if (id == null || id.isBlank()) {
            return GREEN;
        }

        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (CrystalHeartColorPreset preset : values()) {
            if (preset.id.equals(normalized)) {
                return preset;
            }
        }

        return GREEN;
    }
}
