
package porker.pp_legendarydungeons.dungeon_rules;

/**
 * Every independently configurable dungeon restriction.
 *
 * A value of true after preset/parent/zone resolution means the rule is enforced.
 */
public enum DungeonRule {
    BLOCK_BREAKING("Breaking"),
    BLOCK_PLACEMENT("Placing"),
    MINING_FATIGUE("Mining Fatigue"),
    EXPLOSIONS("Explosions"),
    BED_USE("Beds"),
    PC_USE("PC Blocks"),
    HEALER_USE("Healer Blocks"),
    PORTABLE_PC_USE("Portable PC"),
    PORTABLE_HEALER_USE("Portable Healer");

    private final String displayName;

    DungeonRule(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
