
package porker.pp_legendarydungeons.dungeon_rules.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import porker.pp_legendarydungeons.dungeon_rules.network.OpenDungeonRuleEditorPayload;

public final class DungeonRuleClientNetworking {
    private DungeonRuleClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                OpenDungeonRuleEditorPayload.TYPE,
                (payload, context) -> {
                    if (payload.parent()) {
                        context.client().setScreen(new DungeonRuleParentScreen(payload));
                    } else {
                        context.client().setScreen(new DungeonRuleZoneScreen(payload));
                    }
                }
        );
    }
}
