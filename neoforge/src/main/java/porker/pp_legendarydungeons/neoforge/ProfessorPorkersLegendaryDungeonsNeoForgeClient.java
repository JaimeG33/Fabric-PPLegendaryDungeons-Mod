package porker.pp_legendarydungeons.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.LegendaryDungeonsClient;

/**
 * Physical-client-only NeoForge bootstrap.
 */
@Mod(value = LegendaryDungeons.MOD_ID, dist = Dist.CLIENT)
public final class ProfessorPorkersLegendaryDungeonsNeoForgeClient {
    public ProfessorPorkersLegendaryDungeonsNeoForgeClient() {
        LegendaryDungeonsClient.init();
    }
}
