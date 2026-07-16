package porker.pp_legendarydungeons.neoforge.events;

import dev.architectury.event.EventResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import porker.pp_legendarydungeons.items.maps.structure.StructureMapInteractionEvents;

/**
 * NeoForge bridge for armor-stand interactions.
 *
 * <p>NeoForge fires {@link PlayerInteractEvent.EntityInteractSpecific} before
 * its general entity-interaction event. Armor stands normally complete their
 * equipment swap during this specific event, preventing Architectury's general
 * {@code INTERACT_ENTITY} bridge from seeing the click. This listener forwards
 * the earlier event to the shared map-claim handler and cancels vanilla swapping
 * whenever the common handler claims the interaction.</p>
 */
public final class StructureMapNeoForgeEvents {
    private static boolean registered = false;

    private StructureMapNeoForgeEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        NeoForge.EVENT_BUS.addListener(
                StructureMapNeoForgeEvents::onEntityInteractSpecific
        );
    }

    private static void onEntityInteractSpecific(
            PlayerInteractEvent.EntityInteractSpecific event
    ) {
        EventResult result = StructureMapInteractionEvents.handleInteraction(
                event.getEntity(),
                event.getTarget(),
                event.getHand()
        );

        if (!result.isPresent()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(result.asMinecraft());
    }
}
