package porker.pp_legendarydungeons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRulePreviewManager;
import porker.pp_legendarydungeons.dungeon_rules.events.DungeonRuleInteractionEvents;
import porker.pp_legendarydungeons.dungeon_rules.network.DungeonRuleNetworking;
import porker.pp_legendarydungeons.dungeon_rules.preset.DungeonRulePresetRegistry;
import porker.pp_legendarydungeons.features.FeatureTicker;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonReloadRegistrar;
import porker.pp_legendarydungeons.items.ItemGimmickTicker;
import porker.pp_legendarydungeons.loot.LootTableInjectionRegistrar;
import porker.pp_legendarydungeons.secrets.SecretTicker;
import porker.pp_legendarydungeons.setup.ModBlockEntities;
import porker.pp_legendarydungeons.setup.ModBlocks;
import porker.pp_legendarydungeons.setup.ModScoreboards;
import porker.pp_legendarydungeons.summon.dungeon_completion.DungeonCompletionRegistry;
import porker.pp_legendarydungeons.summon.trigger.EntityLegendarySummonTicker;
import porker.pp_legendarydungeons.loot.pokemon.PokemonLootTableRegistrar;

/**
 * Main mod initializer.
 */
public class ProfessorPorkersLegendaryDungeons implements ModInitializer {
    public static final String MOD_ID = "pp_legendarydungeons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        /*
         * Static registrations must happen before worlds create/load the block entities.
         */
        DungeonRulePresetRegistry.bootstrap();
        DungeonCompletionRegistry.bootstrap();
        ModBlocks.register();
        ModBlockEntities.register();
        DungeonRuleNetworking.register();
        DungeonRuleInteractionEvents.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ModScoreboards.setup(server);
            DungeonRuleManager.bootstrap(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            DungeonRuleManager.clear();
            DungeonRulePreviewManager.clear();

            /*
             * Clears temporary battle-faint context used by Pokémon loot injection.
             */
            PokemonLootTableRegistrar.clear();
        });

        WTraderJsonReloadRegistrar.register();

        /*
         * Adds independent pools to selected existing chest/entity loot tables.
         * This preserves the original table and additions made by other mods.
         */
        LootTableInjectionRegistrar.register();
        /*
         * Bridges Cobblemon's native Pokémon drop events to additional Minecraft
         * loot tables. The initial version runs in diagnostic-only mode.
         */
        PokemonLootTableRegistrar.register();

        EntityLegendarySummonTicker.register();
        FeatureTicker.register();
        ItemGimmickTicker.register();
        SecretTicker.register();

        LOGGER.info("Cobblemon: Explore Legendary Dungeons initialized.");
    }
}
// Whenever ready to export build, remember .\gradlew.bat clean build
