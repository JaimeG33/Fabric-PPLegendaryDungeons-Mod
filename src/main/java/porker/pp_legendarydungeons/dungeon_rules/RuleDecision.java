
package porker.pp_legendarydungeons.dungeon_rules;

/**
 * Tri-state rule value used by parent and child overrides.
 *
 * INHERIT = use the next value lower in the hierarchy.
 * ALLOW   = explicitly do not enforce the restriction.
 * DENY    = explicitly enforce the restriction.
 */
public enum RuleDecision {
    INHERIT,
    ALLOW,
    DENY;

    public RuleDecision next() {
        return switch (this) {
            case INHERIT -> DENY;
            case DENY -> ALLOW;
            case ALLOW -> INHERIT;
        };
    }

    public boolean resolve(boolean inherited) {
        return switch (this) {
            case INHERIT -> inherited;
            case ALLOW -> false;
            case DENY -> true;
        };
    }

    public static RuleDecision fromOrdinalSafe(int ordinal) {
        RuleDecision[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : INHERIT;
    }
}
