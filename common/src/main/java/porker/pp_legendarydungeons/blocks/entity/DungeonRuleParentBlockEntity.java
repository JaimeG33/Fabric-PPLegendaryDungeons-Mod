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
 * Non-ticking parent controller.
 *
 * The block's world position plus dimension becomes the unique instance ID.
 * The preset and overrides become the defaults inherited by child zones.
 */
public final class DungeonRuleParentBlockEntity extends BlockEntity {
    private String presetId = DungeonRulePresetRegistry.STANDARD_DUNGEON_ID.toString();
    private int linkChannel = 0;
    private boolean enabled = true;
    private DungeonRuleSet ruleOverrides = new DungeonRuleSet();

    public DungeonRuleParentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DUNGEON_RULE_PARENT.get(), pos, state);
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

    public boolean isEnabled() {
        return enabled;
    }

    public DungeonRuleSet getRuleOverrides() {
        return DungeonRuleSet.fromPackedInt(ruleOverrides.toPackedInt());
    }

    public String getInstanceId() {
        if (level instanceof ServerLevel serverLevel) {
            return DungeonRuleManager.instanceId(serverLevel, worldPosition);
        }

        return "";
    }

    public void updateConfiguration(
            String presetId,
            int linkChannel,
            boolean enabled,
            DungeonRuleSet overrides
    ) {
        this.presetId = getValidPresetId(presetId);
        this.linkChannel = linkChannel;
        this.enabled = enabled;
        this.ruleOverrides = overrides == null ? new DungeonRuleSet() : overrides;
        setChanged();

        if (level instanceof ServerLevel) {
            DungeonRuleManager.refreshParent(this);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (!level.isClientSide) {
            DungeonRuleManager.registerParent(this);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("PresetId", presetId);
        tag.putInt("LinkChannel", linkChannel);
        tag.putBoolean("Enabled", enabled);
        tag.put("RuleOverrides", ruleOverrides.save());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("PresetId")) {
            presetId = getValidPresetId(tag.getString("PresetId"));
        }

        linkChannel = tag.getInt("LinkChannel");
        enabled = !tag.contains("Enabled") || tag.getBoolean("Enabled");

        if (tag.contains("RuleOverrides")) {
            ruleOverrides = DungeonRuleSet.load(tag.getCompound("RuleOverrides"));
        }

        if (level instanceof ServerLevel) {
            DungeonRuleManager.refreshParent(this);
        }
    }

    private static String getValidPresetId(String value) {
        try {
            return ResourceLocation.parse(value).toString();
        } catch (Exception ignored) {
            return DungeonRulePresetRegistry.STANDARD_DUNGEON_ID.toString();
        }
    }
}
