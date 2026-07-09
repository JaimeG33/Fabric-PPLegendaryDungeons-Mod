package porker.pp_legendarydungeons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import porker.pp_legendarydungeons.features.FeatureTicker;
import porker.pp_legendarydungeons.items.ItemGimmickTicker;
import porker.pp_legendarydungeons.secrets.SecretTicker;
import porker.pp_legendarydungeons.setup.ModScoreboards;
import porker.pp_legendarydungeons.summon.trigger.EntityLegendarySummonTicker;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonReloadRegistrar;


/**
 * Main mod initializer.
 *
 * Fabric runs this when the mod loads.
 */
public class ProfessorPorkersLegendaryDungeons implements ModInitializer {
    /**
     * Your mod ID.
     *
     * Useful for logging, identifiers, and later registry names.
     */
    public static final String MOD_ID = "pp_legendarydungeons";

    /**
     * Logger for debugging.
     *
     * LegendarySummonHelper uses this to report spawn failures.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        // On Load Functions That run once
        // Calls the setup file to add teams / scoreboards.
        ServerLifecycleEvents.SERVER_STARTED.register(ModScoreboards::setup);

        // Stuff regarding the custom trade injections
        WTraderJsonReloadRegistrar.register();



        // Tick-like functions that each run while the game is loaded
        // Turns on the entity-based legendary summon scanner.
        EntityLegendarySummonTicker.register();

        // Turns on the regular/static feature scanner.
        FeatureTicker.register();

        // Turns on the special item gimmick scanner.
        ItemGimmickTicker.register();

        // Calls the secret-based scanner.
        SecretTicker.register();

        LOGGER.info("Cobblemon: Explore Legendary Dungeons initialized.");
    }
}