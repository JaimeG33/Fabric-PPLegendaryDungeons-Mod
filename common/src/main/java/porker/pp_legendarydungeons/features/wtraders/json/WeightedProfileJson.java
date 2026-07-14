package porker.pp_legendarydungeons.features.wtraders.json;

/**
 * Weighted profile reference.
 *
 * Example:
 * { "profile": "pp_legendarydungeons:to_skypillar", "weight": 10 }
 */
public final class WeightedProfileJson {
    public String profile;
    public Integer weight;

    public int weightOrDefault(int fallback) {
        return WTraderJsonValues.intOr(weight, fallback);
    }

    public boolean hasProfile() {
        return !WTraderJsonValues.isBlank(profile);
    }
}
