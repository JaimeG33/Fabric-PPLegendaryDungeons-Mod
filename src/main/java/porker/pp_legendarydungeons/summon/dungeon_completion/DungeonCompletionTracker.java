
package porker.pp_legendarydungeons.summon.dungeon_completion;

import net.minecraft.server.MinecraftServer;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonCompletionService;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonRuleSavedData;

import java.util.Collection;

/**
 * Runs once per second from EntityLegendarySummonTicker.
 *
 * It checks only active completion watches. It does not require any player to be
 * near the summon armor stand.
 */
public final class DungeonCompletionTracker {
    private DungeonCompletionTracker() {
    }

    public static void tick(MinecraftServer server) {
        DungeonCompletionRegistry.bootstrap();
        DungeonCompletionSavedData data = DungeonCompletionSavedData.get(server);
        Collection<DungeonCompletionWatch> watches = data.snapshot();

        for (DungeonCompletionWatch watch : watches) {
            boolean instanceStillActive = DungeonRuleSavedData
                    .get(server)
                    .get(watch.instanceId())
                    .map(record -> record.rulesAreActive())
                    .orElse(false);

            if (!instanceStillActive) {
                data.remove(watch.watchId());
                continue;
            }

            DungeonCompletionCondition condition = DungeonCompletionRegistry
                    .get(watch.conditionId())
                    .orElse(null);

            if (condition == null) {
                ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                        "[Dungeon Completion] Missing condition '{}' for watch {}.",
                        watch.conditionId(),
                        watch.watchId()
                );
                data.remove(watch.watchId());
                continue;
            }

            CompletionCheckResult result = condition.evaluate(server, watch);

            if (result == CompletionCheckResult.PENDING) {
                data.markChanged();
                continue;
            }

            if (result == CompletionCheckResult.COMPLETE) {
                DungeonCompletionService.completeInstance(
                        server,
                        watch.instanceId()
                );
            }

            data.remove(watch.watchId());
        }
    }

    public static void add(MinecraftServer server, DungeonCompletionWatch watch) {
        DungeonCompletionSavedData.get(server).add(watch);
    }
}
