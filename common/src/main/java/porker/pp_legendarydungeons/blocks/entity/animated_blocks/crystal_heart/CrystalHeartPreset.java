package porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart;

import java.util.Locale;

/**
 * Named Crystal Heart silhouettes that can be selected through block-entity NBT.
 *
 * <p>Geometry stays normalized so {@code CrystalWidth} and {@code CrystalHeight}
 * continue to resize every preset in the same way. Texture paths are stored as
 * plain strings here so the server-safe block entity does not depend on client
 * rendering classes.</p>
 */
public enum CrystalHeartPreset {
    /** Existing Crystal Heart silhouette. This is the compatibility fallback. */
    BASE(
            "base",
            0.46F,
            "crystal_heart",
            "crystal_heart_a"
    ),

    /** Short upper cap with a much longer lower point. */
    LONG_POINT(
            "long_point",
            0.72F,
            "crystal_heart",
            "crystal_heart_a"
    ),

    /** Tall crystal/spire profile intended to be paired with a narrower width. */
    SPIRE(
            "spire",
            0.60F,
            "crystal_heart",
            "crystal_heart_a"
    );

    private final String id;
    private final float waistY;
    private final String textureFolder;
    private final String textureBaseName;

    CrystalHeartPreset(
            String id,
            float waistY,
            String textureFolder,
            String textureBaseName
    ) {
        this.id = id;
        this.waistY = waistY;
        this.textureFolder = textureFolder;
        this.textureBaseName = textureBaseName;
    }

    public String id() {
        return id;
    }

    public float waistY() {
        return waistY;
    }

    public String textureFolder() {
        return textureFolder;
    }

    public String textureBaseName() {
        return textureBaseName;
    }

    /**
     * Invalid, missing, or future-unknown values safely fall back to the original
     * shape so old structures and edited NBT never stop rendering.
     */
    public static CrystalHeartPreset fromId(String id) {
        if (id == null || id.isBlank()) {
            return BASE;
        }

        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (CrystalHeartPreset preset : values()) {
            if (preset.id.equals(normalized)) {
                return preset;
            }
        }

        return BASE;
    }
}
