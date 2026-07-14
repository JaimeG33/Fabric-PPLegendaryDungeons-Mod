package porker.pp_legendarydungeons.summon.trigger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.summon.LegendarySummonService;
import porker.pp_legendarydungeons.summon.SummonContext;
import porker.pp_legendarydungeons.summon.SummonTriggerType;
import porker.pp_legendarydungeons.summon.dungeon_completion.DungeonCompletionTracker;

import java.util.List;

/**
 * Central entity-based summon scanner.
 *
 * Once per second it:
 * 1. Advances persistent dungeon-completion watches, independent of player proximity.
 * 2. Performs the existing nearby-player legendary summon marker scan.
 */
public final class EntityLegendarySummonTicker {
    private static final String LEGENDARY_SUMMON_TAG = "pp_legendary_summon";
    private static final double PLAYER_TRIGGER_DISTANCE = 20.0D;
    private static final double PLAYER_TRIGGER_DISTANCE_SQUARED =
            PLAYER_TRIGGER_DISTANCE * PLAYER_TRIGGER_DISTANCE;
    private static final int SCAN_INTERVAL_TICKS = 20;

    private EntityLegendarySummonTicker() {
    }

    /**
     * Called once per server tick by the shared Architectury scheduler.
     */
    public static void tick(MinecraftServer server, long tickCount) {
        if (tickCount % SCAN_INTERVAL_TICKS != 0L) {
            return;
        }

        /*
         * Completion checks are not nested inside the player/marker search.
         * A tracked encounter continues to resolve while its entity/chunk is loaded
         * even if nobody is near the original armor stand.
         */
        DungeonCompletionTracker.tick(server);

        for (ServerLevel level : server.getAllLevels()) {
            checkLevel(level);
        }
    }

    private static void checkLevel(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            AABB searchBox = player.getBoundingBox().inflate(PLAYER_TRIGGER_DISTANCE);

            List<ArmorStand> summonMarkers = level.getEntitiesOfClass(
                    ArmorStand.class,
                    searchBox,
                    armorStand ->
                            armorStand.isAlive()
                                    && armorStand.getTags().contains(LEGENDARY_SUMMON_TAG)
                                    && armorStand.distanceToSqr(player)
                                    <= PLAYER_TRIGGER_DISTANCE_SQUARED
            );

            for (ArmorStand summonMarker : summonMarkers) {
                SummonContext context = new SummonContext(
                        level,
                        player,
                        summonMarker,
                        null,
                        null,
                        summonMarker.blockPosition(),
                        SummonTriggerType.ENTITY_PROXIMITY
                );

                LegendarySummonService.trySummon(context);
            }
        }
    }
}
