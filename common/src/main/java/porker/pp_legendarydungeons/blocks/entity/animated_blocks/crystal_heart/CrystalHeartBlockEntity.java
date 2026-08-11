package porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.blocks.animated_blocks.crystal_heart.CrystalHeartBlock;
import porker.pp_legendarydungeons.setup.ModBlockEntities;

/**
 * Persistent per-instance settings for a Crystal Heart.
 *
 * <p>Visual shape/size settings and optional mining/drop behavior are stored in
 * block-entity NBT. Structure blocks preserve this data, so each dungeon heart
 * can choose its own presentation and whether survival players may mine it.</p>
 */
public final class CrystalHeartBlockEntity extends BlockEntity {
    public static final String PRESET_NBT_KEY = "CrystalPreset";
    public static final String WIDTH_NBT_KEY = "CrystalWidth";
    public static final String HEIGHT_NBT_KEY = "CrystalHeight";
    public static final String BOB_AMPLITUDE_NBT_KEY = "BobAmplitude";
    public static final String MINING_ENABLED_NBT_KEY = "MiningEnabled";
    public static final String MINING_MODE_NBT_KEY = "MiningMode";
    public static final String MINING_LOOT_TABLE_NBT_KEY = "MiningLootTable";
    public static final String LIGHT_LEVEL_NBT_KEY = "LightLevel";
    public static final String COLOR_MODE_NBT_KEY = "CrystalColorMode";
    public static final String COLOR_PRESET_NBT_KEY = "CrystalColorPreset";
    public static final String CRYSTAL_COLOR_NBT_KEY = "CrystalColor";

    /** Matches the final floor-anchored datapack particle-heart proportions. */
    public static final float DEFAULT_WIDTH_BLOCKS = 5.5F;
    public static final float DEFAULT_HEIGHT_BLOCKS = 12.0F;
    public static final float DEFAULT_BOB_AMPLITUDE_BLOCKS = 0.35F;
    public static final int DEFAULT_LIGHT_LEVEL = 0;
    public static final CrystalHeartColorMode DEFAULT_COLOR_MODE =
            CrystalHeartColorMode.PRESET;
    public static final CrystalHeartColorPreset DEFAULT_COLOR_PRESET =
            CrystalHeartColorPreset.GREEN;
    public static final int DEFAULT_CRYSTAL_COLOR = 0xFFFFFF;

    public static final boolean DEFAULT_MINING_ENABLED = false;
    public static final CrystalHeartMiningMode DEFAULT_MINING_MODE =
            CrystalHeartMiningMode.SILK_SELF_ELSE_LOOT;
    public static final ResourceLocation DEFAULT_MINING_LOOT_TABLE =
            ResourceLocation.fromNamespaceAndPath(
                    LegendaryDungeons.MOD_ID,
                    "blocks/default_ch_drop"
            );

    public static final float MIN_WIDTH_BLOCKS = 0.25F;
    public static final float MAX_WIDTH_BLOCKS = 64.0F;
    public static final float MIN_HEIGHT_BLOCKS = 0.25F;
    public static final float MAX_HEIGHT_BLOCKS = 128.0F;
    public static final float MIN_BOB_AMPLITUDE_BLOCKS = 0.0F;
    public static final float MAX_BOB_AMPLITUDE_BLOCKS = 8.0F;
    public static final int MIN_LIGHT_LEVEL = 0;
    public static final int MAX_LIGHT_LEVEL = 15;

    private CrystalHeartPreset preset = CrystalHeartPreset.BASE;
    private float widthBlocks = DEFAULT_WIDTH_BLOCKS;
    private float heightBlocks = DEFAULT_HEIGHT_BLOCKS;
    private float bobAmplitudeBlocks = DEFAULT_BOB_AMPLITUDE_BLOCKS;
    private int lightLevel = DEFAULT_LIGHT_LEVEL;
    private CrystalHeartColorMode colorMode = DEFAULT_COLOR_MODE;
    private CrystalHeartColorPreset colorPreset = DEFAULT_COLOR_PRESET;
    private int crystalColor = DEFAULT_CRYSTAL_COLOR;
    private boolean miningEnabled = DEFAULT_MINING_ENABLED;
    private CrystalHeartMiningMode miningMode = DEFAULT_MINING_MODE;
    private ResourceLocation miningLootTable = DEFAULT_MINING_LOOT_TABLE;

    public CrystalHeartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_HEART.get(), pos, state);
    }

    public CrystalHeartPreset getPreset() {
        return preset;
    }

    public float getWidthBlocks() {
        return widthBlocks;
    }

    public float getHeightBlocks() {
        return heightBlocks;
    }

    public float getBobAmplitudeBlocks() {
        return bobAmplitudeBlocks;
    }

    public int getLightLevel() {
        return lightLevel;
    }

    public CrystalHeartColorMode getColorMode() {
        return colorMode;
    }

    public CrystalHeartColorPreset getColorPreset() {
        return colorPreset;
    }

    public int getCrystalColor() {
        return crystalColor;
    }

    public boolean isMiningEnabled() {
        return miningEnabled;
    }

    public CrystalHeartMiningMode getMiningMode() {
        return miningMode;
    }

    public ResourceLocation getMiningLootTable() {
        return miningLootTable;
    }

    public boolean usesDefaultMiningLootTable() {
        return DEFAULT_MINING_LOOT_TABLE.equals(miningLootTable);
    }

    /**
     * Future gameplay code can use this instead of editing NBT directly.
     */
    public void setPreset(CrystalHeartPreset preset) {
        this.preset = preset == null ? CrystalHeartPreset.BASE : preset;
        markChangedAndSync();
    }

    /**
     * Future gameplay code can use this instead of editing NBT directly.
     */
    public void setDimensions(float widthBlocks, float heightBlocks) {
        this.widthBlocks = clampWidth(widthBlocks);
        this.heightBlocks = clampHeight(heightBlocks);
        markChangedAndSync();
    }

    public void setLightLevel(int lightLevel) {
        this.lightLevel = clampLightLevel(lightLevel);
        syncLightLevelToBlockState();
        markChangedAndSync();
    }

    public void setColorMode(CrystalHeartColorMode colorMode) {
        this.colorMode = colorMode == null ? DEFAULT_COLOR_MODE : colorMode;
        markChangedAndSync();
    }

    public void setColorPreset(CrystalHeartColorPreset colorPreset) {
        this.colorPreset = colorPreset == null ? DEFAULT_COLOR_PRESET : colorPreset;
        markChangedAndSync();
    }

    public void setCrystalColor(int crystalColor) {
        this.crystalColor = normalizeColor(crystalColor);
        markChangedAndSync();
    }

    public void setMiningEnabled(boolean miningEnabled) {
        this.miningEnabled = miningEnabled;
        markChangedAndSync();
    }

    public void setMiningMode(CrystalHeartMiningMode miningMode) {
        this.miningMode = miningMode == null ? DEFAULT_MINING_MODE : miningMode;
        markChangedAndSync();
    }

    public void setMiningLootTable(ResourceLocation miningLootTable) {
        this.miningLootTable = miningLootTable == null
                ? DEFAULT_MINING_LOOT_TABLE
                : miningLootTable;
        markChangedAndSync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(PRESET_NBT_KEY, preset.id());
        tag.putFloat(WIDTH_NBT_KEY, widthBlocks);
        tag.putFloat(HEIGHT_NBT_KEY, heightBlocks);
        tag.putFloat(BOB_AMPLITUDE_NBT_KEY, bobAmplitudeBlocks);
        tag.putInt(LIGHT_LEVEL_NBT_KEY, lightLevel);
        tag.putString(COLOR_MODE_NBT_KEY, colorMode.id());
        tag.putString(COLOR_PRESET_NBT_KEY, colorPreset.id());
        tag.putInt(CRYSTAL_COLOR_NBT_KEY, crystalColor);
        tag.putBoolean(MINING_ENABLED_NBT_KEY, miningEnabled);
        tag.putString(MINING_MODE_NBT_KEY, miningMode.id());
        tag.putString(MINING_LOOT_TABLE_NBT_KEY, miningLootTable.toString());
    }

    /**
     * Stores this exact configured heart on a dropped BlockItem. Minecraft's
     * BlockItem placement path reapplies the saved block-entity data when the
     * item is placed again.
     */
    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        BlockItem.setBlockEntityData(stack, getType(), tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        preset = tag.contains(PRESET_NBT_KEY)
                ? CrystalHeartPreset.fromId(tag.getString(PRESET_NBT_KEY))
                : CrystalHeartPreset.BASE;

        widthBlocks = tag.contains(WIDTH_NBT_KEY)
                ? clampWidth(tag.getFloat(WIDTH_NBT_KEY))
                : DEFAULT_WIDTH_BLOCKS;

        heightBlocks = tag.contains(HEIGHT_NBT_KEY)
                ? clampHeight(tag.getFloat(HEIGHT_NBT_KEY))
                : DEFAULT_HEIGHT_BLOCKS;

        bobAmplitudeBlocks = tag.contains(BOB_AMPLITUDE_NBT_KEY)
                ? clampBobAmplitude(tag.getFloat(BOB_AMPLITUDE_NBT_KEY))
                : DEFAULT_BOB_AMPLITUDE_BLOCKS;

        lightLevel = tag.contains(LIGHT_LEVEL_NBT_KEY)
                ? clampLightLevel(tag.getInt(LIGHT_LEVEL_NBT_KEY))
                : DEFAULT_LIGHT_LEVEL;

        colorMode = tag.contains(COLOR_MODE_NBT_KEY)
                ? CrystalHeartColorMode.fromId(tag.getString(COLOR_MODE_NBT_KEY))
                : DEFAULT_COLOR_MODE;

        colorPreset = tag.contains(COLOR_PRESET_NBT_KEY)
                ? CrystalHeartColorPreset.fromId(tag.getString(COLOR_PRESET_NBT_KEY))
                : DEFAULT_COLOR_PRESET;

        crystalColor = tag.contains(CRYSTAL_COLOR_NBT_KEY)
                ? normalizeColor(tag.getInt(CRYSTAL_COLOR_NBT_KEY))
                : DEFAULT_CRYSTAL_COLOR;

        miningEnabled = tag.contains(MINING_ENABLED_NBT_KEY)
                ? tag.getBoolean(MINING_ENABLED_NBT_KEY)
                : DEFAULT_MINING_ENABLED;

        miningMode = tag.contains(MINING_MODE_NBT_KEY)
                ? CrystalHeartMiningMode.fromId(tag.getString(MINING_MODE_NBT_KEY))
                : DEFAULT_MINING_MODE;

        miningLootTable = tag.contains(MINING_LOOT_TABLE_NBT_KEY)
                ? parseMiningLootTable(tag.getString(MINING_LOOT_TABLE_NBT_KEY))
                : DEFAULT_MINING_LOOT_TABLE;

        /*
         * /data merge block reloads the block entity NBT while it is already in
         * the world. Mirror LightLevel into BlockState immediately so vanilla's
         * lighting engine sees the new emission value.
         */
        syncLightLevelToBlockState();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void markChangedAndSync() {
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    private void syncLightLevelToBlockState() {
        if (level == null || level.isClientSide) {
            return;
        }

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof CrystalHeartBlock)
                || !state.hasProperty(CrystalHeartBlock.LIGHT_LEVEL)
                || state.getValue(CrystalHeartBlock.LIGHT_LEVEL) == lightLevel) {
            return;
        }

        level.setBlock(
                worldPosition,
                state.setValue(CrystalHeartBlock.LIGHT_LEVEL, lightLevel),
                Block.UPDATE_ALL
        );
    }

    private static ResourceLocation parseMiningLootTable(String value) {
        ResourceLocation parsed = ResourceLocation.tryParse(value);
        return parsed == null ? DEFAULT_MINING_LOOT_TABLE : parsed;
    }

    private static int clampLightLevel(int value) {
        return Mth.clamp(value, MIN_LIGHT_LEVEL, MAX_LIGHT_LEVEL);
    }

    private static int normalizeColor(int value) {
        return value & 0x00FFFFFF;
    }

    private static float clampWidth(float value) {
        if (!Float.isFinite(value)) {
            return DEFAULT_WIDTH_BLOCKS;
        }

        return Mth.clamp(value, MIN_WIDTH_BLOCKS, MAX_WIDTH_BLOCKS);
    }

    private static float clampHeight(float value) {
        if (!Float.isFinite(value)) {
            return DEFAULT_HEIGHT_BLOCKS;
        }

        return Mth.clamp(value, MIN_HEIGHT_BLOCKS, MAX_HEIGHT_BLOCKS);
    }

    private static float clampBobAmplitude(float value) {
        if (!Float.isFinite(value)) {
            return DEFAULT_BOB_AMPLITUDE_BLOCKS;
        }

        return Mth.clamp(
                value,
                MIN_BOB_AMPLITUDE_BLOCKS,
                MAX_BOB_AMPLITUDE_BLOCKS
        );
    }
}
