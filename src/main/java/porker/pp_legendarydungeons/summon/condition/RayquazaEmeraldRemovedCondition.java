package porker.pp_legendarydungeons.summon.condition;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.setup.ModScoreboards;
import porker.pp_legendarydungeons.summon.LegendarySummonDefinition;
import porker.pp_legendarydungeons.summon.SummonContext;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Normal Rayquaza condition logic.
 *
 * Current behavior:
 *
 * The structure is expected to spawn with the pp_rayquaza_conditions armor stand
 * holding an emerald block.
 *
 * Normal Rayquaza does NOT summon immediately when the emerald block is removed.
 * Instead, the condition stand must have empty hands for a few check cycles.
 *
 * This gives players enough time to swap the emerald block for another item,
 * such as a nether star for the secret Rayquaza summon.
 *
 * Normal Rayquaza requires:
 * - pp_rayquaza_conditions exists near pp_legendary_summon
 * - event_triggered < 1 on pp_rayquaza_conditions
 * - the nearby player is NOT holding the secret unlock score scs_secrets >= 10
 * - pp_rayquaza_conditions has empty hands
 * - pp_timer reaches EMPTY_HAND_TIMER_THRESHOLD
 * - spawn_once < 1 on pp_summon_rayquaza, or fallback marker
 */
public final class RayquazaEmeraldRemovedCondition implements LegendarySummonCondition {
    /**
     * Your entity summon ticker currently runs once per second.
     *
     * A value of 3 means the condition stand must stay empty for about 3 seconds
     * before normal Rayquaza summons.
     */
    private static final int EMPTY_HAND_TIMER_THRESHOLD = 3;

    /**
     * A player with this score has completed the secret sequence and should be
     * allowed time to place the nether star without accidentally triggering the
     * normal empty-hand Rayquaza summon.
     */
    private static final int SECRET_UNLOCK_SCORE = 10;

    @Override
    public Optional<SummonContext> prepareContext(
            SummonContext context,
            LegendarySummonDefinition definition
    ) {
        ArmorStand summonMarker = context.summonMarker();

        Optional<ArmorStand> conditionMarker = findNearestConditionMarker(
                context,
                definition,
                summonMarker
        );

        if (conditionMarker.isEmpty()) {
            return Optional.empty();
        }

        ArmorStand rayquazaConditionMarker = conditionMarker.get();

        /*
         * event_triggered must be absent, 0, or below 1.
         *
         * If it is already 1 or higher, this structure has already triggered
         * normal Rayquaza.
         *
         * Important:
         * This should only represent the normal Rayquaza summon.
         * Secret Rayquaza should use its own secret-specific lock instead.
         */
        if (!ModScoreboards.entityScoreLessThan(
                rayquazaConditionMarker,
                ModScoreboards.EVENT_TRIGGERED,
                1
        )) {
            return Optional.empty();
        }

        /*
         * If the nearby player has completed the secret sequence, pause the
         * normal empty-hand summon.
         *
         * This prevents the normal Rayquaza timer from advancing while the player
         * is trying to swap the emerald block for the nether star.
         *
         * After secret Rayquaza successfully spawns, RayquazaSecretAftermath should
         * reset the player's scs_secrets score back to 0, allowing the normal
         * summon to be used later if it has not already been used.
         */
        if (context.player() != null) {
            int playerSecretScore = ModScoreboards.getEntityScore(
                    context.player(),
                    ModScoreboards.SCS_SECRETS
            );

            if (playerSecretScore >= SECRET_UNLOCK_SCORE) {
                ModScoreboards.setEntityScore(
                        rayquazaConditionMarker,
                        ModScoreboards.PP_TIMER,
                        0
                );

                return Optional.empty();
            }
        }

        /*
         * If the condition marker is holding any item, normal Rayquaza should
         * not summon.
         *
         * This includes:
         * - emerald block still present
         * - nether star for the secret summon
         * - any other item
         *
         * Reset pp_timer because the stand is not empty.
         */
        if (!hasEmptyHands(rayquazaConditionMarker)) {
            ModScoreboards.setEntityScore(
                    rayquazaConditionMarker,
                    ModScoreboards.PP_TIMER,
                    0
            );

            return Optional.empty();
        }

        /*
         * The stand is empty, so increase pp_timer.
         *
         * Once it has been empty long enough, normal Rayquaza can summon.
         */
        int timer = ModScoreboards.getEntityScore(
                rayquazaConditionMarker,
                ModScoreboards.PP_TIMER
        );

        timer++;

        ModScoreboards.setEntityScore(
                rayquazaConditionMarker,
                ModScoreboards.PP_TIMER,
                timer
        );

        if (timer < EMPTY_HAND_TIMER_THRESHOLD) {
            return Optional.empty();
        }

        Optional<ArmorStand> spawnMarker = findNearestSpawnMarker(
                context,
                definition,
                rayquazaConditionMarker
        );

        /*
         * If a pp_summon_rayquaza marker exists, use it.
         * If not, fall back to the condition marker.
         */
        ArmorStand spawnScoreMarker = spawnMarker.orElse(rayquazaConditionMarker);

        /*
         * spawn_once must also be absent, 0, or below 1.
         *
         * This prevents the same spawn marker from producing normal Rayquaza again.
         *
         * Important:
         * This should only represent the normal Rayquaza summon.
         * Secret Rayquaza should not rely on this same spawn_once lock if you want
         * both normal and secret Rayquaza to be available once each.
         */
        if (!ModScoreboards.entityScoreLessThan(
                spawnScoreMarker,
                ModScoreboards.SPAWN_ONCE,
                1
        )) {
            return Optional.empty();
        }

        BlockPos spawnPos = spawnScoreMarker.blockPosition();

        return Optional.of(
                context.withConditionAndSpawn(
                        rayquazaConditionMarker,
                        spawnMarker.orElse(null),
                        spawnPos
                )
        );
    }

    /**
     * Finds the nearest armor stand tagged pp_rayquaza_conditions near the main
     * pp_legendary_summon marker.
     */
    private Optional<ArmorStand> findNearestConditionMarker(
            SummonContext context,
            LegendarySummonDefinition definition,
            ArmorStand summonMarker
    ) {
        double radius = definition.conditionSearchRadius();

        AABB searchBox = summonMarker.getBoundingBox().inflate(radius);

        List<ArmorStand> matches = context.level().getEntitiesOfClass(
                ArmorStand.class,
                searchBox,
                armorStand ->
                        armorStand.isAlive()
                                && armorStand.getTags().contains(definition.conditionTag())

                                /*
                                 * Backup repeat prevention.
                                 *
                                 * For the normal Rayquaza definition, this used tag should be:
                                 * pp_rayquaza_summoned
                                 *
                                 * This should not block secret Rayquaza, because secret Rayquaza
                                 * has its own used tag:
                                 * pp_rayquaza_secret_summoned
                                 */
                                && !armorStand.getTags().contains(definition.usedTag())
        );

        return matches.stream()
                .min(Comparator.comparingDouble(
                        armorStand -> armorStand.distanceToSqr(summonMarker)
                ));
    }

    /**
     * Finds the nearest armor stand tagged pp_summon_rayquaza near the
     * pp_rayquaza_conditions marker.
     */
    private Optional<ArmorStand> findNearestSpawnMarker(
            SummonContext context,
            LegendarySummonDefinition definition,
            ArmorStand conditionMarker
    ) {
        double radius = definition.spawnMarkerSearchRadius();

        AABB searchBox = conditionMarker.getBoundingBox().inflate(radius);

        List<ArmorStand> matches = context.level().getEntitiesOfClass(
                ArmorStand.class,
                searchBox,
                armorStand ->
                        armorStand.isAlive()
                                && armorStand.getTags().contains(definition.spawnMarkerTag())
        );

        return matches.stream()
                .min(Comparator.comparingDouble(
                        armorStand -> armorStand.distanceToSqr(conditionMarker)
                ));
    }

    /**
     * Normal Rayquaza only triggers when both hands are empty.
     *
     * This prevents the secret nether star summon from accidentally triggering
     * the normal summon.
     */
    private boolean hasEmptyHands(ArmorStand armorStand) {
        return armorStand.getMainHandItem().isEmpty()
                && armorStand.getOffhandItem().isEmpty();
    }
}