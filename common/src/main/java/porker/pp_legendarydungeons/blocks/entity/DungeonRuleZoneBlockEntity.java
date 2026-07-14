package porker.pp_legendarydungeons.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleSet;
import porker.pp_legendarydungeons.dungeon_rules.preset.DungeonRulePresetRegistry;
import porker.pp_legendarydungeons.setup.ModBlockEntities;

/**
 * Non-ticking child box controller.
 *
 * Offsets and sizes are relative to this block. The zone links at runtime to the
 * nearest parent with the same preset and channel.
 */
public final class DungeonRuleZoneBlockEntity extends BlockEntity {
    private String presetId = DungeonRulePresetRegistry.STANDARD_DUNGEON_ID.toString();
    private int linkChannel = 0;
    private int offsetX = -8;
    private int offsetY = -4;
    private int offsetZ = -8;
    private int sizeX = 16;
    private int sizeY = 8;
    private int sizeZ = 16;
    private int priority = 0;
    private int maximumParentDistance = (int) DungeonRuleManager.DEFAULT_PARENT_LINK_DISTANCE;
    private DungeonRuleSet ruleOverrides = new DungeonRuleSet();

    public DungeonRuleZoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DUNGEON_RULE_ZONE.get(), pos, state);
    }

    public ResourceLocation getPresetResourceLocation() {
        try {
            return ResourceLocation.parse(presetId);
        } catch (Exception ignored) {
            return DungeonRulePresetRegistry.STANDARD_DUNGEON_ID;
        }
    }

    public String getPresetId() {
        return presetId;
    }

    public int getLinkChannel() {
        return linkChannel;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getOffsetZ() {
        return offsetZ;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public int getPriority() {
        return priority;
    }

    public double getMaximumParentDistance() {
        return maximumParentDistance;
    }

    public DungeonRuleSet getRuleOverrides() {
        return DungeonRuleSet.fromPackedInt(ruleOverrides.toPackedInt());
    }

    public BlockPos getWorldMinPos() {
        return worldPosition.offset(offsetX, offsetY, offsetZ);
    }

    public BlockPos getWorldMaxPos() {
        BlockPos min = getWorldMinPos();
        return min.offset(
                Math.max(1, sizeX) - 1,
                Math.max(1, sizeY) - 1,
                Math.max(1, sizeZ) - 1
        );
    }

    public void updateConfiguration(
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
            DungeonRuleSet overrides
    ) {
        this.presetId = getValidPresetId(presetId);
        this.linkChannel = linkChannel;
        this.offsetX = clamp(offsetX, -512, 512);
        this.offsetY = clamp(offsetY, -384, 384);
        this.offsetZ = clamp(offsetZ, -512, 512);
        this.sizeX = clamp(sizeX, 1, 1024);
        this.sizeY = clamp(sizeY, 1, 768);
        this.sizeZ = clamp(sizeZ, 1, 1024);
        this.priority = clamp(priority, -1000, 1000);
        this.maximumParentDistance = clamp(maximumParentDistance, 16, 4096);
        this.ruleOverrides = overrides == null ? new DungeonRuleSet() : overrides;
        setChanged();

        if (level instanceof ServerLevel) {
            DungeonRuleManager.refreshZone(this);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (!level.isClientSide) {
            DungeonRuleManager.registerZone(this);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel) {
            DungeonRuleManager.unregisterZone(this);
        }

        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
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
        tag.put("RuleOverrides", ruleOverrides.save());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("PresetId")) {
            presetId = getValidPresetId(tag.getString("PresetId"));
        }

        linkChannel = tag.getInt("LinkChannel");
        offsetX = tag.contains("OffsetX") ? tag.getInt("OffsetX") : -8;
        offsetY = tag.contains("OffsetY") ? tag.getInt("OffsetY") : -4;
        offsetZ = tag.contains("OffsetZ") ? tag.getInt("OffsetZ") : -8;
        sizeX = tag.contains("SizeX") ? Math.max(1, tag.getInt("SizeX")) : 16;
        sizeY = tag.contains("SizeY") ? Math.max(1, tag.getInt("SizeY")) : 8;
        sizeZ = tag.contains("SizeZ") ? Math.max(1, tag.getInt("SizeZ")) : 16;
        priority = tag.getInt("Priority");
        maximumParentDistance = tag.contains("MaximumParentDistance")
                ? Math.max(16, tag.getInt("MaximumParentDistance"))
                : (int) DungeonRuleManager.DEFAULT_PARENT_LINK_DISTANCE;

        if (tag.contains("RuleOverrides")) {
            ruleOverrides = DungeonRuleSet.load(tag.getCompound("RuleOverrides"));
        }

        if (level instanceof ServerLevel) {
            DungeonRuleManager.refreshZone(this);
        }
    }

    private static String getValidPresetId(String value) {
        try {
            return ResourceLocation.parse(value).toString();
        } catch (Exception ignored) {
            return DungeonRulePresetRegistry.STANDARD_DUNGEON_ID.toString();
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
