package porker.pp_legendarydungeons.fabric.events;

import dev.architectury.event.EventResult;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;
import porker.pp_legendarydungeons.items.maps.structure.StructureMapInteractionEvents;

/**
 * Fabric bridge for structure-map armor-stand interactions.
 *
 * <p>This feature must use a loader-specific event boundary. Armor stands use
 * Minecraft's position-aware entity interaction so they can choose the clicked
 * equipment slot. Architectury's Fabric {@code INTERACT_ENTITY} bridge hooks the
 * general {@code interact}/{@code interactOn} path, which can be bypassed when
 * the armor stand completes its hand-item swap through the position-aware path.
 * Fabric API's {@link UseEntityCallback} covers both forms on the logical client
 * and server, allowing the client to send the normal packet while the server
 * runs the shared claim logic before vanilla can transfer the placeholder map.</p>
 *
 * <p>Only this interception layer is Fabric-specific. Map-profile validation,
 * loot-table execution, duplicate prevention, and item delivery remain in the
 * common {@link StructureMapInteractionEvents} handler also used by NeoForge.</p>
 */
public final class StructureMapFabricEvents {
    private static boolean registered = false;

    private StructureMapFabricEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        UseEntityCallback.EVENT.register(
                (player, level, hand, entity, hitResult) -> {
                    EventResult result =
                            StructureMapInteractionEvents.handleInteraction(
                                    player,
                                    entity,
                                    hand
                            );

                    return result.isPresent()
                            ? result.asMinecraft()
                            : InteractionResult.PASS;
                }
        );
    }
}
