package porker.pp_legendarydungeons.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record ItemGimmickContext(
        ServerPlayer player,
        int slotIndex,
        ItemStack stack
) {
}
