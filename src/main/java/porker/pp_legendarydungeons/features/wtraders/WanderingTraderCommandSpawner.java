package porker.pp_legendarydungeons.features.wtraders;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;
import porker.pp_legendarydungeons.features.FeatureContext;
import porker.pp_legendarydungeons.items.maps.MapGimmickData;
import porker.pp_legendarydungeons.maps.LegendaryMapTarget;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Spawns custom wandering traders for structure feature markers.
 *
 * This version does NOT sell the old placeholder/voucher map.
 *
 * Instead:
 * 1. It runs the existing Sky Pillar map loot table at the trader marker.
 * 2. It captures the generated map ItemStack.
 * 3. It marks the generated map with hidden custom data.
 * 4. It deletes the temporary dropped item.
 * 5. It spawns a wandering trader.
 * 6. After the trader exists in the world, it clears vanilla offers and inserts custom offers.
 */
public final class WanderingTraderCommandSpawner {
    private static final float PRICE_MULTIPLIER = 0.05F;

    private WanderingTraderCommandSpawner() {
    }

    public static boolean spawnSkyPillarTrader(FeatureContext context, ArmorStand traderMarker) {
        Optional<ItemStack> generatedMap = generateMapFromLootTable(
                context,
                traderMarker,
                LegendaryMapTarget.SKY_PILLAR
        );

        if (generatedMap.isEmpty()) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "Could not generate Sky Pillar map at {}. Trader was not spawned.",
                    traderMarker.blockPosition()
            );
            return false;
        }

        ItemStack skyPillarMap = generatedMap.get();

        // Adds hidden custom data:
        // pp_gimmick = map_lore
        // pp_map_target = skypillar
        // pp_coords_revealed = false
        //
        // The item gimmick ticker will add the lore/coordinates only after
        // the player owns the map.
        MapGimmickData.markMapForLore(
                skyPillarMap,
                LegendaryMapTarget.SKY_PILLAR,
                context.level()
        );

        WanderingTrader trader = EntityType.WANDERING_TRADER.create(context.level());

        if (trader == null) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "Could not create wandering trader entity at {}.",
                    traderMarker.blockPosition()
            );
            return false;
        }

        trader.moveTo(
                traderMarker.getX(),
                traderMarker.getY(),
                traderMarker.getZ(),
                traderMarker.getYRot(),
                traderMarker.getXRot()
        );

        trader.setPersistenceRequired();
        trader.setDespawnDelay(48000);

        trader.setCustomName(Component.literal("Sky Pillar Map Trader"));
        trader.setCustomNameVisible(true);

        trader.addTag("pp_spawned_feature_trader");
        trader.addTag("pp_map_trader");
        trader.addTag("pp_to_skypillar");

        boolean added = context.level().addFreshEntity(trader);

        if (!added) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "Wandering trader entity was not added to the world at {}.",
                    traderMarker.blockPosition()
            );
            return false;
        }

        applySkyPillarOffersAfterSpawn(trader, skyPillarMap);

        ProfessorPorkersLegendaryDungeons.LOGGER.info(
                "Spawned Sky Pillar map trader at {} with {} custom offers.",
                trader.blockPosition(),
                trader.getOffers().size()
        );

        return true;
    }

    /**
     * Important:
     * The trader must already exist in the world before this runs.
     *
     * Calling getOffers() here allows vanilla to initialize its offer list if it wants to.
     * Then we immediately clear that list and insert our own custom offers.
     */
    private static void applySkyPillarOffersAfterSpawn(WanderingTrader trader, ItemStack generatedSkyPillarMap) {
        MerchantOffers offers = trader.getOffers();

        offers.clear();
        offers.addAll(createSkyPillarOffers(generatedSkyPillarMap));

        trader.overrideOffers(offers);
    }

    /**
     * Uses your existing loot table:
     * pp_legendarydungeons:maps/find_skypillar
     *
     * This lets the loot table's minecraft:exploration_map function generate the real map,
     * but captures it before the player ever buys it.
     */
    private static Optional<ItemStack> generateMapFromLootTable(
            FeatureContext context,
            ArmorStand marker,
            String lootTableId
    ) {
        AABB searchBox = marker.getBoundingBox().inflate(2.0D);

        Set<UUID> existingItemEntities = new HashSet<>();
        List<ItemEntity> beforeItems = context.level().getEntitiesOfClass(
                ItemEntity.class,
                searchBox,
                Entity::isAlive
        );

        for (ItemEntity itemEntity : beforeItems) {
            existingItemEntities.add(itemEntity.getUUID());
        }

        boolean commandSucceeded = runCommandAtMarker(
                context,
                marker,
                "loot spawn ~ ~ ~ loot " + lootTableId
        );

        if (!commandSucceeded) {
            return Optional.empty();
        }

        List<ItemEntity> afterItems = context.level().getEntitiesOfClass(
                ItemEntity.class,
                searchBox,
                itemEntity ->
                        itemEntity.isAlive()
                                && !existingItemEntities.contains(itemEntity.getUUID())
                                && !itemEntity.getItem().isEmpty()
        );

        for (ItemEntity itemEntity : afterItems) {
            ItemStack generatedStack = itemEntity.getItem().copy();
            generatedStack.setCount(1);

            itemEntity.discard();

            return Optional.of(generatedStack);
        }

        return Optional.empty();
    }

    private static MerchantOffers createSkyPillarOffers(ItemStack generatedSkyPillarMap) {
        MerchantOffers offers = new MerchantOffers();

        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 3),
                new ItemStack(Items.CHERRY_SAPLING, 5),
                3,
                1,
                PRICE_MULTIPLIER
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 32),
                Optional.of(new ItemCost(modItem("cobblemon:relic_coin"), 32)),
                generatedSkyPillarMap.copy(),
                1,
                1,
                PRICE_MULTIPLIER
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 8),
                new ItemStack(modItem("cobblemon:flying_gem"), 1),
                4,
                1,
                PRICE_MULTIPLIER
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.APPLE, 1),
                new ItemStack(modItem("cobblemon:leftovers"), 1),
                8,
                1,
                PRICE_MULTIPLIER
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 5),
                new ItemStack(modItem("cobblemon:green_apricorn_seed"), 1),
                6,
                1,
                PRICE_MULTIPLIER
        ));

        // Temporary simplified copy of the old iron hoe trade.
        // We can restore the exact enchantments later.
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 10),
                Optional.of(new ItemCost(modItem("cobblemon:relic_coin"), 16)),
                new ItemStack(Items.IRON_HOE, 1),
                1,
                1,
                PRICE_MULTIPLIER
        ));

        return offers;
    }

    private static Item modItem(String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));

        if (item == Items.AIR) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "Item id {} resolved to minecraft:air. Is the required mod loaded?",
                    id
            );
        }

        return item;
    }

    private static boolean runCommandAtMarker(FeatureContext context, ArmorStand marker, String command) {
        try {
            CommandSourceStack source = context.level()
                    .getServer()
                    .createCommandSourceStack()
                    .withLevel(context.level())
                    .withPosition(marker.position())
                    .withPermission(4)
                    .withSuppressedOutput();

            context.level().getServer().getCommands().performPrefixedCommand(source, command);
            return true;
        } catch (Exception exception) {
            ProfessorPorkersLegendaryDungeons.LOGGER.error(
                    "Failed to run wandering trader helper command at {}: {}",
                    marker.blockPosition(),
                    command,
                    exception
            );
            return false;
        }
    }

    public static boolean spawnRolledTrader(FeatureContext context, ArmorStand traderMarker) {
        int roll = context.level().getRandom().nextInt(100);

        if (roll < 20) {
            return spawnRegularTrader(context, traderMarker);
        }

        if (roll < 60) {
            return spawnCustomTraderWithoutMap(context, traderMarker);
        }

        return spawnCustomTraderWithMap(context, traderMarker);
    }

    private static boolean spawnRegularTrader(FeatureContext context, ArmorStand traderMarker) {
        WanderingTrader trader = EntityType.WANDERING_TRADER.create(context.level());

        if (trader == null) {
            return false;
        }

        trader.moveTo(
                traderMarker.getX(),
                traderMarker.getY(),
                traderMarker.getZ(),
                traderMarker.getYRot(),
                traderMarker.getXRot()
        );

        trader.setPersistenceRequired();
        trader.setDespawnDelay(48000);

        trader.addTag("pp_spawned_feature_trader");
        trader.addTag("pp_regular_trader");

        return context.level().addFreshEntity(trader);
    }

    private static boolean spawnCustomTraderWithoutMap(FeatureContext context, ArmorStand traderMarker) {
        WanderingTraderProfile profile = WanderingTraderPool.randomNoMap(context.level());
        return spawnProfileTrader(context, traderMarker, profile);
    }

    private static boolean spawnCustomTraderWithMap(FeatureContext context, ArmorStand traderMarker) {
        WanderingTraderProfile profile = WanderingTraderPool.randomWithMap(context.level());
        return spawnProfileTrader(context, traderMarker, profile);
    }

    private static boolean spawnProfileTrader(
            FeatureContext context,
            ArmorStand traderMarker,
            WanderingTraderProfile profile
    ) {
        Optional<ItemStack> generatedMap = Optional.empty();

        if (profile.mapOffer().isPresent()) {
            WanderingTraderMapOffer mapOffer = profile.mapOffer().get();

            generatedMap = generateMapFromLootTable(
                    context,
                    traderMarker,
                    mapOffer.lootTableId()
            );

            if (generatedMap.isEmpty()) {
                ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                        "Could not generate map {} for wandering trader profile {} at {}.",
                        mapOffer.lootTableId(),
                        profile.id(),
                        traderMarker.blockPosition()
                );

                return false;
            }
        }

        MerchantOffers offers = profile.createOffers(generatedMap);

        return spawnTraderWithOffers(
                context,
                traderMarker,
                profile,
                offers
        );
    }

    private static boolean spawnTraderWithOffers(
            FeatureContext context,
            ArmorStand traderMarker,
            WanderingTraderProfile profile,
            MerchantOffers offers
    ) {
        WanderingTrader trader = EntityType.WANDERING_TRADER.create(context.level());

        if (trader == null) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "Could not create wandering trader entity at {}.",
                    traderMarker.blockPosition()
            );
            return false;
        }

        trader.moveTo(
                traderMarker.getX(),
                traderMarker.getY(),
                traderMarker.getZ(),
                traderMarker.getYRot(),
                traderMarker.getXRot()
        );

        trader.setPersistenceRequired();
        trader.setDespawnDelay(48000);

        trader.setCustomName(profile.displayName());
        trader.setCustomNameVisible(true);

        trader.addTag("pp_spawned_feature_trader");
        trader.addTag("pp_custom_trader");
        trader.addTag("pp_wtrader_" + profile.id());

        if (profile.hasMapOffer()) {
            trader.addTag("pp_map_trader");
        } else {
            trader.addTag("pp_no_map_trader");
        }

        boolean added = context.level().addFreshEntity(trader);

        if (!added) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "Wandering trader entity was not added to the world at {}.",
                    traderMarker.blockPosition()
            );
            return false;
        }

        MerchantOffers activeOffers = trader.getOffers();
        activeOffers.clear();
        activeOffers.addAll(offers);
        trader.overrideOffers(activeOffers);

        ProfessorPorkersLegendaryDungeons.LOGGER.info(
                "Spawned wandering trader profile {} at {} with {} offers.",
                profile.id(),
                trader.blockPosition(),
                trader.getOffers().size()
        );

        return true;
    }
}