package porker.pp_legendarydungeons.loot;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

import java.util.List;
import java.util.Set;

/**
 * Registers additive loot-table injections for vanilla or mod-bundled loot tables.
 *
 * <p>Each injection group connects one reusable loot table from this mod to a set
 * of existing target loot tables. Fabric adds the nested pool to the existing
 * builder; it does not replace the target table or remove pools added by vanilla
 * or other mods.</p>
 *
 * <p>The actual drop chances, weights, and generated items stay in JSON under:</p>
 *
 * <pre>
 * src/main/resources/data/pp_legendarydungeons/loot_table/loot_injections/
 * </pre>
 */
public final class LootTableInjectionRegistrar {
    private static boolean registered = false;

    /*
     * Reusable injection tables owned by this mod.
     *
     * These paths match JSON files under:
     * data/pp_legendarydungeons/loot_table/loot_injections/
     */
    private static final ResourceKey<LootTable> VILLAGE_RANDOM_MAPS_INJECTION = modLootTable(
            "loot_injections/chests/village_random_maps"
    );

    private static final ResourceKey<LootTable> DUNGEON_KEY_BASE_INJECTION = modLootTable(
            "loot_injections/entities/dungeon_key_base"
    );

    /*
     * There is a dedicated vanilla cartographer chest table, but no dedicated
     * vanilla librarian chest table. The generic biome house tables are included
     * so the maps can also appear in ordinary village residential chests.
     *
     * Remove any entry from this set if the maps feel too common, or add another
     * vanilla/mod loot-table ID to expand the injection to more village chests.
     */
    private static final Set<ResourceKey<LootTable>> VILLAGE_RANDOM_MAP_TARGETS = Set.of(
            vanillaLootTable("chests/village/village_cartographer"),
            vanillaLootTable("chests/village/village_desert_house"),
            vanillaLootTable("chests/village/village_plains_house"),
            vanillaLootTable("chests/village/village_savanna_house"),
            vanillaLootTable("chests/village/village_snowy_house"),
            vanillaLootTable("chests/village/village_taiga_house")
    );

    /*
     * These are the normal vanilla death loot tables. The nested JSON table
     * controls the key's chance and requires the entity to have been killed by
     * a player. This does not modify the separate custom dungeon elder-guardian
     * loot table already present in the mod.
     */
    private static final Set<ResourceKey<LootTable>> DUNGEON_KEY_BASE_TARGETS = Set.of(
            vanillaLootTable("entities/warden"),
            vanillaLootTable("entities/elder_guardian")
    );

    /**
     * A tag-like Java grouping of reusable injection tables and their targets.
     *
     * <p>An actual loot-table datapack tag is unnecessary here because Fabric's
     * MODIFY callback already supplies the exact target ResourceKey. Keeping the
     * target lists here also makes the injection behavior visible in one file.</p>
     */
    private static final List<InjectionGroup> INJECTION_GROUPS = List.of(
            new InjectionGroup(VILLAGE_RANDOM_MAPS_INJECTION, VILLAGE_RANDOM_MAP_TARGETS),
            new InjectionGroup(DUNGEON_KEY_BASE_INJECTION, DUNGEON_KEY_BASE_TARGETS)
    );

    private LootTableInjectionRegistrar() {
    }

    /**
     * Registers the Fabric loot-table callback once during mod initialization.
     */
    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            /*
             * Respect external datapack replacements. Vanilla tables and tables
             * bundled by mods are builtin; external datapack and REPLACE-event
             * tables are not.
             */
            if (!source.isBuiltin()) {
                return;
            }

            for (InjectionGroup group : INJECTION_GROUPS) {
                if (group.targets().contains(key)) {
                    addNestedInjection(tableBuilder, group.injectionTable());
                }
            }
        });

        ProfessorPorkersLegendaryDungeons.LOGGER.info(
                "[Loot Injection] Registered {} additive loot-table injection groups.",
                INJECTION_GROUPS.size()
        );
    }

    /**
     * Adds one independent pool that delegates its result to the injection table.
     * The referenced JSON table decides whether anything drops and what it is.
     */
    private static void addNestedInjection(
            LootTable.Builder tableBuilder,
            ResourceKey<LootTable> injectionTable
    ) {
        tableBuilder.withPool(
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(NestedLootTable.lootTableReference(injectionTable))
        );
    }

    private static ResourceKey<LootTable> vanillaLootTable(String path) {
        return lootTableKey(ResourceLocation.withDefaultNamespace(path));
    }

    private static ResourceKey<LootTable> modLootTable(String path) {
        return lootTableKey(ResourceLocation.fromNamespaceAndPath(
                ProfessorPorkersLegendaryDungeons.MOD_ID,
                path
        ));
    }

    private static ResourceKey<LootTable> lootTableKey(ResourceLocation id) {
        return ResourceKey.create(Registries.LOOT_TABLE, id);
    }

    private record InjectionGroup(
            ResourceKey<LootTable> injectionTable,
            Set<ResourceKey<LootTable>> targets
    ) {
    }
}
