package porker.pp_legendarydungeons.features.wtraders.json;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

import java.util.List;

/**
 * Validation helpers for the wandering trader JSON loader.
 *
 * The goal is to skip broken JSON definitions with useful log messages instead
 * of crashing the whole game. The validator is intentionally conservative for
 * the first version.
 */
public final class WTraderJsonValidator {
    private WTraderJsonValidator() {
    }

    public static boolean validateTradePool(ResourceLocation fileId, TradePoolJson pool) {
        if (pool == null) {
            warn(fileId, "Trade pool JSON parsed to null.");
            return false;
        }

        if (!requireId(fileId, pool.id, "trade pool")) {
            return false;
        }

        if (WTraderJsonValues.isBlank(pool.category)) {
            warn(fileId, "Trade pool {} is missing category.", pool.id);
            return false;
        }

        if (pool.categoryWeightOrDefault(0) <= 0) {
            warn(fileId, "Trade pool {} must have category_weight > 0.", pool.id);
            return false;
        }

        if (!validatePrice(fileId, pool.id, "base_price", pool.basePrice, true)) {
            return false;
        }

        if (pool.secondaryPrice != null && !validatePrice(fileId, pool.id, "secondary_price", pool.secondaryPrice, false)) {
            return false;
        }

        List<TradeEntryJson> entries = pool.entriesOrEmpty();

        if (entries.isEmpty()) {
            warn(fileId, "Trade pool {} has no entries.", pool.id);
            return false;
        }

        boolean anyValidEntry = false;

        for (int index = 0; index < entries.size(); index++) {
            if (validateTradeEntry(fileId, pool.id, index, entries.get(index))) {
                anyValidEntry = true;
            }
        }

        if (!anyValidEntry) {
            warn(fileId, "Trade pool {} has no valid entries.", pool.id);
            return false;
        }

        return true;
    }

    private static boolean validateTradeEntry(ResourceLocation fileId, String poolId, int index, TradeEntryJson entry) {
        if (entry == null) {
            warn(fileId, "Trade pool {} has null entry at index {}.", poolId, index);
            return false;
        }

        boolean hasItem = !WTraderJsonValues.isBlank(entry.item);
        boolean hasLootTable = !WTraderJsonValues.isBlank(entry.lootTable);

        if (!hasItem && !hasLootTable) {
            warn(fileId, "Trade pool {} entry {} is missing item or loot_table.", poolId, index);
            return false;
        }

        if (hasItem && hasLootTable) {
            warn(fileId, "Trade pool {} entry {} must use only one of item or loot_table, not both.", poolId, index);
            return false;
        }

        if (hasItem && !validateItemId(fileId, poolId, "entry " + index + " item", entry.item)) {
            return false;
        }

        if (hasLootTable && !validateResourceLocation(fileId, poolId, "entry " + index + " loot_table", entry.lootTable)) {
            return false;
        }

        if (entry.weightOrDefault(0) <= 0) {
            warn(fileId, "Trade pool {} entry {} must have weight > 0.", poolId, index);
            return false;
        }

        if (!validateRange(fileId, poolId, "entry " + index + " count", entry.countMinOrDefault(1), entry.countMaxOrDefault(1), 1, 64)) {
            return false;
        }

        if (!validateRange(fileId, poolId, "entry " + index + " max_uses", entry.maxUsesMinOrDefault(1), entry.maxUsesMaxOrDefault(1), 1, Integer.MAX_VALUE)) {
            return false;
        }

        if (entry.xpOrDefault(0) < 0) {
            warn(fileId, "Trade pool {} entry {} has negative xp.", poolId, index);
            return false;
        }

        return true;
    }

    public static boolean validateTraderProfile(ResourceLocation fileId, TraderProfileJson profile) {
        if (profile == null) {
            warn(fileId, "Trader profile JSON parsed to null.");
            return false;
        }

        if (!requireId(fileId, profile.id, "trader profile")) {
            return false;
        }

        if (WTraderJsonValues.isBlank(profile.displayName)) {
            warn(fileId, "Trader profile {} is missing display_name.", profile.id);
            return false;
        }

        if (WTraderJsonValues.isBlank(profile.traderType)) {
            warn(fileId, "Trader profile {} is missing trader_type.", profile.id);
            return false;
        }

        if (profile.totalTradesOrDefault(0) <= 0) {
            warn(fileId, "Trader profile {} must have total_trades > 0.", profile.id);
            return false;
        }

        // The deeper validation of guaranteed trade fields happens in the future
        // trade generator. This first loader only confirms the profile is shaped
        // enough to store safely.
        return true;
    }

    public static boolean validateSelectionTable(ResourceLocation fileId, SelectionTableJson table) {
        if (table == null) {
            warn(fileId, "Selection table JSON parsed to null.");
            return false;
        }

        if (!requireId(fileId, table.id, "selection table")) {
            return false;
        }

        if (table.topLevelRollsOrEmpty().isEmpty()) {
            warn(fileId, "Selection table {} has no top_level_rolls.", table.id);
            return false;
        }

        boolean anyTopLevelRoll = false;

        for (int index = 0; index < table.topLevelRollsOrEmpty().size(); index++) {
            WeightedResultJson result = table.topLevelRollsOrEmpty().get(index);

            if (result != null && result.hasResult() && result.weightOrDefault(0) > 0) {
                anyTopLevelRoll = true;
            } else {
                warn(fileId, "Selection table {} has invalid top_level_roll at index {}.", table.id);
            }
        }

        if (!anyTopLevelRoll) {
            warn(fileId, "Selection table {} has no valid top_level_rolls.", table.id);
            return false;
        }

        return true;
    }

    public static boolean validateMapOffer(ResourceLocation fileId, MapOfferJson mapOffer) {
        if (mapOffer == null) {
            warn(fileId, "Map offer JSON parsed to null.");
            return false;
        }

        if (!requireId(fileId, mapOffer.id, "map offer")) {
            return false;
        }

        if (WTraderJsonValues.isBlank(mapOffer.lootTable)) {
            warn(fileId, "Map offer {} is missing loot_table.", mapOffer.id);
            return false;
        }

        if (!validateResourceLocation(fileId, mapOffer.id, "loot_table", mapOffer.lootTable)) {
            return false;
        }

        return true;
    }

    private static boolean validatePrice(
            ResourceLocation fileId,
            String ownerId,
            String fieldName,
            TradePriceJson price,
            boolean required
    ) {
        if (price == null) {
            if (required) {
                warn(fileId, "{} is missing required {}.", ownerId, fieldName);
            }

            return !required;
        }

        if (WTraderJsonValues.isBlank(price.item)) {
            warn(fileId, "{} has {} with missing item.", ownerId, fieldName);
            return false;
        }

        if (!validateItemId(fileId, ownerId, fieldName + " item", price.item)) {
            return false;
        }

        int count = price.countOrDefault(0);

        if (count <= 0 || count > 64) {
            warn(fileId, "{} has {} count outside 1-64: {}", ownerId, fieldName, count);
            return false;
        }

        int min = price.varianceMinOrDefault(0);
        int max = price.varianceMaxOrDefault(0);

        if (min > max) {
            warn(fileId, "{} has {} variance_min greater than variance_max.", ownerId, fieldName);
            return false;
        }

        if (count + min <= 0) {
            warn(fileId, "{} has {} variance that can reduce price below 1.", ownerId, fieldName);
            return false;
        }

        if (count + max > 64) {
            warn(fileId, "{} has {} variance that can increase price above 64.", ownerId, fieldName);
            return false;
        }

        return true;
    }

    private static boolean validateRange(
            ResourceLocation fileId,
            String ownerId,
            String fieldName,
            int min,
            int max,
            int allowedMin,
            int allowedMax
    ) {
        if (min > max) {
            warn(fileId, "{} has {} min greater than max.", ownerId, fieldName);
            return false;
        }

        if (min < allowedMin || max > allowedMax) {
            warn(fileId, "{} has {} outside allowed range {}-{}: {}-{}.", ownerId, fieldName, allowedMin, allowedMax, min, max);
            return false;
        }

        return true;
    }

    private static boolean requireId(ResourceLocation fileId, String id, String typeName) {
        if (WTraderJsonValues.isBlank(id)) {
            warn(fileId, "{} is missing id.", typeName);
            return false;
        }

        return validateResourceLocation(fileId, id, "id", id);
    }

    private static boolean validateItemId(ResourceLocation fileId, String ownerId, String fieldName, String itemId) {
        ResourceLocation parsed = parseResourceLocation(fileId, ownerId, fieldName, itemId);

        if (parsed == null) {
            return false;
        }

        Item item = BuiltInRegistries.ITEM.get(parsed);

        if (item == Items.AIR && !parsed.equals(ResourceLocation.withDefaultNamespace("air"))) {
            warn(fileId, "{} has {} that resolved to minecraft:air: {}", ownerId, fieldName, itemId);
            return false;
        }

        return true;
    }

    private static boolean validateResourceLocation(ResourceLocation fileId, String ownerId, String fieldName, String value) {
        return parseResourceLocation(fileId, ownerId, fieldName, value) != null;
    }

    private static ResourceLocation parseResourceLocation(ResourceLocation fileId, String ownerId, String fieldName, String value) {
        try {
            return ResourceLocation.parse(value);
        } catch (Exception exception) {
            warn(fileId, "{} has invalid {} resource location: {}", ownerId, fieldName, value);
            return null;
        }
    }

    private static void warn(ResourceLocation fileId, String message, Object... args) {
        ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                "[WTrader JSON] {}: " + message,
                prepend(fileId, args)
        );
    }

    private static Object[] prepend(ResourceLocation fileId, Object[] args) {
        Object[] output = new Object[args.length + 1];
        output[0] = fileId;
        System.arraycopy(args, 0, output, 1, args.length);
        return output;
    }
}
