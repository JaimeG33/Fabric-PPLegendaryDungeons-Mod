package porker.pp_legendarydungeons;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import porker.pp_legendarydungeons.dungeon_rules.network.DungeonRuleNetworking;

/**
 * Fabric loader entrypoint.
 *
 * <p>Shared initialization lives in {@link LegendaryDungeons}. Only Fabric
 * networking remains here until the networking and client-boundary phase.</p>
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
         * Deferred to Phase 5: networking and client boundary.
         */
        DungeonRuleNetworking.register();

        LOGGER.info("Cobblemon: Explore Legendary Dungeons initialized on Fabric.");
    }
}
// Whenever ready to export build, remember .\gradlew.bat clean build
// Or also .\gradlew.bat clean runClient to boot up the game as well right after
// May change a bit during Architectury Multiloader Migration
