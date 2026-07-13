package porker.pp_legendarydungeons;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import porker.pp_legendarydungeons.dungeon_rules.events.DungeonRuleInteractionEvents;
import porker.pp_legendarydungeons.dungeon_rules.network.DungeonRuleNetworking;

/**
 * Fabric loader entrypoint.
 *
 * <p>Most initialization now lives in {@link LegendaryDungeons}. The two
 * registrations left here still use Fabric-specific APIs and will move into
 * shared Architectury code during their dedicated migration phases.</p>
 */
public final class ProfessorPorkersLegendaryDungeons implements ModInitializer {
    /**
     * Compatibility alias retained while existing classes are migrated away
     * from referencing the Fabric entrypoint directly.
     */
    public static final String MOD_ID = LegendaryDungeons.MOD_ID;

    /**
     * Compatibility alias retained for existing logging call sites.
     */
    public static final Logger LOGGER = LegendaryDungeons.LOGGER;

    @Override
    public void onInitialize() {
        LegendaryDungeons.init();

        /*
         * Deferred to later phases:
         * - Phase 3: interaction and protection events
         * - Phase 5: networking and client boundary
         */
        DungeonRuleNetworking.register();
        DungeonRuleInteractionEvents.register();

        LOGGER.info("Cobblemon: Explore Legendary Dungeons initialized on Fabric.");
    }
}
// Whenever ready to export build, remember .\gradlew.bat clean build
// Or also .\gradlew.bat clean runClient to boot up the game as well right after
// May change a bit during Architectury Multiloader Migration
