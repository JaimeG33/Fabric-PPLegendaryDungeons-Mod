package porker.pp_legendarydungeons.features.wtraders.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Datapack-defined metadata for one specific functional-map destination.
 *
 * <p>The id is a stable metadata id. Built-in targets generally use the same
 * id as the concrete worldgen structure they describe, but the loader does not
 * require that convention.</p>
 */
public final class MapTargetJson {
    public String id;

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("description_lines")
    public List<MapDescriptionLineJson> descriptionLines;

    @SerializedName("structure_tag")
    public String structureTag;

    public List<String> groups;
    public List<String> aliases;

    public List<MapDescriptionLineJson> descriptionLinesOrEmpty() {
        return WTraderJsonValues.listOrEmpty(descriptionLines);
    }

    public List<String> groupsOrEmpty() {
        return WTraderJsonValues.listOrEmpty(groups);
    }

    public List<String> aliasesOrEmpty() {
        return WTraderJsonValues.listOrEmpty(aliases);
    }
}
