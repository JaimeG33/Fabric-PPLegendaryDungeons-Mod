package porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart;

import java.util.Locale;

/**
 * Survival drop behavior for a mineable Crystal Heart.
 */
public enum CrystalHeartMiningMode {
    /** Always drop the registered Crystal Heart block item. */
    SELF("self"),

    /** Always roll the configured mining loot table. */
    LOOT("loot"),

    /** Silk Touch drops the block item; other pickaxes roll the configured table. */
    SILK_SELF_ELSE_LOOT("silk_self_else_loot");

    private final String id;

    CrystalHeartMiningMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    /** Unknown values use the safest useful enabled-mode default. */
    public static CrystalHeartMiningMode fromId(String id) {
        if (id == null || id.isBlank()) {
            return SILK_SELF_ELSE_LOOT;
        }

        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (CrystalHeartMiningMode mode : values()) {
            if (mode.id.equals(normalized)) {
                return mode;
            }
        }

        return SILK_SELF_ELSE_LOOT;
    }
}
