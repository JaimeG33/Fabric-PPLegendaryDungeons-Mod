
package porker.pp_legendarydungeons;

import net.fabricmc.api.ClientModInitializer;
import porker.pp_legendarydungeons.dungeon_rules.client.DungeonRuleClientNetworking;

public final class ProfessorPorkersLegendaryDungeonsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DungeonRuleClientNetworking.register();
    }
}
