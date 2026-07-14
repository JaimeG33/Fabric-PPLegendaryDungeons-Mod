
package porker.pp_legendarydungeons.summon.dungeon_completion;

import net.minecraft.server.MinecraftServer;

public interface DungeonCompletionCondition {
    String id();

    CompletionCheckResult evaluate(
            MinecraftServer server,
            DungeonCompletionWatch watch
    );
}
