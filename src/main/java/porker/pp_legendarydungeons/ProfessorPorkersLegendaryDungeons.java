package porker.pp_legendarydungeons;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

/**
 * Thin Fabric loader entrypoint.
 *
 * <p>All common initialization, including networking, now lives in
 * {@link LegendaryDungeons}.</p>
 */
public final class ProfessorPorkersLegendaryDungeons implements ModInitializer {
    public static final String MOD_ID = LegendaryDungeons.MOD_ID;
    public static final Logger LOGGER = LegendaryDungeons.LOGGER;

    @Override
    public void onInitialize() {
        LegendaryDungeons.init();
        LOGGER.info("Cobblemon: Explore Legendary Dungeons initialized on Fabric.");
    }
}
// Whenever ready to export build, remember .\gradlew.bat clean build
// Or also .\gradlew.bat clean runClient to boot up the game as well right after
// May change a bit during Architectury Multiloader Migration
