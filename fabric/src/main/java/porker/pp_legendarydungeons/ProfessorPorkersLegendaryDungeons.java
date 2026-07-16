package porker.pp_legendarydungeons;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import porker.pp_legendarydungeons.fabric.events.StructureMapFabricEvents;

/**
 * Thin Fabric loader entrypoint.
 *
 * <p>Shared initialization remains in {@link LegendaryDungeons}. Fabric-native
 * interaction hooks are registered here because armor-stand position-aware
 * interactions are not exposed early enough by Architectury's general entity
 * event.</p>
 */
public final class ProfessorPorkersLegendaryDungeons implements ModInitializer {
    public static final String MOD_ID = LegendaryDungeons.MOD_ID;
    public static final Logger LOGGER = LegendaryDungeons.LOGGER;

    @Override
    public void onInitialize() {
        LegendaryDungeons.init();
        StructureMapFabricEvents.register();
        LOGGER.info("Cobblemon: Explore Legendary Dungeons initialized on Fabric.");
    }
}
// Whenever ready to export build, remember .\gradlew.bat clean build
// Or also .\gradlew.bat clean runClient to boot up the game as well right after
// May change a bit during Architectury Multiloader Migration
