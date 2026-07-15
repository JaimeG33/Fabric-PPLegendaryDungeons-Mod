package porker.pp_legendarydungeons.dungeon_rules.instance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleSet;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleZoneBounds;

import java.util.Locale;

/**
 * Persistent definition of one child rule box.
 *
 * <p>Once a generated controller block has loaded at least once, this record allows
 * its zone to remain indexed even when the controller's own chunk later unloads.</p>
 *
 * <p>The controller facing is persisted so server restarts rebuild the same
 * rotation-aware bounds even when the controller chunk is not loaded.</p>
 */
public final class DungeonZoneRecord {
    private final String zoneKey;
    private String dimensionId;
    private long anchorPos;
    private Direction facing;
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
            Direction facing,
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
        this.facing = horizontalFacingOrNorth(facing);
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

    public Direction facing() {
        return facing;
    }

    public ResourceLocation presetId() {
        try {
            return ResourceLocation.parse(presetId);
        } catch (Exception ignored) {
            return ResourceLocation.fromNamespaceAndPath(
                    "pp_legendarydungeons",
                    "standard_dungeon"
            );
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

    public DungeonRuleZoneBounds bounds() {
        return DungeonRuleZoneBounds.fromLocalBox(
                anchorPos(),
                facing,
                offsetX,
                offsetY,
                offsetZ,
                sizeX,
                sizeY,
                sizeZ
        );
    }

    public BlockPos minPos() {
        return bounds().minPos();
    }

    public BlockPos maxPos() {
        return bounds().maxPos();
    }

    public void update(
            String dimensionId,
            BlockPos anchorPos,
            Direction facing,
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
        this.facing = horizontalFacingOrNorth(facing);
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
        tag.putString("Facing", facing.name().toLowerCase(Locale.ROOT));
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
                parseFacing(
                        tag.contains("Facing")
                                ? tag.getString("Facing")
                                : "north"
                ),
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

    private static Direction parseFacing(String value) {
        try {
            return horizontalFacingOrNorth(
                    Direction.valueOf(value.toUpperCase(Locale.ROOT))
            );
        } catch (Exception ignored) {
            return Direction.NORTH;
        }
    }

    private static Direction horizontalFacingOrNorth(Direction value) {
        if (value == null) {
            return Direction.NORTH;
        }

        return switch (value) {
            case NORTH, EAST, SOUTH, WEST -> value;
            default -> Direction.NORTH;
        };
    }
}
