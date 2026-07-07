package porker.pp_legendarydungeons.secrets;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SecretItemChecks {
    private SecretItemChecks() {
    }

    public static boolean hasBlueOrbItem(ArmorStand armorStand) {
        return handMatches(armorStand, stack ->
                stack.is(Items.LAPIS_BLOCK)
                        || itemIdEquals(stack, "mega_showdown:blueorb")
        );
    }

    public static boolean hasRedOrbItem(ArmorStand armorStand) {
        return handMatches(armorStand, stack ->
                stack.is(Items.REDSTONE_BLOCK)
                        || itemIdEquals(stack, "mega_showdown:redorb")
        );
    }

    public static boolean hasMeteoriteItem(ArmorStand armorStand) {
        return handMatches(armorStand, stack ->
                stack.is(Items.NETHER_STAR)
                        || itemIdEquals(stack, "mega_showdown:deoxys_meteorite")
        );
    }

    public static boolean hasNetherStar(ArmorStand armorStand) {
        return armorStand.getMainHandItem().is(Items.NETHER_STAR)
                || armorStand.getOffhandItem().is(Items.NETHER_STAR);
    }

    public static void clearNetherStar(ArmorStand armorStand) {
        if (armorStand.getMainHandItem().is(Items.NETHER_STAR)) {
            armorStand.setItemSlot(
                    net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                    ItemStack.EMPTY
            );
        }

        if (armorStand.getOffhandItem().is(Items.NETHER_STAR)) {
            armorStand.setItemSlot(
                    net.minecraft.world.entity.EquipmentSlot.OFFHAND,
                    ItemStack.EMPTY
            );
        }
    }

    private static boolean handMatches(ArmorStand armorStand, java.util.function.Predicate<ItemStack> predicate) {
        return predicate.test(armorStand.getMainHandItem())
                || predicate.test(armorStand.getOffhandItem());
    }

    private static boolean itemIdEquals(ItemStack stack, String id) {
        if (stack.isEmpty()) {
            return false;
        }

        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(id);
    }
}