package porker.pp_legendarydungeons.maps;

/**
 * Central list of custom map targets.
 *
 * This first version stores the old datapack values so they are no longer scattered
 * through command strings and functions.
 *
 * The customModelData value is kept for compatibility/visuals.
 * Later, real map identity should use a more explicit item component/custom data marker.
 */
public enum LegendaryMapTarget {
    SKY_PILLAR(
            "skypillar",
            3311101,
            "Sky Pillar Exploration Map",
            "This map shows the location to the legendary dungeon of Rayquaza",
            "pp_legendarydungeons:maps/find_skypillar",
            "pp_legendarydungeons:skypillar_tag"
    );

    private final String id;
    private final int customModelData;
    private final String displayName;
    private final String lore;
    private final String lootTableId;
    private final String structureTagId;

    LegendaryMapTarget(
            String id,
            int customModelData,
            String displayName,
            String lore,
            String lootTableId,
            String structureTagId
    ) {
        this.id = id;
        this.customModelData = customModelData;
        this.displayName = displayName;
        this.lore = lore;
        this.lootTableId = lootTableId;
        this.structureTagId = structureTagId;
    }

    public String id() {
        return id;
    }

    public int customModelData() {
        return customModelData;
    }

    public String displayName() {
        return displayName;
    }

    public String lore() {
        return lore;
    }

    public String lootTableId() {
        return lootTableId;
    }

    public String structureTagId() {
        return structureTagId;
    }
}
