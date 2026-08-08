package porker.pp_legendarydungeons.client.animated_blocks.crystal_heart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart.CrystalHeartBlockEntity;
import porker.pp_legendarydungeons.setup.ModBlocks;

/**
 * Renders the Crystal Heart as a large, slowly rotating textured bipyramid.
 * Animation is derived from client world time and requires no server ticking or
 * per-tick network packets.
 */
public final class CrystalHeartBlockEntityRenderer
        implements BlockEntityRenderer<CrystalHeartBlockEntity> {
    private static final ResourceLocation TEXTURE_A = ResourceLocation.fromNamespaceAndPath(
            LegendaryDungeons.MOD_ID,
            "textures/entity/animated_blocks/crystal_heart/crystal_heart_a.png"
    );
    private static final ResourceLocation TEXTURE_B = ResourceLocation.fromNamespaceAndPath(
            LegendaryDungeons.MOD_ID,
            "textures/entity/animated_blocks/crystal_heart/crystal_heart_b.png"
    );
    private static final ResourceLocation TEXTURE_C = ResourceLocation.fromNamespaceAndPath(
            LegendaryDungeons.MOD_ID,
            "textures/entity/animated_blocks/crystal_heart/crystal_heart_c.png"
    );

    /** 0.5 degrees/tick = 10 degrees/second = one turn every 36 seconds. */
    private static final float DEGREES_PER_TICK = 0.5F;

    /** Slow, subtle hover: roughly one full bob cycle every 7.85 seconds. */
    private static final float BOB_RADIANS_PER_TICK = 0.04F;
    private static final float BOB_AMPLITUDE_BLOCKS = 0.18F;

    /**
     * Keeps the bottom point slightly above the invisible anchor block's base,
     * preserving the old floor-anchored particle-heart placement style while
     * still making the rendered model read as floating.
     */
    private static final double BOTTOM_POINT_OFFSET = 0.25D;

    public CrystalHeartBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            CrystalHeartBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        float time = blockEntity.getLevel().getGameTime() + partialTick;
        float rotationDegrees = (time * DEGREES_PER_TICK) % 360.0F;
        float bobOffset = Mth.sin(time * BOB_RADIANS_PER_TICK) * BOB_AMPLITUDE_BLOCKS;

        poseStack.pushPose();

        // Center over the anchor, add the subtle hover, then rotate around Y.
        poseStack.translate(0.5D, BOTTOM_POINT_OFFSET + bobOffset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

        // One normalized mesh can now represent any width/height combination.
        poseStack.scale(
                blockEntity.getWidthBlocks(),
                blockEntity.getHeightBlocks(),
                blockEntity.getWidthBlocks()
        );

        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityTranslucent(resolveTexture(blockEntity.getBlockState().getBlock()))
        );

        // Full-bright keeps the dungeon landmark readable in a dark crystal cave.
        CrystalHeartGeometry.render(
                poseStack.last(),
                consumer,
                LightTexture.FULL_BRIGHT,
                packedOverlay
        );

        poseStack.popPose();
    }

    private static ResourceLocation resolveTexture(Block block) {
        if (block == ModBlocks.CRYSTAL_HEART_C.get()) {
            return TEXTURE_C;
        }

        if (block == ModBlocks.CRYSTAL_HEART_B.get()) {
            return TEXTURE_B;
        }

        return TEXTURE_A;
    }

    @Override
    public boolean shouldRenderOffScreen(CrystalHeartBlockEntity blockEntity) {
        // The visual can be much larger than the invisible one-block anchor.
        return true;
    }

    @Override
    public int getViewDistance() {
        /*
         * Match the player's configured chunk render distance instead of using
         * Minecraft's short default block-entity distance. This does NOT force
         * chunks to load; if the crystal's chunk is not rendered/loaded, the
         * block entity is not available to draw.
         *
         * A small margin keeps very large crystal geometry from disappearing
         * exactly when the anchor reaches the edge of the player's view.
         */
        int renderDistanceChunks = Minecraft.getInstance().options.renderDistance().get();
        return Math.max(64, renderDistanceChunks * 16 + 32);
    }
}
