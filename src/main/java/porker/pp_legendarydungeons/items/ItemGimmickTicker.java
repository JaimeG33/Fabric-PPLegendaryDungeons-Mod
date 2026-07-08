package porker.pp_legendarydungeons.items;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class ItemGimmickTicker {
    private static final int SCAN_INTERVAL_TICKS = 40;
    private static int ticks = 0;

    private ItemGimmickTicker() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ticks++;

            if (ticks % SCAN_INTERVAL_TICKS != 0) {
                return;
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                scanPlayerInventory(player);
            }
        });
    }

    private static void scanPlayerInventory(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        boolean changedAnyStack = false;

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            boolean changedThisStack = ItemGimmickService.process(
                    new ItemGimmickContext(player, slot, stack)
            );

            if (changedThisStack) {
                changedAnyStack = true;
            }
        }

        if (changedAnyStack) {
            inventory.setChanged();
            player.inventoryMenu.broadcastChanges();
        }
    }
}
