package porker.pp_legendarydungeons.summon.trigger;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.summon.LegendarySummonService;
import porker.pp_legendarydungeons.summon.SummonContext;
import porker.pp_legendarydungeons.summon.SummonTriggerType;

import java.util.List;

/**
 * EntityLegendarySummonTicker is the central entity-based summon scanner.
 *
 * It does NOT know anything specific about Rayquaza.
 *
 * Its only job is:
 * 1. Once per second, check all loaded server worlds.
 * 2. For each player, search within 10 blocks.
 * 3. Find armor stands tagged pp_legendary_summon.
 * 4. Create a generic SummonContext.
 * 5. Pass that context to LegendarySummonService.
 *
 * This keeps the scanner lightweight and reusable.
 */
public final class EntityLegendarySummonTicker {
    /**
     * Main identifier for entity-based legendary summon areas.
     *
     * Your structures should include an armor stand with this tag.
     */
    private static final String LEGENDARY_SUMMON_TAG = "pp_legendary_summon";

    /**
     * Player must be within this many blocks of pp_legendary_summon to begin the check.
     */
    private static final double PLAYER_TRIGGER_DISTANCE = 20.0D;

    /**
     * Squared distance is used because Minecraft distance checks often avoid square roots for performance.
     */
    private static final double PLAYER_TRIGGER_DISTANCE_SQUARED =
            PLAYER_TRIGGER_DISTANCE * PLAYER_TRIGGER_DISTANCE;

    /**
     * Counts server ticks.
     *
     * Minecraft runs 20 ticks per second.
     * Checking every tick is unnecessary here, so we check every 20 ticks instead.
     */
    private static int ticks = 0;

    private EntityLegendarySummonTicker() {
    }

    /**
     * Registers this ticker with Fabric's server tick event.
     *
     * This method must be called from your mod initializer.
     */
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ticks++;

            // Only run once per second.
            if (ticks % 20 != 0) {
                return;
            }

            // Check every loaded dimension/server level.
            for (ServerLevel level : server.getAllLevels()) {
                checkLevel(level);
            }
        });
    }

    /**
     * Checks one server level for players near pp_legendary_summon markers.
     */
    private static void checkLevel(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            // Search around the player, not the whole world.
            // This is much better for performance.
            AABB searchBox = player.getBoundingBox().inflate(PLAYER_TRIGGER_DISTANCE);

            List<ArmorStand> summonMarkers = level.getEntitiesOfClass(
                    ArmorStand.class,
                    searchBox,
                    armorStand ->
                            armorStand.isAlive()

                                    // Main marker tag for all entity-based legendary summons.
                                    && armorStand.getTags().contains(LEGENDARY_SUMMON_TAG)

                                    // Confirm it is actually within 10 blocks.
                                    && armorStand.distanceToSqr(player) <= PLAYER_TRIGGER_DISTANCE_SQUARED
            );

            for (ArmorStand summonMarker : summonMarkers) {
                // Build a generic context.
                //
                // At this stage, we only know:
                // - player is near pp_legendary_summon
                // - this is an entity proximity trigger
                //
                // Rayquaza-specific logic happens later inside RayquazaEmeraldBlockCondition.
                SummonContext context = new SummonContext(
                        level,
                        player,
                        summonMarker,
                        null,
                        summonMarker.blockPosition(),
                        SummonTriggerType.ENTITY_PROXIMITY
                );

                LegendarySummonService.trySummon(context);
            }
        }
    }
}