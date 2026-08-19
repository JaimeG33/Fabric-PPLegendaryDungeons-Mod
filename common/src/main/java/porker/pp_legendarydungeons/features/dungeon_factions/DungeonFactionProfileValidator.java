package porker.pp_legendarydungeons.features.dungeon_factions;

import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.HashSet;
import java.util.Set;

/**
 * Validation boundary for reloadable dungeon faction definitions.
 */
public final class DungeonFactionProfileValidator {
    private DungeonFactionProfileValidator() {
    }

    public static boolean validate(
            ResourceLocation fileId,
            ResourceLocation factionId,
            DungeonFactionProfileJson profile
    ) {
        if (profile == null) {
            return false;
        }

        if (profile.hostile_factions == null) {
            return true;
        }

        Set<ResourceLocation> seen = new HashSet<>();

        for (String configured : profile.hostile_factions) {
            if (configured == null || configured.isBlank()) {
                return invalid(fileId, factionId, "hostile_factions cannot contain a blank ID.");
            }

            ResourceLocation hostileId;

            try {
                hostileId = ResourceLocation.parse(configured.trim());
            } catch (Exception exception) {
                return invalid(
                        fileId,
                        factionId,
                        "invalid hostile faction resource ID: " + configured
                );
            }

            if (hostileId.equals(factionId)) {
                return invalid(
                        fileId,
                        factionId,
                        "a faction cannot list itself as hostile."
                );
            }

            if (!seen.add(hostileId)) {
                return invalid(
                        fileId,
                        factionId,
                        "duplicate hostile faction: " + hostileId
                );
            }
        }

        return true;
    }

    private static boolean invalid(
            ResourceLocation fileId,
            ResourceLocation factionId,
            String reason
    ) {
        LegendaryDungeons.LOGGER.warn(
                "[Dungeon Factions] Ignoring invalid faction {} from {}: {}",
                factionId,
                fileId,
                reason
        );
        return false;
    }
}
