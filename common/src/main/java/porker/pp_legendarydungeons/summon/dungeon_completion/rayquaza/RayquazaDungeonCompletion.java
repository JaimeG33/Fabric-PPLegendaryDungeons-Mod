
package porker.pp_legendarydungeons.summon.dungeon_completion.rayquaza;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonInstanceRecord;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonRuleSavedData;
import porker.pp_legendarydungeons.dungeon_rules.preset.DungeonRulePresetRegistry;
import porker.pp_legendarydungeons.summon.SummonContext;
import porker.pp_legendarydungeons.summon.dungeon_completion.DungeonCompletionTracker;
import porker.pp_legendarydungeons.summon.dungeon_completion.DungeonCompletionWatch;

import java.util.Optional;
import java.util.UUID;

/**
 * Rayquaza-specific arming step called only after a successful normal summon.
 */
public final class RayquazaDungeonCompletion {
    private static final double COMPLETION_DISTANCE = 100.0D;
    private static final double PARENT_FALLBACK_DISTANCE = 512.0D;

    private RayquazaDungeonCompletion() {
    }

    public static void arm(
            SummonContext context,
            PokemonEntity spawnedRayquaza
    ) {
        ServerLevel level = context.level();
        BlockPos origin = context.spawnPos();

        Optional<String> instanceAtSpawn =
                DungeonRuleManager.findInstanceAt(level, origin);

        Optional<String> instanceId = instanceAtSpawn.isPresent()
                ? instanceAtSpawn
                : DungeonRuleSavedData
                .get(level.getServer())
                .findNearestActive(
                        level.dimension(),
                        origin,
                        DungeonRulePresetRegistry.SKY_PILLAR_ID,
                        0,
                        PARENT_FALLBACK_DISTANCE
                )
                .map(DungeonInstanceRecord::instanceId);

        if (instanceId.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "[Dungeon Completion] Rayquaza spawned at {}, but no active Sky Pillar dungeon instance was found.",
                    origin
            );
            return;
        }

        DungeonCompletionWatch watch = new DungeonCompletionWatch(
                UUID.randomUUID(),
                RayquazaCompletionCondition.ID,
                instanceId.get(),
                level.dimension().location().toString(),
                spawnedRayquaza.getUUID(),
                origin,
                COMPLETION_DISTANCE,
                spawnedRayquaza.blockPosition(),
                0,
                0
        );

        spawnedRayquaza.addTag("pp_dungeon_completion_tracked");
        DungeonCompletionTracker.add(level.getServer(), watch);

        LegendaryDungeons.LOGGER.info(
                "[Dungeon Completion] Armed Rayquaza completion watch {} for instance {}.",
                watch.watchId(),
                watch.instanceId()
        );
    }
}
