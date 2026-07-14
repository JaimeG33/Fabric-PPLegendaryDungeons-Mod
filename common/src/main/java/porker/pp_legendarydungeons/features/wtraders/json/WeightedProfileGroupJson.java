package porker.pp_legendarydungeons.features.wtraders.json;

import java.util.List;

/**
 * Weighted group of map profiles.
 *
 * Example groups:
 * - single_target
 * - random_misc
 * - random_research_outpost
 * - random_dungeon
 */
public final class WeightedProfileGroupJson {
    public String group;
    public Integer weight;
    public List<WeightedProfileJson> profiles;

    public int weightOrDefault(int fallback) {
        return WTraderJsonValues.intOr(weight, fallback);
    }

    public List<WeightedProfileJson> profilesOrEmpty() {
        return WTraderJsonValues.listOrEmpty(profiles);
    }

    public boolean hasGroup() {
        return !WTraderJsonValues.isBlank(group);
    }
}
