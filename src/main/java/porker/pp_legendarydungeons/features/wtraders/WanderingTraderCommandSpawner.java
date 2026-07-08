package porker.pp_legendarydungeons.features.wtraders;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;
import porker.pp_legendarydungeons.features.FeatureContext;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Spawns wandering traders for structure feature markers.
 *
 * Current behavior:
 * - 20% regular unaltered wandering trader
 * - 40% custom trader without map
 * - 40% custom trader with map
 *
 * Map traders use the new map system:
 * - The selected map loot table is run at the trader marker.
 * - The generated map item is captured.
 * - The generated map is inserted directly into the trader offer.
 * - The map keeps whatever hidden custom data the loot table gave it.
 *
 * Important:
 * This class should NOT manually add old voucher/replacement-map data.
 * Fixed maps and random maps should be configured through their loot tables.
 */
public final class WanderingTraderCommandSpawner {
    private WanderingTraderCommandSpawner() {
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

    /**
     * Spawns a normal wandering trader and does not override its offers.
     * This is the 20% "regular unaltered trader" result.
     */
    private static boolean spawnRegularTrader(FeatureContext context, ArmorStand traderMarker) {
        WanderingTrader trader = EntityType.WANDERING_TRADER.create(context.level());

        if (trader == null) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "Could not create regular wandering trader entity at {}.",
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

        trader.addTag("pp_spawned_feature_trader");
        trader.addTag("pp_regular_trader");

        boolean added = context.level().addFreshEntity(trader);

        if (!added) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "Regular wandering trader entity was not added to the world at {}.",
                    traderMarker.blockPosition()
            );
            return false;
        }

        ProfessorPorkersLegendaryDungeons.LOGGER.info(
                "Spawned regular wandering trader at {}.",
                trader.blockPosition()
        );

        return true;
    }

    private static boolean spawnCustomTraderWithoutMap(FeatureContext context, ArmorStand traderMarker) {
        WanderingTraderProfile profile = WanderingTraderPool.randomNoMap(context.level());
        return spawnProfileTrader(context, traderMarker, profile);
    }

    private static boolean spawnCustomTraderWithMap(FeatureContext context, ArmorStand traderMarker) {
        WanderingTraderProfile profile = WanderingTraderPool.randomWithMap(context.level());
        return spawnProfileTrader(context, traderMarker, profile);
    }

    /**
     * Spawns a custom trader from a profile.
     *
     * If the profile has a map offer, this generates a real map from the profile's
     * configured loot table before creating the trader offers.
     */
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
                    "Could not create custom wandering trader entity at {}.",
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
                    "Custom wandering trader entity was not added to the world at {}.",
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

    /**
     * Runs a map loot table at the trader marker, captures the newly spawned item,
     * removes the temporary item entity, and returns the generated ItemStack.
     *
     * This intentionally does not call MapGimmickData.markMapForLore(...).
     * The map loot table itself should decide whether the map uses:
     * - pp_gimmick = map_lore
     * - pp_gimmick = random_map_lore
     * - or any future map gimmick.
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

        ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                "Loot table {} ran at {}, but no generated item was captured.",
                lootTableId,
                marker.blockPosition()
        );

        return Optional.empty();
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
}