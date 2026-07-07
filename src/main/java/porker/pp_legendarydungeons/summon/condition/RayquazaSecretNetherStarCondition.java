package porker.pp_legendarydungeons.summon.condition;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.setup.ModScoreboards;
import porker.pp_legendarydungeons.summon.LegendarySummonDefinition;
import porker.pp_legendarydungeons.summon.SummonContext;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Secret Rayquaza summon condition.
 *
 * This is the top-of-tower part of the secret summon.
 *
 * This condition checks:
 * - a nearby pp_rayquaza_conditions marker exists
 * - the player has scs_secrets = 10
 * - pp_rayquaza_conditions is holding a nether star
 * - pp_rayquaza_conditions has not already used the secret summon
 * - pp_summon_rayquaza has not already spawned Rayquaza
 *
 * This does NOT check the bottom secret puzzle.
 * The bottom puzzle should be handled separately in the secrets folder,
 * and should give the player scs_secrets = 10 when completed.
 */
public final class RayquazaSecretNetherStarCondition implements LegendarySummonCondition {
    /**
     * The player must have this scs_secrets score to use the secret Rayquaza summon.
     *
     * This matches the old datapack behavior:
     * scoreboard players set <player> scs_secrets 10
     */
    private static final int REQUIRED_PLAYER_SECRET_SCORE = 10;

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
         * The nearby player must have completed the bottom secret sequence.
         *
         * This intentionally checks the player, not the tower-top armor stand.
         * That lets the bottom puzzle be far away from the top summon area.
         */
        int playerSecretScore = ModScoreboards.getEntityScore(
                context.player(),
                ModScoreboards.SCS_SECRETS
        );

        if (playerSecretScore != REQUIRED_PLAYER_SECRET_SCORE) {
            return Optional.empty();
        }

        /*
         * If event_triggered is already 1 or higher, this Rayquaza summon event
         * has already been used.
         *
         * This helps prevent the secret summon from happening after the normal
         * summon already happened.
         */
        if (!ModScoreboards.entityScoreLessThan(
                rayquazaConditionMarker,
                ModScoreboards.EVENT_TRIGGERED,
                1
        )) {
            return Optional.empty();
        }

        /*
         * scs_secrets on the condition marker is used as a parallel secret-specific
         * lock. This allows you to distinguish "secret already used" from other
         * event state later if needed.
         */
        if (!ModScoreboards.entityScoreLessThan(
                rayquazaConditionMarker,
                ModScoreboards.SCS_SECRETS,
                1
        )) {
            return Optional.empty();
        }

        /*
         * The actual top secret trigger:
         *
         * The player must place a nether star into the pp_rayquaza_conditions
         * armor stand's hand.
         */
        if (!hasNetherStar(rayquazaConditionMarker)) {
            return Optional.empty();
        }

        /*
         * Because a nether star is now present, reset pp_timer.
         *
         * This prevents the normal empty-hand Rayquaza summon from continuing
         * its timer after the player swaps the emerald block for the nether star.
         */
        ModScoreboards.setEntityScore(
                rayquazaConditionMarker,
                ModScoreboards.PP_TIMER,
                0
        );

        Optional<ArmorStand> spawnMarker = findNearestSpawnMarker(
                context,
                definition,
                rayquazaConditionMarker
        );

        /*
         * If pp_summon_rayquaza exists, use it.
         * If not, fall back to pp_rayquaza_conditions.
         */
        ArmorStand spawnScoreMarker = spawnMarker.orElse(rayquazaConditionMarker);

        /*
         * spawn_once prevents the same spawn marker from producing Rayquaza again.
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
                                 * Backup prevention for this specific secret definition.
                                 *
                                 * The scoreboard checks are the main prevention system,
                                 * but the used tag gives an extra layer of safety.
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
     * Checks either hand for a vanilla nether star.
     *
     * This is the item the player places into pp_rayquaza_conditions after
     * unlocking the secret at the bottom of the tower.
     */
    private boolean hasNetherStar(ArmorStand armorStand) {
        return armorStand.getMainHandItem().is(Items.NETHER_STAR)
                || armorStand.getOffhandItem().is(Items.NETHER_STAR);
    }
}