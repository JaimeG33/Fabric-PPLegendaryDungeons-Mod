package porker.pp_legendarydungeons.features.wtraders.json;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Converts loaded JSON trader profiles into MerchantOffers.
 *
 * This is the first gameplay-bridge scaffold. It is intentionally not connected
 * to WanderingTraderCommandSpawner yet.
 *
 * Supported in this version:
 * - direct_item
 * - pool_roll
 * - generated_map, if the caller supplies an already generated map ItemStack
 *
 * Map generation itself still belongs in the existing wtrader spawning flow. The
 * future connection step will generate maps from MapOfferJson.lootTable and pass
 * them into this generator.
 */
public final class WTraderTradeGenerator {
    private static final float PRICE_MULTIPLIER = 0.05F;

    private WTraderTradeGenerator() {
    }

    public static MerchantOffers createOffersForProfile(RandomSource random, TraderProfileJson profile) {
        return createOffersForProfile(random, profile, Map.of());
    }

    public static MerchantOffers createOffersForProfile(
            RandomSource random,
            TraderProfileJson profile,
            Map<ResourceLocation, ItemStack> generatedMaps
    ) {
        MerchantOffers offers = new MerchantOffers();

        if (profile == null) {
            return offers;
        }

        int totalTrades = Math.max(1, profile.totalTradesOrDefault(Integer.MAX_VALUE));

        for (TraderTradeJson guaranteedTrade : profile.guaranteedTradesOrEmpty()) {
            if (offers.size() >= totalTrades) {
                return offers;
            }

            addGuaranteedTrade(random, offers, totalTrades, profile, guaranteedTrade, generatedMaps);
        }

        for (TraderRandomSlotJson randomSlot : profile.randomSlotsOrEmpty()) {
            int count = Math.max(1, randomSlot.countOrDefault(1));

            for (int i = 0; i < count; i++) {
                if (offers.size() >= totalTrades) {
                    return offers;
                }

                createPoolRollOffer(
                        random,
                        randomSlot.categoryFilterMode,
                        randomSlot.categoriesOrEmpty(),
                        randomSlot.tagFilterMode,
                        randomSlot.tagsOrEmpty()
                ).ifPresent(offers::add);
            }
        }

        return offers;
    }

    private static void addGuaranteedTrade(
            RandomSource random,
            MerchantOffers offers,
            int totalTrades,
            TraderProfileJson profile,
            TraderTradeJson trade,
            Map<ResourceLocation, ItemStack> generatedMaps
    ) {
        if (trade == null || !trade.hasType()) {
            return;
        }

        switch (trade.type) {
            case WTraderJsonTradeTypes.DIRECT_ITEM -> createDirectItemOffer(random, trade).ifPresent(offers::add);

            case WTraderJsonTradeTypes.POOL_ROLL -> {
                int count = Math.max(1, trade.countOrDefault(1));

                for (int i = 0; i < count && offers.size() < totalTrades; i++) {
                    createPoolRollOffer(
                            random,
                            trade.categoryFilterMode,
                            trade.categoriesOrEmpty(),
                            trade.tagFilterMode,
                            trade.tagsOrEmpty()
                    ).ifPresent(offers::add);
                }
            }

            case WTraderJsonTradeTypes.GENERATED_MAP -> createGeneratedMapOffer(random, profile, trade, generatedMaps).ifPresent(offers::add);

            default -> LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] Profile {} has unsupported trade type: {}",
                    profile.id,
                    trade.type
            );
        }
    }

    private static Optional<MerchantOffer> createDirectItemOffer(RandomSource random, TraderTradeJson trade) {
        if (WTraderJsonValues.isBlank(trade.sellItem)) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] direct_item trade is missing sell_item."
            );
            return Optional.empty();
        }

        Item item = itemFromId(trade.sellItem);

        if (item == Items.AIR) {
            return Optional.empty();
        }

        int sellCount = randomBetween(random, trade.sellCountMinOrDefault(1), trade.sellCountMaxOrDefault(1));
        ItemStack sellStack = new ItemStack(item, sellCount);

        return createOfferFromStack(random, trade.basePrice, trade.secondaryPrice, sellStack, trade.maxUsesMinOrDefault(1), trade.maxUsesMaxOrDefault(1), trade.xpOrDefault(0));
    }

    private static Optional<MerchantOffer> createGeneratedMapOffer(
            RandomSource random,
            TraderProfileJson profile,
            TraderTradeJson trade,
            Map<ResourceLocation, ItemStack> generatedMaps
    ) {
        if (WTraderJsonValues.isBlank(trade.mapOffer)) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] generated_map trade in profile {} is missing map_offer.",
                    profile.id
            );
            return Optional.empty();
        }

        ResourceLocation mapOfferId;

        try {
            mapOfferId = ResourceLocation.parse(trade.mapOffer);
        } catch (Exception exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] generated_map trade in profile {} has invalid map_offer id: {}",
                    profile.id,
                    trade.mapOffer
            );
            return Optional.empty();
        }

        ItemStack generatedMap = generatedMaps.get(mapOfferId);

        if (generatedMap == null || generatedMap.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] Profile {} requested generated map {}, but no generated map ItemStack was supplied yet.",
                    profile.id,
                    mapOfferId
            );
            return Optional.empty();
        }

        return createOfferFromStack(
                random,
                trade.basePrice,
                trade.secondaryPrice,
                generatedMap.copy(),
                trade.maxUsesMinOrDefault(1),
                trade.maxUsesMaxOrDefault(1),
                trade.xpOrDefault(0)
        );
    }

    private static Optional<MerchantOffer> createPoolRollOffer(
            RandomSource random,
            String categoryFilterMode,
            List<String> categories,
            String tagFilterMode,
            List<String> tags
    ) {
        List<TradePoolJson> matchingPools = new ArrayList<>();

        for (TradePoolJson pool : WTraderJsonRegistry.tradePools().values()) {
            if (pool == null) {
                continue;
            }

            if (!WTraderTradeFilter.matchesCategory(categoryFilterMode, categories, pool.category)) {
                continue;
            }

            if (matchingEntries(pool, tagFilterMode, tags).isEmpty()) {
                continue;
            }

            matchingPools.add(pool);
        }

        Optional<TradePoolJson> pickedPool = WTraderWeightedPicker.pick(
                random,
                matchingPools,
                pool -> pool.categoryWeightOrDefault(0)
        );

        if (pickedPool.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] No trade pools matched category filter {} {} and tag filter {} {}.",
                    categoryFilterMode,
                    categories,
                    tagFilterMode,
                    tags
            );
            return Optional.empty();
        }

        TradePoolJson pool = pickedPool.get();
        List<TradeEntryJson> entries = matchingEntries(pool, tagFilterMode, tags);

        Optional<TradeEntryJson> pickedEntry = WTraderWeightedPicker.pick(
                random,
                entries,
                entry -> entry.weightOrDefault(0)
        );

        if (pickedEntry.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] Trade pool {} had no weighted matching entries.",
                    pool.id
            );
            return Optional.empty();
        }

        TradeEntryJson entry = pickedEntry.get();
        Item item = itemFromId(entry.item);

        if (item == Items.AIR) {
            return Optional.empty();
        }

        int sellCount = randomBetween(random, entry.countMinOrDefault(1), entry.countMaxOrDefault(1));
        ItemStack sellStack = new ItemStack(item, sellCount);

        return createOfferFromStack(
                random,
                pool.basePrice,
                pool.secondaryPrice,
                sellStack,
                entry.maxUsesMinOrDefault(1),
                entry.maxUsesMaxOrDefault(1),
                entry.xpOrDefault(0)
        );
    }

    private static List<TradeEntryJson> matchingEntries(TradePoolJson pool, String tagFilterMode, List<String> tags) {
        List<TradeEntryJson> matching = new ArrayList<>();

        for (TradeEntryJson entry : pool.entriesOrEmpty()) {
            if (entry == null) {
                continue;
            }

            List<String> combinedTags = new ArrayList<>();
            combinedTags.addAll(pool.tagsOrEmpty());
            combinedTags.addAll(entry.tagsOrEmpty());

            if (WTraderTradeFilter.matchesTags(tagFilterMode, tags, combinedTags)) {
                matching.add(entry);
            }
        }

        return matching;
    }

    private static Optional<MerchantOffer> createOfferFromStack(
            RandomSource random,
            TradePriceJson basePrice,
            TradePriceJson secondaryPrice,
            ItemStack sellStack,
            int maxUsesMin,
            int maxUsesMax,
            int xp
    ) {
        Optional<ItemCost> baseCost = createItemCost(random, basePrice);

        if (baseCost.isEmpty()) {
            return Optional.empty();
        }

        int maxUses = randomBetween(random, maxUsesMin, maxUsesMax);
        int safeXp = Math.max(0, xp);

        if (secondaryPrice == null) {
            return Optional.of(new MerchantOffer(
                    baseCost.get(),
                    sellStack,
                    maxUses,
                    safeXp,
                    PRICE_MULTIPLIER
            ));
        }

        Optional<ItemCost> secondaryCost = createItemCost(random, secondaryPrice);

        if (secondaryCost.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new MerchantOffer(
                baseCost.get(),
                secondaryCost,
                sellStack,
                maxUses,
                safeXp,
                PRICE_MULTIPLIER
        ));
    }

    private static Optional<ItemCost> createItemCost(RandomSource random, TradePriceJson price) {
        if (price == null || !price.hasItem()) {
            return Optional.empty();
        }

        Item item = itemFromId(price.item);

        if (item == Items.AIR) {
            return Optional.empty();
        }

        int count = price.countOrDefault(1);
        int min = count + price.varianceMinOrDefault(0);
        int max = count + price.varianceMaxOrDefault(0);
        int finalCount = randomBetween(random, min, max);

        finalCount = Math.max(1, Math.min(64, finalCount));

        return Optional.of(new ItemCost(item, finalCount));
    }

    private static Item itemFromId(String itemId) {
        try {
            ResourceLocation id = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.get(id);

            if (item == Items.AIR && !id.equals(ResourceLocation.withDefaultNamespace("air"))) {
                LegendaryDungeons.LOGGER.warn(
                        "[WTrader JSON] Item id {} resolved to minecraft:air.",
                        itemId
                );
                return Items.AIR;
            }

            return item;
        } catch (Exception exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader JSON] Invalid item id: {}",
                    itemId
            );
            return Items.AIR;
        }
    }

    private static int randomBetween(RandomSource random, int min, int max) {
        if (min > max) {
            int oldMin = min;
            min = max;
            max = oldMin;
        }

        if (min == max) {
            return min;
        }

        return min + random.nextInt(max - min + 1);
    }
}
