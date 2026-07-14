package porker.pp_legendarydungeons.features.wtraders.runtime;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Executes a normal Minecraft loot table and returns one generated ItemStack for
 * use inside a trade.
 *
 * <p>This replaces the need to run /loot, spawn a temporary item entity, capture
 * it, and delete it. The CHEST loot context supplies the origin required by
 * exploration-map functions and optionally supplies the merchant as THIS_ENTITY.</p>
 */
public final class LootTableTradeStackFactory {
    private LootTableTradeStackFactory() {
    }

    public static Optional<ItemStack> generate(
            ServerLevel level,
            Vec3 origin,
            Entity contextEntity,
            String lootTableId
    ) {
        if (level == null || origin == null || lootTableId == null) {
            return Optional.empty();
        }

        ResourceLocation parsedId;

        try {
            parsedId = ResourceLocation.parse(lootTableId);
        } catch (Exception exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[WTrader Runtime] Invalid trade loot-table id: {}",
                    lootTableId
            );
            return Optional.empty();
        }

        try {
            ResourceKey<LootTable> lootTableKey = ResourceKey.create(
                    Registries.LOOT_TABLE,
                    parsedId
            );

            LootTable lootTable = level
                    .getServer()
                    .reloadableRegistries()
                    .getLootTable(lootTableKey);

            LootParams.Builder parameters = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, origin)
                    .withOptionalParameter(
                            LootContextParams.THIS_ENTITY,
                            contextEntity
                    );

            LootParams lootParams = parameters.create(
                    LootContextParamSets.CHEST
            );

            List<ItemStack> nonEmptyStacks = new ArrayList<>();

            for (ItemStack generated : lootTable.getRandomItems(lootParams)) {
                if (generated != null && !generated.isEmpty()) {
                    nonEmptyStacks.add(generated);
                }
            }

            if (nonEmptyStacks.isEmpty()) {
                LegendaryDungeons.LOGGER.warn(
                        "[WTrader Runtime] Loot table {} generated no usable trade stack at {}.",
                        parsedId,
                        origin
                );
                return Optional.empty();
            }

            if (nonEmptyStacks.size() > 1) {
                LegendaryDungeons.LOGGER.warn(
                        "[WTrader Runtime] Loot table {} generated {} stacks for one trade; using the first stack.",
                        parsedId,
                        nonEmptyStacks.size()
                );
            }

            return Optional.of(nonEmptyStacks.getFirst().copy());
        } catch (Throwable throwable) {
            LegendaryDungeons.LOGGER.error(
                    "[WTrader Runtime] Failed to generate trade stack from loot table {}.",
                    parsedId,
                    throwable
            );
            return Optional.empty();
        }
    }
}
