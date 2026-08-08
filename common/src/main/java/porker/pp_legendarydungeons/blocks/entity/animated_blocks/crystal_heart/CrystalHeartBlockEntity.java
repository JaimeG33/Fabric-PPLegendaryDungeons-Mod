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
 * <p>Width and height are stored independently in NBT so one registered block
 * can represent many differently sized hearts. Structure blocks preserve this
 * NBT, so a dungeon can save a specific size without requiring another block
 * variant or model.</p>
 */
public final class CrystalHeartBlockEntity extends BlockEntity {
    public static final String WIDTH_NBT_KEY = "CrystalWidth";
    public static final String HEIGHT_NBT_KEY = "CrystalHeight";

    /** Matches the final floor-anchored datapack particle-heart proportions. */
    public static final float DEFAULT_WIDTH_BLOCKS = 5.5F;
    public static final float DEFAULT_HEIGHT_BLOCKS = 12.0F;

    public static final float MIN_WIDTH_BLOCKS = 0.25F;
    public static final float MAX_WIDTH_BLOCKS = 64.0F;
    public static final float MIN_HEIGHT_BLOCKS = 0.25F;
    public static final float MAX_HEIGHT_BLOCKS = 128.0F;

    private float widthBlocks = DEFAULT_WIDTH_BLOCKS;
    private float heightBlocks = DEFAULT_HEIGHT_BLOCKS;

    public CrystalHeartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_HEART.get(), pos, state);
    }

    public float getWidthBlocks() {
        return widthBlocks;
    }

    public float getHeightBlocks() {
        return heightBlocks;
    }

    /**
     * Future gameplay code can use this instead of editing NBT directly.
     */
    public void setDimensions(float widthBlocks, float heightBlocks) {
        this.widthBlocks = clampWidth(widthBlocks);
        this.heightBlocks = clampHeight(heightBlocks);
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat(WIDTH_NBT_KEY, widthBlocks);
        tag.putFloat(HEIGHT_NBT_KEY, heightBlocks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        widthBlocks = tag.contains(WIDTH_NBT_KEY)
                ? clampWidth(tag.getFloat(WIDTH_NBT_KEY))
                : DEFAULT_WIDTH_BLOCKS;

        heightBlocks = tag.contains(HEIGHT_NBT_KEY)
                ? clampHeight(tag.getFloat(HEIGHT_NBT_KEY))
                : DEFAULT_HEIGHT_BLOCKS;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
}
