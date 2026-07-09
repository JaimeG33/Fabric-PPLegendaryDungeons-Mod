package porker.pp_legendarydungeons.features.wtraders.json;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Category/tag filtering helpers used by pool-roll trades.
 *
 * Category filters apply to a TradePoolJson.category.
 * Tag filters apply to the combined tags of the pool and the selected entry.
 */
public final class WTraderTradeFilter {
    private WTraderTradeFilter() {
    }

    public static boolean matchesCategory(String mode, List<String> requestedCategories, String category) {
        List<String> categories = WTraderJsonValues.listOrEmpty(requestedCategories);

        if (categories.isEmpty()) {
            return true;
        }

        String normalizedMode = WTraderJsonValues.stringOr(mode, WTraderJsonFilterModes.INCLUDE);

        if (WTraderJsonFilterModes.EXCLUDE.equals(normalizedMode)) {
            return !categories.contains(category);
        }

        return categories.contains(category);
    }

    public static boolean matchesTags(String mode, List<String> requestedTags, List<String> availableTags) {
        List<String> tags = WTraderJsonValues.listOrEmpty(requestedTags);

        if (tags.isEmpty()) {
            return true;
        }

        String normalizedMode = WTraderJsonValues.stringOr(mode, WTraderJsonFilterModes.INCLUDE);
        Set<String> available = new HashSet<>(WTraderJsonValues.listOrEmpty(availableTags));

        if (WTraderJsonFilterModes.EXCLUDE.equals(normalizedMode)) {
            for (String tag : tags) {
                if (available.contains(tag)) {
                    return false;
                }
            }

            return true;
        }

        // Include mode requires every requested tag to be present.
        return available.containsAll(tags);
    }
}
