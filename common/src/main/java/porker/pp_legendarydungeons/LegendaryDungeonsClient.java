package porker.pp_legendarydungeons;

import porker.pp_legendarydungeons.client.animated_blocks.AnimatedBlockEntityRenderers;
import porker.pp_legendarydungeons.dungeon_rules.client.DungeonRuleClientNetworking;

/**
 * Loader-neutral client initialization boundary.
 *
 * <p>Fabric and the NeoForge client bootstrap both call {@link #init()}.
 * This class must never be referenced from dedicated-server initialization.</p>
 */
public final class LegendaryDungeonsClient {
    private static boolean initialized = false;

    private LegendaryDungeonsClient() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;
        AnimatedBlockEntityRenderers.register();
        DungeonRuleClientNetworking.register();
    }
}
