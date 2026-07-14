package porker.pp_legendarydungeons.features.wtraders.json;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Selects JSON-loaded trader profiles and top-level trader results.
 *
 * <p>The no-argument profile methods preserve the original structure-trader
 * behavior by using the default table and falling back to any loaded profile of
 * the requested type. The overloads that accept a table id are strict: they use
 * only that selection table and return empty when it is missing or invalid.</p>
 */
public final class WTraderJsonProfileSelector {
    public static final ResourceLocation DEFAULT_SELECTION_TABLE = ResourceLocation.fromNamespaceAndPath(
            LegendaryDungeons.MOD_ID,
            "default"
    );

    private WTraderJsonProfileSelector() {
    }

    /**
     * Rolls one top-level result such as vanilla_wandering_trader,
     * custom_no_map, or custom_map from a specific selection table.
     */
    public static Optional<String> pickTopLevelResult(
            RandomSource random,
            ResourceLocation selectionTableId
    ) {
        Optional<SelectionTableJson> table = WTraderJsonRegistry.getSelectionTable(selectionTableId);

        if (table.isEmpty()) {
            return Optional.empty();
        }

        List<WeightedResultJson> validResults = new ArrayList<>();

        for (WeightedResultJson result : table.get().topLevelRollsOrEmpty()) {
            if (result == null || !result.hasResult() || result.weightOrDefault(0) <= 0) {
                continue;
            }

            validResults.add(result);
        }

        return WTraderWeightedPicker.pick(
                random,
                validResults,
                result -> result.weightOrDefault(0)
        ).map(result -> result.result);
    }

    /**
     * Original structure-trader selector. It keeps the old fallback behavior.
     */
    public static Optional<TraderProfileJson> pickNoMapProfile(RandomSource random) {
        Optional<TraderProfileJson> fromDefault = pickNoMapProfile(
                random,
                DEFAULT_SELECTION_TABLE
        );

        if (fromDefault.isPresent()) {
            return fromDefault;
        }

        return pickAnyLoadedProfileOfType(
                random,
                WTraderJsonTraderTypes.CUSTOM_NO_MAP
        );
    }

    /**
     * Strict no-map selection from one named selection table.
     */
    public static Optional<TraderProfileJson> pickNoMapProfile(
            RandomSource random,
            ResourceLocation selectionTableId
    ) {
        return WTraderJsonRegistry
                .getSelectionTable(selectionTableId)
                .flatMap(table -> pickFromWeightedProfileRefs(
                        random,
                        table.customNoMapProfilesOrEmpty(),
                        WTraderJsonTraderTypes.CUSTOM_NO_MAP
                ));
    }

    /**
     * Original structure-trader selector. It keeps the old fallback behavior.
     */
    public static Optional<TraderProfileJson> pickMapProfile(RandomSource random) {
        Optional<TraderProfileJson> fromDefault = pickMapProfile(
                random,
                DEFAULT_SELECTION_TABLE
        );

        if (fromDefault.isPresent()) {
            return fromDefault;
        }

        return pickAnyLoadedProfileOfType(
                random,
                WTraderJsonTraderTypes.CUSTOM_MAP
        );
    }

    /**
     * Strict map-profile selection from one named selection table.
     */
    public static Optional<TraderProfileJson> pickMapProfile(
            RandomSource random,
            ResourceLocation selectionTableId
    ) {
        return WTraderJsonRegistry
                .getSelectionTable(selectionTableId)
                .flatMap(table -> pickMapProfileFromGroups(
                        random,
                        table.customMapProfileGroupsOrEmpty()
                ));
    }

    private static Optional<TraderProfileJson> pickMapProfileFromGroups(
            RandomSource random,
            List<WeightedProfileGroupJson> groups
    ) {
        List<ResolvedProfileGroup> validGroups = new ArrayList<>();

        for (WeightedProfileGroupJson group : groups) {
            if (group == null || group.weightOrDefault(0) <= 0) {
                continue;
            }

            List<ResolvedProfile> profiles = resolveWeightedProfileRefs(
                    group.profilesOrEmpty(),
                    WTraderJsonTraderTypes.CUSTOM_MAP
            );

            if (!profiles.isEmpty()) {
                validGroups.add(new ResolvedProfileGroup(
                        group.weightOrDefault(0),
                        profiles
                ));
            }
        }

        Optional<ResolvedProfileGroup> pickedGroup = WTraderWeightedPicker.pick(
                random,
                validGroups,
                ResolvedProfileGroup::weight
        );

        if (pickedGroup.isEmpty()) {
            return Optional.empty();
        }

        return WTraderWeightedPicker.pick(
                random,
                pickedGroup.get().profiles(),
                ResolvedProfile::weight
        ).map(ResolvedProfile::profile);
    }

    private static Optional<TraderProfileJson> pickFromWeightedProfileRefs(
            RandomSource random,
            List<WeightedProfileJson> profileRefs,
            String requiredTraderType
    ) {
        List<ResolvedProfile> profiles = resolveWeightedProfileRefs(
                profileRefs,
                requiredTraderType
        );

        return WTraderWeightedPicker.pick(
                random,
                profiles,
                ResolvedProfile::weight
        ).map(ResolvedProfile::profile);
    }

    private static List<ResolvedProfile> resolveWeightedProfileRefs(
            List<WeightedProfileJson> profileRefs,
            String requiredTraderType
    ) {
        List<ResolvedProfile> resolved = new ArrayList<>();

        for (WeightedProfileJson ref : profileRefs) {
            if (ref == null || !ref.hasProfile() || ref.weightOrDefault(0) <= 0) {
                continue;
            }

            ResourceLocation profileId;

            try {
                profileId = ResourceLocation.parse(ref.profile);
            } catch (Exception exception) {
                LegendaryDungeons.LOGGER.warn(
                        "[WTrader JSON] Selection table has invalid profile id: {}",
                        ref.profile
                );
                continue;
            }

            Optional<TraderProfileJson> profile = WTraderJsonRegistry.getTraderProfile(
                    profileId
            );

            if (profile.isEmpty()) {
                LegendaryDungeons.LOGGER.warn(
                        "[WTrader JSON] Selection table referenced missing trader profile {}.",
                        profileId
                );
                continue;
            }

            if (!requiredTraderType.equals(profile.get().traderType)) {
                LegendaryDungeons.LOGGER.warn(
                        "[WTrader JSON] Selection table profile {} has trader_type {}, but expected {}.",
                        profileId,
                        profile.get().traderType,
                        requiredTraderType
                );
                continue;
            }

            resolved.add(new ResolvedProfile(
                    profile.get(),
                    ref.weightOrDefault(0)
            ));
        }

        return resolved;
    }

    private static Optional<TraderProfileJson> pickAnyLoadedProfileOfType(
            RandomSource random,
            String traderType
    ) {
        List<ResolvedProfile> profiles = new ArrayList<>();

        for (TraderProfileJson profile : WTraderJsonRegistry.traderProfiles().values()) {
            if (profile == null) {
                continue;
            }

            if (traderType.equals(profile.traderType)) {
                profiles.add(new ResolvedProfile(profile, 1));
            }
        }

        return WTraderWeightedPicker.pick(
                random,
                profiles,
                ResolvedProfile::weight
        ).map(ResolvedProfile::profile);
    }

    private record ResolvedProfile(
            TraderProfileJson profile,
            int weight
    ) {
    }

    private record ResolvedProfileGroup(
            int weight,
            List<ResolvedProfile> profiles
    ) {
    }
}
