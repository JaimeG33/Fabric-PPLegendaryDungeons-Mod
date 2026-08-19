package porker.pp_legendarydungeons.features.dungeon_mobs;

/**
 * Mixin-backed storage boundary for persistent dungeon-mob encounter data.
 *
 * <p>Ordinary vanilla mobs do not expose arbitrary common-platform persistent
 * data. The shared Mob mixin implements this interface and writes the data into
 * the entity's normal NBT save/load cycle.</p>
 */
public interface DungeonMobDataHolder {
    DungeonMobPersistentData ppLegendaryDungeons$getDungeonMobData();

    void ppLegendaryDungeons$setDungeonMobData(DungeonMobPersistentData data);
}
