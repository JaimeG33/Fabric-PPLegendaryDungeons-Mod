
package porker.pp_legendarydungeons.summon.dungeon_completion.rayquaza;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import porker.pp_legendarydungeons.summon.dungeon_completion.CompletionCheckResult;
import porker.pp_legendarydungeons.summon.dungeon_completion.DungeonCompletionCondition;
import porker.pp_legendarydungeons.summon.dungeon_completion.DungeonCompletionWatch;

/**
 * Completes Sky Pillar when the exact spawned Rayquaza:
 * - is captured/owned,
 * - dies or is removed while its last chunk is loaded, or
 * - travels beyond the configured radius.
 *
 * Tracking by UUID is more reliable than repeatedly searching for any nearby
 * Rayquaza and avoids confusing another wild/owned Rayquaza with the encounter.
 */
public final class RayquazaCompletionCondition implements DungeonCompletionCondition {
    public static final String ID = "rayquaza_sky_pillar";
    private static final int GRACE_CHECKS = 5;
    private static final int REQUIRED_MISSING_CHECKS = 3;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public CompletionCheckResult evaluate(
            MinecraftServer server,
            DungeonCompletionWatch watch
    ) {
        ResourceLocation dimensionId;

        try {
            dimensionId = ResourceLocation.parse(watch.dimensionId());
        } catch (Exception ignored) {
            return CompletionCheckResult.CANCEL;
        }

        ServerLevel level = server.getLevel(
                ResourceKey.create(Registries.DIMENSION, dimensionId)
        );

        if (level == null) {
            return CompletionCheckResult.PENDING;
        }

        Entity entity = level.getEntity(watch.trackedEntityId());

        if (entity instanceof PokemonEntity pokemon) {
            watch.markSeen(pokemon.blockPosition());

            if (!pokemon.isAlive()) {
                return CompletionCheckResult.COMPLETE;
            }

            if (pokemon.getOwnerUUID() != null) {
                return CompletionCheckResult.COMPLETE;
            }

            double maximumDistanceSquared =
                    watch.maximumDistance() * watch.maximumDistance();

            if (pokemon.blockPosition().distSqr(watch.origin())
                    > maximumDistanceSquared) {
                return CompletionCheckResult.COMPLETE;
            }

            return CompletionCheckResult.PENDING;
        }

        if (watch.ageChecks() < GRACE_CHECKS) {
            watch.markPendingWithoutMissing();
            return CompletionCheckResult.PENDING;
        }

        BlockPos lastKnownPos = watch.lastKnownPos();

        /*
         * Do not interpret an unloaded entity chunk as defeat/capture.
         * The watch waits until that location is loaded again.
         */
        if (!level.hasChunkAt(lastKnownPos)) {
            watch.markPendingWithoutMissing();
            return CompletionCheckResult.PENDING;
        }

        watch.markMissing();

        return watch.missingChecks() >= REQUIRED_MISSING_CHECKS
                ? CompletionCheckResult.COMPLETE
                : CompletionCheckResult.PENDING;
    }
}
