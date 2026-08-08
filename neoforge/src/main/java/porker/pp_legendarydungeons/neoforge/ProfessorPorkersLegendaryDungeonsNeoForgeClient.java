package porker.pp_legendarydungeons.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.LegendaryDungeonsClient;

/**
 * Physical-client-only NeoForge bootstrap.
 *
 * <p>Client initialization is deferred until {@link FMLClientSetupEvent}.
 * NeoForge constructs @Mod classes before registry events run, so resolving
 * registered block-entity suppliers directly from this constructor can happen
 * before those registry objects exist.</p>
 */
@Mod(value = LegendaryDungeons.MOD_ID, dist = Dist.CLIENT)
public final class ProfessorPorkersLegendaryDungeonsNeoForgeClient {
    public ProfessorPorkersLegendaryDungeonsNeoForgeClient(IEventBus modBus) {
        modBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(LegendaryDungeonsClient::init);
    }
}
