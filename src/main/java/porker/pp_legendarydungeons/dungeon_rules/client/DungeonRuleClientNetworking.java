package porker.pp_legendarydungeons.dungeon_rules.client;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
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
                OpenDungeonRuleEditorPayload.ID,
                (buffer, context) -> {
                    OpenDungeonRuleEditorPayload payload =
                            OpenDungeonRuleEditorPayload.CODEC.decode(buffer);

                    context.queue(() -> openEditorScreen(payload));
                }
        );
    }

    public static void sendUpdate(UpdateDungeonRuleBlockPayload payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        UpdateDungeonRuleBlockPayload.CODEC.encode(buffer, payload);

        NetworkManager.sendToServer(
                UpdateDungeonRuleBlockPayload.ID,
                buffer
        );
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
