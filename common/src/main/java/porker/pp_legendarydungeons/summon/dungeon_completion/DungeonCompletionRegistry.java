
package porker.pp_legendarydungeons.summon.dungeon_completion;

import porker.pp_legendarydungeons.summon.dungeon_completion.rayquaza.RayquazaCompletionCondition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DungeonCompletionRegistry {
    private static final Map<String, DungeonCompletionCondition> CONDITIONS = new HashMap<>();
    private static boolean bootstrapped;

    private DungeonCompletionRegistry() {
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }

        register(new RayquazaCompletionCondition());
        bootstrapped = true;
    }

    private static void register(DungeonCompletionCondition condition) {
        CONDITIONS.put(condition.id(), condition);
    }

    public static Optional<DungeonCompletionCondition> get(String id) {
        bootstrap();
        return Optional.ofNullable(CONDITIONS.get(id));
    }
}
