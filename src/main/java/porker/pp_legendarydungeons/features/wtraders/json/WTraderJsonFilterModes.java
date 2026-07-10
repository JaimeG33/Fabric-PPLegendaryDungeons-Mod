package porker.pp_legendarydungeons.features.wtraders.json;

/**
 * Planned filter mode ids for category_filter_mode and tag_filter_mode.
 */
public final class WTraderJsonFilterModes {
    public static final String INCLUDE = "include";
    public static final String EXCLUDE = "exclude";

    /**
     * Tag-only mode:
     * Matches when at least one requested tag is present.
     *
     * Example:
     * tag_filter_mode = "include_any_of"
     * tags = ["tools", "cooking", "ocean"]
     *
     * This matches entries that have tools OR cooking OR ocean.
     */
    public static final String INCLUDE_ANY_OF = "include_any_of";

    private WTraderJsonFilterModes() {
    }
}