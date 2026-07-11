
package porker.pp_legendarydungeons.dungeon_rules.instance;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleSet;

/**
 * Persistent record for one generated dungeon copy.
 *
 * Preset IDs may be reused. Instance IDs are unique because they include the
 * parent block's dimension and world position.
 */
public final class DungeonInstanceRecord {
    private final String instanceId;
    private String dimensionId;
    private long parentPos;
    private String presetId;
    private int linkChannel;
    private boolean enabled;
    private DungeonInstanceState state;
    private int parentOverridesPacked;

    public DungeonInstanceRecord(
            String instanceId,
            String dimensionId,
            long parentPos,
            String presetId,
            int linkChannel,
            boolean enabled,
            DungeonInstanceState state,
            int parentOverridesPacked
    ) {
        this.instanceId = instanceId;
        this.dimensionId = dimensionId;
        this.parentPos = parentPos;
        this.presetId = presetId;
        this.linkChannel = linkChannel;
        this.enabled = enabled;
        this.state = state;
        this.parentOverridesPacked = parentOverridesPacked;
    }

    public String instanceId() {
        return instanceId;
    }

    public String dimensionId() {
        return dimensionId;
    }

    public BlockPos parentPos() {
        return BlockPos.of(parentPos);
    }

    public String presetId() {
        return presetId;
    }

    public int linkChannel() {
        return linkChannel;
    }

    public boolean enabled() {
        return enabled;
    }

    public DungeonInstanceState state() {
        return state;
    }

    public DungeonRuleSet parentOverrides() {
        return DungeonRuleSet.fromPackedInt(parentOverridesPacked);
    }

    public boolean rulesAreActive() {
        return enabled && state == DungeonInstanceState.ACTIVE;
    }

    public void updateConfiguration(
            String dimensionId,
            BlockPos parentPos,
            ResourceLocation presetId,
            int linkChannel,
            boolean enabled,
            DungeonRuleSet parentOverrides
    ) {
        this.dimensionId = dimensionId;
        this.parentPos = parentPos.asLong();
        this.presetId = presetId.toString();
        this.linkChannel = linkChannel;
        this.enabled = enabled;
        this.parentOverridesPacked = parentOverrides.toPackedInt();
    }

    public void setState(DungeonInstanceState state) {
        this.state = state;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("InstanceId", instanceId);
        tag.putString("Dimension", dimensionId);
        tag.putLong("ParentPos", parentPos);
        tag.putString("PresetId", presetId);
        tag.putInt("LinkChannel", linkChannel);
        tag.putBoolean("Enabled", enabled);
        tag.putString("State", state.name());
        tag.putInt("ParentOverrides", parentOverridesPacked);
        return tag;
    }

    public static DungeonInstanceRecord load(CompoundTag tag) {
        return new DungeonInstanceRecord(
                tag.getString("InstanceId"),
                tag.getString("Dimension"),
                tag.getLong("ParentPos"),
                tag.getString("PresetId"),
                tag.getInt("LinkChannel"),
                !tag.contains("Enabled") || tag.getBoolean("Enabled"),
                DungeonInstanceState.parse(tag.getString("State")),
                tag.getInt("ParentOverrides")
        );
    }
}
