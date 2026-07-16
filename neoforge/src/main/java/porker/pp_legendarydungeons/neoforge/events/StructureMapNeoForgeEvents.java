package porker.pp_legendarydungeons.neoforge.events;

import dev.architectury.event.EventResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import porker.pp_legendarydungeons.items.maps.structure.StructureMapInteractionEvents;

/**
 * NeoForge bridge for armor-stand interactions.
 *
 * <p>This feature must use a loader-specific event boundary. NeoForge exposes
 * armor stands' position-aware interaction as
 * {@link PlayerInteractEvent.EntityInteractSpecific}, which fires before the
 * general entity event. Vanilla can complete the equipment swap there, so an
 * Architectury {@code INTERACT_ENTITY} listener would run too late or not run at
 * all. This bridge forwards the earlier specific event to the shared map-claim
 * handler and cancels vanilla swapping when the handler claims the click.</p>
 *
 * <p>Fabric requires its own equivalent bridge through
 * {@code UseEntityCallback}. Only interception differs between loaders; map
 * validation, loot-table execution, duplicate prevention, and delivery remain
 * in the common {@link StructureMapInteractionEvents} implementation.</p>
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
