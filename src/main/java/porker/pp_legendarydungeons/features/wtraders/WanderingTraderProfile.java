package porker.pp_legendarydungeons.features.wtraders;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.Optional;
import java.util.function.Function;

public record WanderingTraderProfile(
        String id,
        Component displayName,
        Optional<WanderingTraderMapOffer> mapOffer,
        Function<Optional<ItemStack>, MerchantOffers> offerFactory
) {
    public boolean hasMapOffer() {
        return mapOffer.isPresent();
    }

    public MerchantOffers createOffers(Optional<ItemStack> generatedMap) {
        return offerFactory.apply(generatedMap);
    }
}