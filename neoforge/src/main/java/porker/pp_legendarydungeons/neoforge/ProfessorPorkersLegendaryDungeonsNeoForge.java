package porker.pp_legendarydungeons.neoforge;

import net.neoforged.fml.common.Mod;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.neoforge.events.StructureMapNeoForgeEvents;

/**
 * Thin NeoForge loader entrypoint. Shared initialization remains in common.
 */
@Mod(LegendaryDungeons.MOD_ID)
public final class ProfessorPorkersLegendaryDungeonsNeoForge {
    public ProfessorPorkersLegendaryDungeonsNeoForge() {
        LegendaryDungeons.init();
        StructureMapNeoForgeEvents.register();
        LegendaryDungeons.LOGGER.info(
                "Cobblemon: Explore Legendary Dungeons initialized on NeoForge."
        );
    }
}
