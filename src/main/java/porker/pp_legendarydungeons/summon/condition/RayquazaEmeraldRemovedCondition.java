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
 * Rayquaza condition logic.
 *
 * New behavior:
 *
 * The structure starts with pp_rayquaza_conditions holding an emerald block.
 * Rayquaza only summons after the emerald block is removed and the marker's hands are empty.
 *
 * It also checks scoreboard values:
 *
 * event_triggered < 1 on pp_rayquaza_conditions
 * spawn_once < 1 on pp_summon_rayquaza, or fallback marker
 */
public final class RayquazaEmeraldRemovedCondition implements LegendarySummonCondition {
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
         * Rayquaza's normal summon.
         */
        if (!ModScoreboards.entityScoreLessThan(
                rayquazaConditionMarker,
                ModScoreboards.EVENT_TRIGGERED,
                1
        )) {
            return Optional.empty();
        }

        /*
         * The new normal-Rayquaza trigger:
         *
         * The condition marker must have empty hands.
         *
         * This means the structure can start with an emerald block in the hand,
         * and removing that emerald block activates the normal Rayquaza summon.
         *
         * Future special forms can check for specific held items instead.
         */
        if (!hasEmptyHands(rayquazaConditionMarker)) {
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
         * This prevents the same spawn marker from producing Rayquaza again.
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
     * Finds the nearest pp_rayquaza_conditions marker near pp_legendary_summon.
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
                                 * This tag check is kept as a backup repeat-prevention layer.
                                 * The scoreboard is the main datapack-style state check.
                                 */
                                && !armorStand.getTags().contains(definition.usedTag())
        );

        return matches.stream()
                .min(Comparator.comparingDouble(
                        armorStand -> armorStand.distanceToSqr(summonMarker)
                ));
    }

    /**
     * Finds the nearest pp_summon_rayquaza marker near pp_rayquaza_conditions.
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
     * Normal Rayquaza currently triggers when both hands are empty.
     *
     * This is more future-proof than checking "not emerald block",
     * because future special forms can use other held items without accidentally
     * triggering normal Rayquaza.
     */
    private boolean hasEmptyHands(ArmorStand armorStand) {
        return armorStand.getMainHandItem().isEmpty()
                && armorStand.getOffhandItem().isEmpty();
    }
}