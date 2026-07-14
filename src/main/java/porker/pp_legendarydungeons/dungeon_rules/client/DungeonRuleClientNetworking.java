package porker.pp_legendarydungeons.dungeon_rules.client;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import porker.pp_legendarydungeons.dungeon_rules.network.OpenDungeonRuleEditorPayload;
import porker.pp_legendarydungeons.dungeon_rules.network.UpdateDungeonRuleBlockPayload;

/**
 * Shared client-side Architectury networking for dungeon-rule screens.
 *
 * <p>This class is initialized only from the loader's client entrypoint.</p>
 */
public final class DungeonRuleClientNetworking {
    private static boolean registered = false;

    private DungeonRuleClientNetworking() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                OpenDungeonRuleEditorPayload.TYPE,
                OpenDungeonRuleEditorPayload.CODEC,
                (payload, context) ->
                        context.queue(() -> openEditorScreen(payload))
        );
    }

    /**
     * Sends one typed controller update to the server.
     *
     * <p>The packet payload already contains its type and codec information,
     * so no manually allocated byte buffer is required.</p>
     */
    public static void sendUpdate(
            UpdateDungeonRuleBlockPayload payload
    ) {
        NetworkManager.sendToServer(payload);
    }

    private static void openEditorScreen(
            OpenDungeonRuleEditorPayload payload
    ) {
        Minecraft client = Minecraft.getInstance();

        if (payload.parent()) {
            client.setScreen(new DungeonRuleParentScreen(payload));
        } else {
            client.setScreen(new DungeonRuleZoneScreen(payload));
        }
    }
}