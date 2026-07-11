
package porker.pp_legendarydungeons.dungeon_rules.instance;

public enum DungeonInstanceState {
    ACTIVE,
    COMPLETED,
    DISABLED;

    public static DungeonInstanceState parse(String value) {
        try {
            return DungeonInstanceState.valueOf(value);
        } catch (Exception ignored) {
            return ACTIVE;
        }
    }
}
