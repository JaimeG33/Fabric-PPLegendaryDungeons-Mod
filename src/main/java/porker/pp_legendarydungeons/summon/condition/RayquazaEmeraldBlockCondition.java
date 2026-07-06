package porker.pp_legendarydungeons.summon.condition;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.summon.LegendarySummonDefinition;
import porker.pp_legendarydungeons.summon.SummonContext;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Rayquaza-specific condition logic.
 *
 * This class checks your desired Rayquaza setup:
 *
 * 1. Start from the nearby pp_legendary_summon marker.
 * 2. Search near it for an armor stand tagged pp_rayquaza_conditions.
 * 3. Check if that condition armor stand is holding an emerald block.
 * 4. Search near the condition marker for pp_summon_rayquaza.
 * 5. If found, Rayquaza spawns there.
 * 6. If not found, Rayquaza spawns at pp_rayquaza_conditions as fallback.
 */
public final class RayquazaEmeraldBlockCondition implements LegendarySummonCondition {
    @Override
    public Optional<SummonContext> prepareContext(
            SummonContext context,
            LegendarySummonDefinition definition
    ) {
        // The main marker is the armor stand tagged pp_legendary_summon.
        ArmorStand summonMarker = context.summonMarker();

        // Find the nearest Rayquaza condition marker near the main summon marker.
        Optional<ArmorStand> conditionMarker = findNearestConditionMarker(
                context,
                definition,
                summonMarker
        );

        // No pp_rayquaza_conditions marker found nearby, so Rayquaza cannot summon.
        if (conditionMarker.isEmpty()) {
            return Optional.empty();
        }

        ArmorStand rayquazaConditionMarker = conditionMarker.get();

        // The condition marker must be holding an emerald block.
        // This is your current test trigger for Rayquaza.
        if (!hasEmeraldBlockInHand(rayquazaConditionMarker)) {
            return Optional.empty();
        }

        // Search for the preferred final spawn marker:
        // pp_summon_rayquaza
        Optional<ArmorStand> spawnMarker = findNearestSpawnMarker(
                context,
                definition,
                rayquazaConditionMarker
        );

        // If pp_summon_rayquaza exists, use that position.
        // Otherwise, fall back to the pp_rayquaza_conditions marker position.
        BlockPos spawnPos = spawnMarker
                .map(ArmorStand::blockPosition)
                .orElse(rayquazaConditionMarker.blockPosition());

        // Return a new context with the condition marker and final spawn position filled in.
        return Optional.of(
                context.withConditionAndSpawn(rayquazaConditionMarker, spawnPos)
        );
    }

    /**
     * Finds the nearest armor stand tagged pp_rayquaza_conditions near the main pp_legendary_summon marker.
     */
    private Optional<ArmorStand> findNearestConditionMarker(
            SummonContext context,
            LegendarySummonDefinition definition,
            ArmorStand summonMarker
    ) {
        double radius = definition.conditionSearchRadius();

        // Build a search box around the main summon marker.
        AABB searchBox = summonMarker.getBoundingBox().inflate(radius);

        List<ArmorStand> matches = context.level().getEntitiesOfClass(
                ArmorStand.class,
                searchBox,
                armorStand ->
                        armorStand.isAlive()

                                // This will be pp_rayquaza_conditions for Rayquaza.
                                && armorStand.getTags().contains(definition.conditionTag())

                                // Prevent reusing the same condition marker after a successful summon.
                                && !armorStand.getTags().contains(definition.usedTag())
        );

        // If more than one condition marker is nearby, choose the closest one.
        return matches.stream()
                .min(Comparator.comparingDouble(
                        armorStand -> armorStand.distanceToSqr(summonMarker)
                ));
    }

    /**
     * Finds the nearest armor stand tagged pp_summon_rayquaza near the condition marker.
     */
    private Optional<ArmorStand> findNearestSpawnMarker(
            SummonContext context,
            LegendarySummonDefinition definition,
            ArmorStand conditionMarker
    ) {
        double radius = definition.spawnMarkerSearchRadius();

        // Search around the Rayquaza condition marker.
        AABB searchBox = conditionMarker.getBoundingBox().inflate(radius);

        List<ArmorStand> matches = context.level().getEntitiesOfClass(
                ArmorStand.class,
                searchBox,
                armorStand ->
                        armorStand.isAlive()

                                // This will be pp_summon_rayquaza for Rayquaza.
                                && armorStand.getTags().contains(definition.spawnMarkerTag())
        );

        // If multiple spawn markers exist, choose the closest one to the condition marker.
        return matches.stream()
                .min(Comparator.comparingDouble(
                        armorStand -> armorStand.distanceToSqr(conditionMarker)
                ));
    }

    /**
     * Checks either hand of the armor stand for an emerald block.
     *
     * This allows you to put the item in main hand or offhand.
     */
    private boolean hasEmeraldBlockInHand(ArmorStand armorStand) {
        return armorStand.getMainHandItem().is(Items.EMERALD_BLOCK)
                || armorStand.getOffhandItem().is(Items.EMERALD_BLOCK);
    }
}