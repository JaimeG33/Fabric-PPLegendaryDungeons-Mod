package porker.pp_legendarydungeons.summon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SummonAftermath {
    private SummonAftermath() {
    }

    public static void onRayquazaSummoned(
            ServerLevel level,
            ArmorStand armorStand,
            ServerPlayer player,
            PokemonEntity rayquaza
    ) {
        removeEmeraldBlockFromHand(armorStand);

        player.sendSystemMessage(
                Component.literal("The emerald block reacts... Rayquaza appeared!")
        );
    }

    private static void removeEmeraldBlockFromHand(ArmorStand armorStand) {
        if (armorStand.getMainHandItem().is(Items.EMERALD_BLOCK)) {
            armorStand.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return;
        }

        if (armorStand.getOffhandItem().is(Items.EMERALD_BLOCK)) {
            armorStand.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }
}