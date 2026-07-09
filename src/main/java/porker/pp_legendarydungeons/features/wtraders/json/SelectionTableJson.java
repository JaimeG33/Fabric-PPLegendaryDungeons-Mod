package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Runtime JSON model for:
 * data/<namespace>/wtrader_selection_tables/*.json
 */
public final class SelectionTableJson {
    public String id;

    @SerializedName("top_level_rolls")
    public List<WeightedResultJson> topLevelRolls;

    @SerializedName("custom_no_map_profiles")
    public List<WeightedProfileJson> customNoMapProfiles;

    @SerializedName("custom_map_profile_groups")
    public List<WeightedProfileGroupJson> customMapProfileGroups;

    public List<WeightedResultJson> topLevelRollsOrEmpty() {
        return WTraderJsonValues.listOrEmpty(topLevelRolls);
    }

    public List<WeightedProfileJson> customNoMapProfilesOrEmpty() {
        return WTraderJsonValues.listOrEmpty(customNoMapProfiles);
    }

    public List<WeightedProfileGroupJson> customMapProfileGroupsOrEmpty() {
        return WTraderJsonValues.listOrEmpty(customMapProfileGroups);
    }

    public boolean hasId() {
        return !WTraderJsonValues.isBlank(id);
    }
}
