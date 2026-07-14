package porker.pp_legendarydungeons.features.wtraders.villager;

import dev.architectury.registry.level.entity.trade.TradeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonTradeGenerator;
import porker.pp_legendarydungeons.features.wtraders.runtime.LootTableTradeStackFactory;

/**
 * Adds data-driven offers to vanilla villager profession candidate lists.
 *
 * <p>This uses Architectury's additive trade registry. It does not replace the
 * vanilla cartographer table and does not remove trades registered by other
 * mods.</p>
 *
 * <h2>Current cartographer additions</h2>
 *
 * <ul>
 *     <li>Level 1: random misc map pool</li>
 *     <li>Level 1: random research-outpost map pool</li>
 *     <li>Level 5: random dungeon map pool</li>
 * </ul>
 *
 * <p>Each listing references one exact WTrader trade_pool. The pool supplies
 * prices, max uses, XP, price multiplier, and the loot table that generates the
 * functional map.</p>
 */
public final class VillagerTradeInjectionRegistrar {
    private static final ResourceLocation CARTOGRAPHER_RANDOM_MISC_POOL =
            modId("villagers/cartographer/random_misc_map");

    private static final ResourceLocation CARTOGRAPHER_RANDOM_RESEARCH_POOL =
            modId("villagers/cartographer/random_research_outpost_map");

    private static final ResourceLocation CARTOGRAPHER_RANDOM_DUNGEON_POOL =
            modId("villagers/cartographer/random_dungeon_map");

    private static boolean registered = false;

    private VillagerTradeInjectionRegistrar() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        /*
         * Vanilla level 1 has paper and empty-map candidates. These two
         * factories are appended to the same candidate list.
         */
        TradeRegistry.registerVillagerTrade(
                VillagerProfession.CARTOGRAPHER,
                1,
                new TradePoolListing(CARTOGRAPHER_RANDOM_MISC_POOL),
                new TradePoolListing(CARTOGRAPHER_RANDOM_RESEARCH_POOL)
        );

        /*
         * Vanilla level 5 has the globe banner-pattern candidate. With no other
         * trade-injection mods installed, vanilla's two-trades-per-level logic
         * selects both the globe pattern and this dungeon map.
         */
        TradeRegistry.registerVillagerTrade(
                VillagerProfession.CARTOGRAPHER,
                5,
                new TradePoolListing(CARTOGRAPHER_RANDOM_DUNGEON_POOL)
        );

        LegendaryDungeons.LOGGER.info(
                "[Villager Trades] Registered 3 additive Architectury cartographer trade-pool listings."
        );
    }

    private static ResourceLocation modId(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                LegendaryDungeons.MOD_ID,
                path
        );
    }

    /**
     * Resolves one exact trade pool when the villager generates this level's
     * offers. Returning null is the normal vanilla signal that this candidate
     * could not create an offer.
     */
    private record TradePoolListing(
            ResourceLocation tradePoolId
    ) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(
                Entity trader,
                RandomSource random
        ) {
            if (!(trader.level() instanceof ServerLevel serverLevel)) {
                return null;
            }

            return WTraderJsonTradeGenerator.createOfferFromPool(
                    random,
                    tradePoolId,
                    lootTableId -> LootTableTradeStackFactory.generate(
                            serverLevel,
                            trader.position(),
                            trader,
                            lootTableId
                    )
            ).orElse(null);
        }
    }
}
