package porker.pp_legendarydungeons;

import net.fabricmc.api.ClientModInitializer;

/**
 * Thin Fabric client entrypoint.
 */
public final class ProfessorPorkersLegendaryDungeonsClient
        implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LegendaryDungeonsClient.init();
    }
}
