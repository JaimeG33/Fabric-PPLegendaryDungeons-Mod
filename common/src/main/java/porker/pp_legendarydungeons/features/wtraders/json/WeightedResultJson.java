package porker.pp_legendarydungeons.features.wtraders.json;

/**
 * Weighted top-level roll result.
 *
 * Example:
 * { "result": "custom_map", "weight": 40 }
 */
public final class WeightedResultJson {
    public String result;
    public Integer weight;

    public int weightOrDefault(int fallback) {
        return WTraderJsonValues.intOr(weight, fallback);
    }

    public boolean hasResult() {
        return !WTraderJsonValues.isBlank(result);
    }
}
