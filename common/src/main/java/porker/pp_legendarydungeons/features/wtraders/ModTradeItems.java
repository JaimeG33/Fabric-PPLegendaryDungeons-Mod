package porker.pp_legendarydungeons.features.wtraders;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import porker.pp_legendarydungeons.LegendaryDungeons;

public final class ModTradeItems {
    private ModTradeItems() {
    }

    public static Item modItem(String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));

        if (item == Items.AIR) {
            LegendaryDungeons.LOGGER.warn(
                    "Item id {} resolved to minecraft:air. Is the required mod loaded?",
                    id
            );
        }

        return item;
    }
}