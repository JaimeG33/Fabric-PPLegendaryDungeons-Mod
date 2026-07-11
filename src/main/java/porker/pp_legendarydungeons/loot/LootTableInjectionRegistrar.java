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
 *
 * <h2>Adding another injection</h2>
 *
 * <p>To inject a new custom loot table into one or more existing loot tables:</p>
 *
 * <ol>
 *     <li>
 *         Create the injection JSON somewhere under:
 *         {@code data/pp_legendarydungeons/loot_table/loot_injections/}
 *     </li>
 *     <li>
 *         Create a {@link ResourceKey} below using {@link #modLootTable(String)}.
 *         The path does not include {@code data/pp_legendarydungeons/loot_table/}
 *         or the {@code .json} extension.
 *     </li>
 *     <li>
 *         Create a target set containing each existing loot table that should
 *         receive the injection.
 *     </li>
 *     <li>
 *         Add a new {@link InjectionGroup} to {@link #INJECTION_GROUPS}.
 *     </li>
 * </ol>
 *
 * <p>Example:</p>
 *
 * <pre>
 * private static final ResourceKey&lt;LootTable&gt; EXAMPLE_INJECTION = modLootTable(
 *         "loot_injections/chests/example"
 * );
 *
 * private static final Set&lt;ResourceKey&lt;LootTable&gt;&gt; EXAMPLE_TARGETS = Set.of(
 *         vanillaLootTable("chests/simple_dungeon"),
 *         vanillaLootTable("chests/abandoned_mineshaft")
 * );
 *
 * // Then add this inside INJECTION_GROUPS:
 * new InjectionGroup(EXAMPLE_INJECTION, EXAMPLE_TARGETS)
 * </pre>
 *
 * <p>For a loot table belonging to another mod, use:</p>
 *
 * <pre>
 * lootTable("other_mod_id", "chests/example")
 * </pre>
 */
public final class LootTableInjectionRegistrar {
    private static boolean registered = false;

    /*
     * Reusable injection tables owned by this mod.
     *
     * These paths match JSON files under:
     * data/pp_legendarydungeons/loot_table/loot_injections/
     *
     * Do not include ".json" at the end of these paths.
     */
    private static final ResourceKey<LootTable> VILLAGE_RANDOM_MAPS_INJECTION = modLootTable(
            "loot_injections/chests/village_random_maps"
    );

    private static final ResourceKey<LootTable> SNOW_VILLAGE_EXTRAS_INJECTION = modLootTable(
            "loot_injections/chests/snow_village_extras"
    );

    private static final ResourceKey<LootTable> DUNGEON_KEY_BASE_INJECTION = modLootTable(
            "loot_injections/entities/dungeon_key_base"
    );

    /*
     * There is a dedicated vanilla cartographer chest table, but no dedicated
     * vanilla librarian chest table. The taiga house table is currently included
     * so the maps can also appear in taiga village residential chests.
     *
     * Remove any entry from this set if the maps feel too common, or add another
     * vanilla/mod loot-table ID to expand the injection to more village chests.
     */
    private static final Set<ResourceKey<LootTable>> VILLAGE_RANDOM_MAP_TARGETS = Set.of(
            vanillaLootTable("chests/village/village_cartographer")
    );

    /*
     * Adds snow_village_extras to:
     *
     * - Taiga village house chests
     * - The basement chest found inside vanilla igloos
     *
     * The injection table controls its own chances, weights, empty results,
     * quality values, and generated items.
     */
    private static final Set<ResourceKey<LootTable>> SNOW_VILLAGE_EXTRAS_TARGETS = Set.of(
            vanillaLootTable("chests/village/village_taiga_house"),
            vanillaLootTable("chests/igloo_chest")
    );

    /*
     * These are the normal vanilla death loot tables. The nested JSON table
     * controls the key's chance and requires the entity to have been killed by
     * a player. This does not modify the separate custom dungeon elder-guardian
     * loot table already present in the mod.
     */
    private static final Set<ResourceKey<LootTable>> DUNGEON_KEY_BASE_TARGETS = Set.of(
            vanillaLootTable("entities/warden"),
            vanillaLootTable("entities/elder_guardian"),
            vanillaLootTable("entities/wither")
    );

    /**
     * A tag-like Java grouping of reusable injection tables and their targets.
     *
     * <p>An actual loot-table datapack tag is unnecessary here because Fabric's
     * MODIFY callback already supplies the exact target ResourceKey. Keeping the
     * target lists here also makes the injection behavior visible in one file.</p>
     *
     * <p>Each entry follows this syntax:</p>
     *
     * <pre>
     * new InjectionGroup(INJECTION_TABLE, TARGET_TABLE_SET)
     * </pre>
     */
    private static final List<InjectionGroup> INJECTION_GROUPS = List.of(
            new InjectionGroup(VILLAGE_RANDOM_MAPS_INJECTION, VILLAGE_RANDOM_MAP_TARGETS),
            new InjectionGroup(SNOW_VILLAGE_EXTRAS_INJECTION, SNOW_VILLAGE_EXTRAS_TARGETS),
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

    /**
     * Creates a key for an existing vanilla loot table.
     *
     * Example:
     * vanillaLootTable("chests/igloo_chest")
     */
    private static ResourceKey<LootTable> vanillaLootTable(String path) {
        return lootTableKey(ResourceLocation.withDefaultNamespace(path));
    }

    /**
     * Creates a key for a loot table owned by this mod.
     *
     * Example:
     * modLootTable("loot_injections/chests/snow_village_extras")
     */
    private static ResourceKey<LootTable> modLootTable(String path) {
        return lootTable(
                ProfessorPorkersLegendaryDungeons.MOD_ID,
                path
        );
    }

    /**
     * Creates a key for a loot table belonging to any namespace.
     *
     * This can be used later to target another mod's bundled loot table:
     *
     * lootTable("other_mod_id", "chests/example")
     */
    private static ResourceKey<LootTable> lootTable(
            String namespace,
            String path
    ) {
        return lootTableKey(ResourceLocation.fromNamespaceAndPath(
                namespace,
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