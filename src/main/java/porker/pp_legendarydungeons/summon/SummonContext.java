package porker.pp_legendarydungeons.summon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

/**
 * SummonContext stores all information about a specific summon attempt.
 */
public record SummonContext(
        ServerLevel level,
        ServerPlayer player,

        /**
         * Main nearby marker.
         *
         * For entity-based summons, this is usually:
         * pp_legendary_summon
         */
        ArmorStand summonMarker,

        /**
         * Pokemon-specific condition marker.
         *
         * For Rayquaza:
         * pp_rayquaza_conditions
         */
        ArmorStand conditionMarker,

        /**
         * Pokemon-specific spawn marker.
         *
         * For Rayquaza:
         * pp_summon_rayquaza
         *
         * This can be null if no separate spawn marker exists.
         */
        ArmorStand spawnMarker,

        /**
         * Final block position where the Pokemon should spawn.
         *
         * If spawnMarker exists, this is spawnMarker.blockPosition().
         * If no spawnMarker exists, this can fall back to conditionMarker.blockPosition().
         */
        BlockPos spawnPos,

        SummonTriggerType triggerType
) {
    /**
     * Creates a copy of this context with condition/spawn data filled in.
     */
    public SummonContext withConditionAndSpawn(
            ArmorStand newConditionMarker,
            ArmorStand newSpawnMarker,
            BlockPos newSpawnPos
    ) {
        return new SummonContext(
                level,
                player,
                summonMarker,
                newConditionMarker,
                newSpawnMarker,
                newSpawnPos,
                triggerType
        );
    }
}