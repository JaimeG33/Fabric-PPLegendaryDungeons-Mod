package porker.pp_legendarydungeons.features.wtraders.runtime;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.features.wtraders.json.MapOfferJson;
import porker.pp_legendarydungeons.features.wtraders.json.TraderProfileJson;
import porker.pp_legendarydungeons.features.wtraders.json.TraderTradeJson;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonRegistry;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonTradeGenerator;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonTradeTypes;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonValues;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Creates a complete MerchantOffers list from an existing WTrader JSON profile
 * without requiring a structure marker or temporary dropped item entities.
 *
 * <p>This is used by naturally spawned wandering traders. It is also designed
 * so the structure trader spawner can be refactored to use it later.</p>
 */
public final class WTraderProfileOfferFactory {
    private WTraderProfileOfferFactory() {
    }

    public static Optional<MerchantOffers> createOffers(
            ServerLevel level,
            Entity merchant,
            TraderProfileJson profile
    ) {
        if (level == null || merchant == null || profile == null) {
            return Optional.empty();
        }

        Optional<Map<ResourceLocation, ItemStack>> generatedMaps =
                generateMapsForProfile(level, merchant, profile);

        if (generatedMaps.isEmpty()) {
            return Optional.empty();
        }

        MerchantOffers offers = WTraderJsonTradeGenerator.createOffersForProfile(
                level.getRandom(),
                profile,
                generatedMaps.get(),
                lootTableId -> LootTableTradeStackFactory.generate(
                        level,
                        merchant.position(),
                        merchant,
                        lootTableId
                )
        );

        if (offers.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader Runtime] Profile {} generated zero offers.",
                    profile.id
            );
            return Optional.empty();
        }

        return Optional.of(offers);
    }

    private static Optional<Map<ResourceLocation, ItemStack>> generateMapsForProfile(
            ServerLevel level,
            Entity merchant,
            TraderProfileJson profile
    ) {
        Map<ResourceLocation, ItemStack> generatedMaps = new HashMap<>();

        for (TraderTradeJson trade : profile.guaranteedTradesOrEmpty()) {
            if (trade == null
                    || !WTraderJsonTradeTypes.GENERATED_MAP.equals(trade.type)) {
                continue;
            }

            if (WTraderJsonValues.isBlank(trade.mapOffer)) {
                LegendaryDungeons.LOGGER.warn(
                        "[WTrader Runtime] Profile {} has generated_map trade with missing map_offer.",
                        profile.id
                );
                return Optional.empty();
            }

            ResourceLocation mapOfferId;

            try {
                mapOfferId = ResourceLocation.parse(trade.mapOffer);
            } catch (Exception exception) {
                LegendaryDungeons.LOGGER.warn(
                        "[WTrader Runtime] Profile {} has invalid map_offer id {}.",
                        profile.id,
                        trade.mapOffer
                );
                return Optional.empty();
            }

            if (generatedMaps.containsKey(mapOfferId)) {
                continue;
            }

            Optional<MapOfferJson> mapOffer =
                    WTraderJsonRegistry.getMapOffer(mapOfferId);

            if (mapOffer.isEmpty()) {
                LegendaryDungeons.LOGGER.warn(
                        "[WTrader Runtime] Profile {} referenced missing map offer {}.",
                        profile.id,
                        mapOfferId
                );
                return Optional.empty();
            }

            Optional<ItemStack> generatedMap =
                    LootTableTradeStackFactory.generate(
                            level,
                            merchant.position(),
                            merchant,
                            mapOffer.get().lootTable
                    );

            if (generatedMap.isEmpty()) {
                LegendaryDungeons.LOGGER.warn(
                        "[WTrader Runtime] Could not generate map {} for profile {} at {}.",
                        mapOffer.get().lootTable,
                        profile.id,
                        merchant.blockPosition()
                );
                return Optional.empty();
            }

            generatedMaps.put(mapOfferId, generatedMap.get());
        }

        return Optional.of(generatedMaps);
    }
}
