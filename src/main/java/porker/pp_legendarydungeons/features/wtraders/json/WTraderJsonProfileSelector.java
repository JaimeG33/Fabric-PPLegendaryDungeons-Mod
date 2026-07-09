package porker.pp_legendarydungeons.features.wtraders.json;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Selects JSON-loaded trader profiles using the loaded selection table.
 *
 * This intentionally does not spawn anything. It only chooses which loaded
 * TraderProfileJson should be used.
 */
public final class WTraderJsonProfileSelector {
    private static final ResourceLocation DEFAULT_SELECTION_TABLE = ResourceLocation.fromNamespaceAndPath(
            ProfessorPorkersLegendaryDungeons.MOD_ID,
            "default"
    );

    private WTraderJsonProfileSelector() {
    }

    public static Optional<TraderProfileJson> pickNoMapProfile(RandomSource random) {
        Optional<SelectionTableJson> table = WTraderJsonRegistry.getSelectionTable(DEFAULT_SELECTION_TABLE);

        if (table.isPresent()) {
            Optional<TraderProfileJson> fromTable = pickFromWeightedProfileRefs(
                    random,
                    table.get().customNoMapProfilesOrEmpty(),
                    WTraderJsonTraderTypes.CUSTOM_NO_MAP
            );

            if (fromTable.isPresent()) {
                return fromTable;
            }
        }

        return pickAnyLoadedProfileOfType(random, WTraderJsonTraderTypes.CUSTOM_NO_MAP);
    }

    public static Optional<TraderProfileJson> pickMapProfile(RandomSource random) {
        Optional<SelectionTableJson> table = WTraderJsonRegistry.getSelectionTable(DEFAULT_SELECTION_TABLE);

        if (table.isPresent()) {
            Optional<TraderProfileJson> fromTable = pickMapProfileFromGroups(
                    random,
                    table.get().customMapProfileGroupsOrEmpty()
            );

            if (fromTable.isPresent()) {
                return fromTable;
            }
        }

        return pickAnyLoadedProfileOfType(random, WTraderJsonTraderTypes.CUSTOM_MAP);
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
                validGroups.add(new ResolvedProfileGroup(group.weightOrDefault(0), profiles));
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
        List<ResolvedProfile> profiles = resolveWeightedProfileRefs(profileRefs, requiredTraderType);

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
                ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                        "[WTrader JSON] Selection table has invalid profile id: {}",
                        ref.profile
                );
                continue;
            }

            Optional<TraderProfileJson> profile = WTraderJsonRegistry.getTraderProfile(profileId);

            if (profile.isEmpty()) {
                ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                        "[WTrader JSON] Selection table referenced missing trader profile {}.",
                        profileId
                );
                continue;
            }

            if (!requiredTraderType.equals(profile.get().traderType)) {
                ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                        "[WTrader JSON] Selection table profile {} has trader_type {}, but expected {}.",
                        profileId,
                        profile.get().traderType,
                        requiredTraderType
                );
                continue;
            }

            resolved.add(new ResolvedProfile(profile.get(), ref.weightOrDefault(0)));
        }

        return resolved;
    }

    private static Optional<TraderProfileJson> pickAnyLoadedProfileOfType(RandomSource random, String traderType) {
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

    private record ResolvedProfile(TraderProfileJson profile, int weight) {
    }

    private record ResolvedProfileGroup(int weight, List<ResolvedProfile> profiles) {
    }
}
