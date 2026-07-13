package porker.pp_legendarydungeons.items;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class ItemGimmickTicker {
    private static final int SCAN_INTERVAL_TICKS = 40;

    private ItemGimmickTicker() {
    }

    /**
     * Called once per server tick by the shared Architectury scheduler.
     */
    public static void tick(MinecraftServer server, long tickCount) {
        if (tickCount % SCAN_INTERVAL_TICKS != 0L) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            scanPlayerInventory(player);
        }
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
