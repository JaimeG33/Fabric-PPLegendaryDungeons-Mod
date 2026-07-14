
package porker.pp_legendarydungeons.dungeon_rules.instance;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;

import java.util.Optional;

/**
 * Shared API for completing/reactivating dungeon instances.
 *
 * Legendary summons, boss deaths, puzzles, and commands can all call the same
 * methods instead of knowing how rule blocks are cached.
 */
public final class DungeonCompletionService {
    private DungeonCompletionService() {
    }

    public static boolean completeInstance(MinecraftServer server, String instanceId) {
        boolean changed = DungeonRuleSavedData
                .get(server)
                .setState(instanceId, DungeonInstanceState.COMPLETED);

        if (changed) {
            DungeonRuleManager.onInstanceStateChanged(server, instanceId);
            LegendaryDungeons.LOGGER.info(
                    "[Dungeon Rules] Completed dungeon instance {}.",
                    instanceId
            );
        }

        return changed;
    }

    public static boolean reactivateInstance(MinecraftServer server, String instanceId) {
        boolean changed = DungeonRuleSavedData
                .get(server)
                .setState(instanceId, DungeonInstanceState.ACTIVE);

        if (changed) {
            DungeonRuleManager.onInstanceStateChanged(server, instanceId);
            LegendaryDungeons.LOGGER.info(
                    "[Dungeon Rules] Reactivated dungeon instance {}.",
                    instanceId
            );
        }

        return changed;
    }

    public static Optional<String> instanceAt(ServerLevel level, BlockPos pos) {
        Optional<String> loadedZoneInstance = DungeonRuleManager.findInstanceAt(level, pos);

        if (loadedZoneInstance.isPresent()) {
            return loadedZoneInstance;
        }

        return DungeonRuleSavedData
                .get(level.getServer())
                .findNearestAnyPreset(level.dimension(), pos, 512.0D)
                .map(DungeonInstanceRecord::instanceId);
    }
}
