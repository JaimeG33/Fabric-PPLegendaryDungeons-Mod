package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Datapack-defined metadata for one broad random-map category.
 */
public final class MapGroupJson {
    public String id;

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("description_lines")
    public List<MapDescriptionLineJson> descriptionLines;

    public List<String> aliases;

    public List<MapDescriptionLineJson> descriptionLinesOrEmpty() {
        return WTraderJsonValues.listOrEmpty(descriptionLines);
    }

    public List<String> aliasesOrEmpty() {
        return WTraderJsonValues.listOrEmpty(aliases);
    }
}
