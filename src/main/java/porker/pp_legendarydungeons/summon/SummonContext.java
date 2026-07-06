package porker.pp_legendarydungeons.summon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

/**
 * SummonContext stores all information about a specific summon attempt.
 *
 * This is important because different trigger types may need different data.
 *
 * Example entity-based Rayquaza summon:
 * - level = the ServerLevel where the player and armor stands are
 * - player = the nearby player who activated the check
 * - summonMarker = the armor stand tagged pp_legendary_summon
 * - conditionMarker = the armor stand tagged pp_rayquaza_conditions
 * - spawnPos = the pp_summon_rayquaza position, or fallback position
 * - triggerType = ENTITY_PROXIMITY
 *
 * Later, block-based summons can use the same pipeline by filling in different context data.
 */
public record SummonContext(
        /**
         * The server-side world/dimension where the summon is happening.
         */
        ServerLevel level,

        /**
         * The player who triggered the summon check.
         *
         * For entity proximity, this is the nearby player.
         * For block interaction later, this would be the player who clicked the block.
         */
        ServerPlayer player,

        /**
         * The main armor stand marker.
         *
         * In your system, this is the armor stand tagged:
         * pp_legendary_summon
         */
        ArmorStand summonMarker,

        /**
         * The armor stand that stores/checks this specific legendary's conditions.
         *
         * For Rayquaza, this is the armor stand tagged:
         * pp_rayquaza_conditions
         *
         * This starts as null and gets filled in by RayquazaEmeraldBlockCondition.
         */
        ArmorStand conditionMarker,

        /**
         * The final position where the Pokémon should spawn.
         *
         * For Rayquaza:
         * 1. Prefer nearest pp_summon_rayquaza armor stand.
         * 2. Fall back to pp_rayquaza_conditions armor stand.
         */
        BlockPos spawnPos,

        /**
         * Explains what kind of trigger started this summon attempt.
         */
        SummonTriggerType triggerType
) {
    /**
     * Creates a copy of the current context, but with the condition marker and spawn position filled in.
     *
     * Records are immutable, so instead of editing the old context, we create a new one.
     */
    public SummonContext withConditionAndSpawn(
            ArmorStand newConditionMarker,
            BlockPos newSpawnPos
    ) {
        return new SummonContext(
                level,
                player,
                summonMarker,
                newConditionMarker,
                newSpawnPos,
                triggerType
        );
    }
}