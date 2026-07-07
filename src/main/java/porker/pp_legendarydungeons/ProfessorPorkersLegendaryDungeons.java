package porker.pp_legendarydungeons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import porker.pp_legendarydungeons.secrets.SecretTicker;
import porker.pp_legendarydungeons.setup.ModScoreboards;
import porker.pp_legendarydungeons.summon.trigger.EntityLegendarySummonTicker;

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
        // Turns on the entity-based legendary summon scanner.
        EntityLegendarySummonTicker.register();

        // Calls the setup file to add teams / scoreboards.
        ServerLifecycleEvents.SERVER_STARTED.register(ModScoreboards::setup);

        // Calls the secret-based scanner.
        SecretTicker.register();

        LOGGER.info("Professor Porker's Legendary Dungeons initialized.");
    }
}