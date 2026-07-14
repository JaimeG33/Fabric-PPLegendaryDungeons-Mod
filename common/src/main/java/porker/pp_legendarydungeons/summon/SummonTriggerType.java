package porker.pp_legendarydungeons.summon;

/**
 * Describes HOW a summon attempt started.
 *
 * Right now, we only use ENTITY_PROXIMITY because the player gets near an armor stand.
 *
 * The other values are here so the system can expand later without changing the whole design.
 * For example, a custom block could create a BLOCK_INTERACTION summon context later.
 */
public enum SummonTriggerType {
    /**
     * Triggered when a player gets near an entity marker, such as an armor stand.
     */
    ENTITY_PROXIMITY,

    /**
     * Future use: triggered when a player right-clicks a custom block.
     */
    BLOCK_INTERACTION,

    /**
     * Future use: triggered by a ticking block entity.
     */
    BLOCK_TICK,

    /**
     * Future use: triggered by dungeon progression, such as clearing a boss phase.
     */
    DUNGEON_PHASE
}