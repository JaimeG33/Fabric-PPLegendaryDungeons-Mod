
package porker.pp_legendarydungeons.summon.dungeon_completion;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * Persistent, generic watch record.
 *
 * Condition-specific behavior lives in a file registered by conditionId.
 */
public final class DungeonCompletionWatch {
    private final UUID watchId;
    private final String conditionId;
    private final String instanceId;
    private final String dimensionId;
    private final UUID trackedEntityId;
    private final BlockPos origin;
    private final double maximumDistance;

    private BlockPos lastKnownPos;
    private int ageChecks;
    private int missingChecks;

    public DungeonCompletionWatch(
            UUID watchId,
            String conditionId,
            String instanceId,
            String dimensionId,
            UUID trackedEntityId,
            BlockPos origin,
            double maximumDistance,
            BlockPos lastKnownPos,
            int ageChecks,
            int missingChecks
    ) {
        this.watchId = watchId;
        this.conditionId = conditionId;
        this.instanceId = instanceId;
        this.dimensionId = dimensionId;
        this.trackedEntityId = trackedEntityId;
        this.origin = origin.immutable();
        this.maximumDistance = maximumDistance;
        this.lastKnownPos = lastKnownPos.immutable();
        this.ageChecks = ageChecks;
        this.missingChecks = missingChecks;
    }

    public UUID watchId() {
        return watchId;
    }

    public String conditionId() {
        return conditionId;
    }

    public String instanceId() {
        return instanceId;
    }

    public String dimensionId() {
        return dimensionId;
    }

    public UUID trackedEntityId() {
        return trackedEntityId;
    }

    public BlockPos origin() {
        return origin;
    }

    public double maximumDistance() {
        return maximumDistance;
    }

    public BlockPos lastKnownPos() {
        return lastKnownPos;
    }

    public int ageChecks() {
        return ageChecks;
    }

    public int missingChecks() {
        return missingChecks;
    }

    public void markSeen(BlockPos pos) {
        lastKnownPos = pos.immutable();
        missingChecks = 0;
        ageChecks++;
    }

    public void markMissing() {
        missingChecks++;
        ageChecks++;
    }

    public void markPendingWithoutMissing() {
        ageChecks++;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("WatchId", watchId);
        tag.putString("ConditionId", conditionId);
        tag.putString("InstanceId", instanceId);
        tag.putString("DimensionId", dimensionId);
        tag.putUUID("TrackedEntityId", trackedEntityId);
        tag.putLong("Origin", origin.asLong());
        tag.putDouble("MaximumDistance", maximumDistance);
        tag.putLong("LastKnownPos", lastKnownPos.asLong());
        tag.putInt("AgeChecks", ageChecks);
        tag.putInt("MissingChecks", missingChecks);
        return tag;
    }

    public static DungeonCompletionWatch load(CompoundTag tag) {
        return new DungeonCompletionWatch(
                tag.hasUUID("WatchId") ? tag.getUUID("WatchId") : UUID.randomUUID(),
                tag.getString("ConditionId"),
                tag.getString("InstanceId"),
                tag.getString("DimensionId"),
                tag.getUUID("TrackedEntityId"),
                BlockPos.of(tag.getLong("Origin")),
                tag.getDouble("MaximumDistance"),
                BlockPos.of(tag.getLong("LastKnownPos")),
                tag.getInt("AgeChecks"),
                tag.getInt("MissingChecks")
        );
    }
}
