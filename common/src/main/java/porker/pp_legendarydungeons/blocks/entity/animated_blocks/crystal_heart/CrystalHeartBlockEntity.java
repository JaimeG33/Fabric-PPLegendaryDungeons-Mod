package porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import porker.pp_legendarydungeons.setup.ModBlockEntities;

/**
 * Persistent per-instance settings for a Crystal Heart.
 *
 * <p>Shape, width, height, and bobbing are stored independently in NBT so one
 * registered block can represent many differently proportioned hearts.
 * Structure blocks preserve this NBT, so a dungeon can save a specific preset
 * and size without requiring another block variant or model.</p>
 */
public final class CrystalHeartBlockEntity extends BlockEntity {
    public static final String PRESET_NBT_KEY = "CrystalPreset";
    public static final String WIDTH_NBT_KEY = "CrystalWidth";
    public static final String HEIGHT_NBT_KEY = "CrystalHeight";
    public static final String BOB_AMPLITUDE_NBT_KEY = "BobAmplitude";

    /** Matches the final floor-anchored datapack particle-heart proportions. */
    public static final float DEFAULT_WIDTH_BLOCKS = 5.5F;
    public static final float DEFAULT_HEIGHT_BLOCKS = 12.0F;
    public static final float DEFAULT_BOB_AMPLITUDE_BLOCKS = 0.35F;

    public static final float MIN_WIDTH_BLOCKS = 0.25F;
    public static final float MAX_WIDTH_BLOCKS = 64.0F;
    public static final float MIN_HEIGHT_BLOCKS = 0.25F;
    public static final float MAX_HEIGHT_BLOCKS = 128.0F;
    public static final float MIN_BOB_AMPLITUDE_BLOCKS = 0.0F;
    public static final float MAX_BOB_AMPLITUDE_BLOCKS = 8.0F;

    private CrystalHeartPreset preset = CrystalHeartPreset.BASE;
    private float widthBlocks = DEFAULT_WIDTH_BLOCKS;
    private float heightBlocks = DEFAULT_HEIGHT_BLOCKS;
    private float bobAmplitudeBlocks = DEFAULT_BOB_AMPLITUDE_BLOCKS;

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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(PRESET_NBT_KEY, preset.id());
        tag.putFloat(WIDTH_NBT_KEY, widthBlocks);
        tag.putFloat(HEIGHT_NBT_KEY, heightBlocks);
        tag.putFloat(BOB_AMPLITUDE_NBT_KEY, bobAmplitudeBlocks);
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
