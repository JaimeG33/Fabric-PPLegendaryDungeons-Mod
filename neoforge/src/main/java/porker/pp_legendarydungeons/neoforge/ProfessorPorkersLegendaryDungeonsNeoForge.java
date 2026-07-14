package porker.pp_legendarydungeons.neoforge;

import net.neoforged.fml.common.Mod;
import porker.pp_legendarydungeons.LegendaryDungeons;

/**
 * Thin NeoForge loader entrypoint. Shared initialization remains in common.
 */
@Mod(LegendaryDungeons.MOD_ID)
public final class ProfessorPorkersLegendaryDungeonsNeoForge {
    public ProfessorPorkersLegendaryDungeonsNeoForge() {
        LegendaryDungeons.init();
        LegendaryDungeons.LOGGER.info(
                "Cobblemon: Explore Legendary Dungeons initialized on NeoForge."
        );
    }
}
