package porker.pp_legendarydungeons.features.wtraders;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.List;
import java.util.Optional;

public final class WanderingTraderPool {
    private static final float PRICE_MULTIPLIER = 0.05F;

    private static final List<WanderingTraderProfile> CUSTOM_NO_MAP = List.of(
            createExampleNoMapTrader()
    );

    private static final List<WanderingTraderProfile> CUSTOM_WITH_MAP = List.of(
            createSkyPillarTrader(),
            createRandomDungeonMapTrader(),
            createResearchOutpostMapTrader(),
            createMiscMapTrader()
    );

    private WanderingTraderPool() {
    }

    public static WanderingTraderProfile randomNoMap(ServerLevel level) {
        return CUSTOM_NO_MAP.get(level.getRandom().nextInt(CUSTOM_NO_MAP.size()));
    }

    public static WanderingTraderProfile randomWithMap(ServerLevel level) {
        return CUSTOM_WITH_MAP.get(level.getRandom().nextInt(CUSTOM_WITH_MAP.size()));
    }

    private static WanderingTraderProfile createExampleNoMapTrader() {
        return new WanderingTraderProfile(
                "example_no_map",
                Component.literal("Wandering Supplies Trader"),
                Optional.empty(),
                generatedMap -> {
                    MerchantOffers offers = new MerchantOffers();

                    offers.add(new MerchantOffer(
                            new ItemCost(Items.EMERALD, 3),
                            new ItemStack(Items.CHERRY_SAPLING, 5),
                            3,
                            1,
                            PRICE_MULTIPLIER
                    ));

                    offers.add(new MerchantOffer(
                            new ItemCost(Items.APPLE, 1),
                            new ItemStack(ModTradeItems.modItem("cobblemon:leftovers"), 1),
                            8,
                            1,
                            PRICE_MULTIPLIER
                    ));

                    return offers;
                }
        );
    }

    private static WanderingTraderProfile createSkyPillarTrader() {
        return new WanderingTraderProfile(
                "to_skypillar",
                Component.literal("Sky Pillar Map Trader"),
                Optional.of(WanderingTraderMapOffer.SKY_PILLAR),
                generatedMap -> {
                    MerchantOffers offers = new MerchantOffers();

                    offers.add(new MerchantOffer(
                            new ItemCost(Items.EMERALD, 3),
                            new ItemStack(Items.CHERRY_SAPLING, 5),
                            3,
                            1,
                            PRICE_MULTIPLIER
                    ));

                    generatedMap.ifPresent(map -> offers.add(new MerchantOffer(
                            new ItemCost(Items.EMERALD, 32),
                            Optional.of(new ItemCost(ModTradeItems.modItem("cobblemon:relic_coin"), 32)),
                            map.copy(),
                            1,
                            1,
                            PRICE_MULTIPLIER
                    )));

                    offers.add(new MerchantOffer(
                            new ItemCost(Items.EMERALD, 8),
                            new ItemStack(ModTradeItems.modItem("cobblemon:flying_gem"), 1),
                            4,
                            1,
                            PRICE_MULTIPLIER
                    ));

                    offers.add(new MerchantOffer(
                            new ItemCost(Items.APPLE, 1),
                            new ItemStack(ModTradeItems.modItem("cobblemon:leftovers"), 1),
                            8,
                            1,
                            PRICE_MULTIPLIER
                    ));

                    offers.add(new MerchantOffer(
                            new ItemCost(Items.EMERALD, 5),
                            new ItemStack(ModTradeItems.modItem("cobblemon:green_apricorn_seed"), 1),
                            6,
                            1,
                            PRICE_MULTIPLIER
                    ));

                    return offers;
                }
        );
    }

    private static WanderingTraderProfile createRandomDungeonMapTrader() {
        return new WanderingTraderProfile(
                "random_dungeon_map",
                Component.literal("Legendary Dungeon Map Trader"),
                Optional.of(WanderingTraderMapOffer.RANDOM_DUNGEON),
                generatedMap -> {
                    MerchantOffers offers = new MerchantOffers();

                    offers.add(new MerchantOffer(
                            new ItemCost(Items.EMERALD, 24),
                            Optional.of(new ItemCost(ModTradeItems.modItem("cobblemon:relic_coin"), 16)),
                            generatedMap.orElse(new ItemStack(Items.MAP)).copy(),
                            1,
                            1,
                            PRICE_MULTIPLIER
                    ));

                    return offers;
                }
        );
    }

    private static WanderingTraderProfile createResearchOutpostMapTrader() {
        return new WanderingTraderProfile(
                "random_research_outpost_map",
                Component.literal("Research Outpost Map Trader"),
                Optional.of(WanderingTraderMapOffer.RANDOM_RESEARCH_OUTPOST),
                generatedMap -> {
                    MerchantOffers offers = new MerchantOffers();

                    offers.add(new MerchantOffer(
                            new ItemCost(Items.EMERALD, 16),
                            generatedMap.orElse(new ItemStack(Items.MAP)).copy(),
                            1,
                            1,
                            PRICE_MULTIPLIER
                    ));

                    return offers;
                }
        );
    }

    private static WanderingTraderProfile createMiscMapTrader() {
        return new WanderingTraderProfile(
                "random_misc_map",
                Component.literal("Strange Structure Map Trader"),
                Optional.of(WanderingTraderMapOffer.RANDOM_MISC),
                generatedMap -> {
                    MerchantOffers offers = new MerchantOffers();

                    offers.add(new MerchantOffer(
                            new ItemCost(Items.EMERALD, 12),
                            generatedMap.orElse(new ItemStack(Items.MAP)).copy(),
                            1,
                            1,
                            PRICE_MULTIPLIER
                    ));

                    return offers;
                }
        );
    }
}