package porker.pp_legendarydungeons;

import dev.architectury.event.events.common.LifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRulePreviewManager;
import porker.pp_legendarydungeons.dungeon_rules.events.DungeonRuleBlockEvents;
import porker.pp_legendarydungeons.dungeon_rules.events.DungeonRuleInteractionEvents;
import porker.pp_legendarydungeons.dungeon_rules.network.DungeonRuleNetworking;
import porker.pp_legendarydungeons.dungeon_rules.preset.DungeonRulePresetRegistry;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonReloadRegistrar;
import porker.pp_legendarydungeons.features.wtraders.villager.VillagerTradeInjectionRegistrar;
import porker.pp_legendarydungeons.items.maps.structure.StructureMapInteractionEvents;
import porker.pp_legendarydungeons.loot.LootTableInjectionRegistrar;
import porker.pp_legendarydungeons.loot.pokemon.PokemonLootTableRegistrar;
import porker.pp_legendarydungeons.server.ServerTickScheduler;
import porker.pp_legendarydungeons.setup.ModBlockEntities;
import porker.pp_legendarydungeons.setup.ModBlocks;
import porker.pp_legendarydungeons.setup.ModScoreboards;
import porker.pp_legendarydungeons.setup.ModStructureTypes;
import porker.pp_legendarydungeons.summon.dungeon_completion.DungeonCompletionRegistry;

/**
 * Loader-neutral initialization entrypoint.
 *
 * <p>The current project is still a single-module Fabric project, but this
 * class avoids Fabric entrypoint and Fabric event/networking types. Future
 * Fabric and NeoForge entrypoints will both call {@link #init()}.</p>
 */
public final class LegendaryDungeons {
    public static final String MOD_ID = "pp_legendarydungeons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean initialized = false;

    private LegendaryDungeons() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;

        /*
         * Static and deferred registrations must happen before worlds create
         * or load structure data and dungeon-rule block entities.
         */
        DungeonRulePresetRegistry.bootstrap();
        DungeonCompletionRegistry.bootstrap();
        ModStructureTypes.register();
        ModBlocks.register();
        ModBlockEntities.register();

        /*
         * Shared Architectury networking and event boundaries.
         */
        DungeonRuleNetworking.register();
        registerLifecycleEvents();
        DungeonRuleInteractionEvents.register();
        StructureMapInteractionEvents.register();
        DungeonRuleBlockEvents.register();

        WTraderJsonReloadRegistrar.register();

        /*
         * Adds the three data-driven cartographer map factories to vanilla's
         * candidate lists without replacing vanilla or modded trades.
         */
        VillagerTradeInjectionRegistrar.register();

        /*
         * Adds independent pools to selected existing chest and entity loot
         * tables without replacing their existing contents.
         */
        LootTableInjectionRegistrar.register();

        /*
         * Cobblemon exposes these Pokémon defeat/drop events from shared code,
         * so this bridge is already suitable for the future common module.
         */
        PokemonLootTableRegistrar.register();

        ServerTickScheduler.register();
    }

    private static void registerLifecycleEvents() {
        LifecycleEvent.SERVER_STARTED.register(server -> {
            ModScoreboards.setup(server);
            DungeonRuleManager.bootstrap(server);
        });

        LifecycleEvent.SERVER_STOPPED.register(server -> {
            DungeonRuleManager.clear();
            DungeonRulePreviewManager.clear();

            /*
             * Clears temporary battle-faint context used by Pokémon loot
             * injection and resets the shared scheduler's interval counter.
             */
            PokemonLootTableRegistrar.clear();
            ServerTickScheduler.reset();
        });
    }
}
