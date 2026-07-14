package porker.pp_legendarydungeons.dungeon_rules.instance;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleSet;

/**
 * Persistent definition of one child rule box.
 *
 * Once a generated controller block has loaded at least once, this record allows
 * its zone to remain indexed even when the controller's own chunk later unloads.
 */
public final class DungeonZoneRecord {
    private final String zoneKey;
    private String dimensionId;
    private long anchorPos;
    private String presetId;
    private int linkChannel;
    private int offsetX;
    private int offsetY;
    private int offsetZ;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private int priority;
    private int maximumParentDistance;
    private int ruleOverridesPacked;
    private String resolvedInstanceId;

    public DungeonZoneRecord(
            String zoneKey,
            String dimensionId,
            long anchorPos,
            String presetId,
            int linkChannel,
            int offsetX,
            int offsetY,
            int offsetZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int priority,
            int maximumParentDistance,
            int ruleOverridesPacked,
            String resolvedInstanceId
    ) {
        this.zoneKey = zoneKey;
        this.dimensionId = dimensionId;
        this.anchorPos = anchorPos;
        this.presetId = presetId;
        this.linkChannel = linkChannel;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.sizeX = Math.max(1, sizeX);
        this.sizeY = Math.max(1, sizeY);
        this.sizeZ = Math.max(1, sizeZ);
        this.priority = priority;
        this.maximumParentDistance = Math.max(16, maximumParentDistance);
        this.ruleOverridesPacked = ruleOverridesPacked;
        this.resolvedInstanceId = resolvedInstanceId == null ? "" : resolvedInstanceId;
    }

    public String zoneKey() {
        return zoneKey;
    }

    public String dimensionId() {
        return dimensionId;
    }

    public BlockPos anchorPos() {
        return BlockPos.of(anchorPos);
    }

    public ResourceLocation presetId() {
        try {
            return ResourceLocation.parse(presetId);
        } catch (Exception ignored) {
            return ResourceLocation.fromNamespaceAndPath("pp_legendarydungeons", "standard_dungeon");
        }
    }

    public int linkChannel() {
        return linkChannel;
    }

    public int priority() {
        return priority;
    }

    public double maximumParentDistance() {
        return maximumParentDistance;
    }

    public DungeonRuleSet ruleOverrides() {
        return DungeonRuleSet.fromPackedInt(ruleOverridesPacked);
    }

    public String resolvedInstanceId() {
        return resolvedInstanceId;
    }

    public BlockPos minPos() {
        return anchorPos().offset(offsetX, offsetY, offsetZ);
    }

    public BlockPos maxPos() {
        return minPos().offset(sizeX - 1, sizeY - 1, sizeZ - 1);
    }

    public void update(
            String dimensionId,
            BlockPos anchorPos,
            ResourceLocation presetId,
            int linkChannel,
            int offsetX,
            int offsetY,
            int offsetZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int priority,
            int maximumParentDistance,
            DungeonRuleSet ruleOverrides
    ) {
        this.dimensionId = dimensionId;
        this.anchorPos = anchorPos.asLong();
        this.presetId = presetId.toString();
        this.linkChannel = linkChannel;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.sizeX = Math.max(1, sizeX);
        this.sizeY = Math.max(1, sizeY);
        this.sizeZ = Math.max(1, sizeZ);
        this.priority = priority;
        this.maximumParentDistance = Math.max(16, maximumParentDistance);
        this.ruleOverridesPacked = ruleOverrides.toPackedInt();
    }

    public void setResolvedInstanceId(String resolvedInstanceId) {
        this.resolvedInstanceId = resolvedInstanceId == null ? "" : resolvedInstanceId;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ZoneKey", zoneKey);
        tag.putString("Dimension", dimensionId);
        tag.putLong("AnchorPos", anchorPos);
        tag.putString("PresetId", presetId);
        tag.putInt("LinkChannel", linkChannel);
        tag.putInt("OffsetX", offsetX);
        tag.putInt("OffsetY", offsetY);
        tag.putInt("OffsetZ", offsetZ);
        tag.putInt("SizeX", sizeX);
        tag.putInt("SizeY", sizeY);
        tag.putInt("SizeZ", sizeZ);
        tag.putInt("Priority", priority);
        tag.putInt("MaximumParentDistance", maximumParentDistance);
        tag.putInt("RuleOverrides", ruleOverridesPacked);
        tag.putString("ResolvedInstanceId", resolvedInstanceId);
        return tag;
    }

    public static DungeonZoneRecord load(CompoundTag tag) {
        return new DungeonZoneRecord(
                tag.getString("ZoneKey"),
                tag.getString("Dimension"),
                tag.getLong("AnchorPos"),
                tag.getString("PresetId"),
                tag.getInt("LinkChannel"),
                tag.getInt("OffsetX"),
                tag.getInt("OffsetY"),
                tag.getInt("OffsetZ"),
                tag.contains("SizeX") ? tag.getInt("SizeX") : 1,
                tag.contains("SizeY") ? tag.getInt("SizeY") : 1,
                tag.contains("SizeZ") ? tag.getInt("SizeZ") : 1,
                tag.getInt("Priority"),
                tag.contains("MaximumParentDistance")
                        ? tag.getInt("MaximumParentDistance")
                        : 512,
                tag.getInt("RuleOverrides"),
                tag.getString("ResolvedInstanceId")
        );
    }
}
