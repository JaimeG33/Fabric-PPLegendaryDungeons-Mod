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
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart.CrystalHeartBlockEntity;

/**
 * Renders the Crystal Heart as a large, slowly rotating textured bipyramid.
 * Animation is derived from client world time and requires no server ticking or
 * per-tick network packets.
 */
public final class CrystalHeartBlockEntityRenderer
        implements BlockEntityRenderer<CrystalHeartBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LegendaryDungeons.MOD_ID,
            "textures/entity/animated_blocks/crystal_heart/crystal_heart.png"
    );

    /** 0.5 degrees/tick = 10 degrees/second = one turn every 36 seconds. */
    private static final float DEGREES_PER_TICK = 0.5F;

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

        poseStack.pushPose();

        // Center over the one-block anchor and lift the bottom point off the floor.
        poseStack.translate(0.5D, BOTTOM_POINT_OFFSET, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

        // One normalized mesh can now represent any width/height combination.
        poseStack.scale(
                blockEntity.getWidthBlocks(),
                blockEntity.getHeightBlocks(),
                blockEntity.getWidthBlocks()
        );

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));

        // Full-bright keeps the dungeon landmark readable in a dark crystal cave.
        CrystalHeartGeometry.render(
                poseStack.last(),
                consumer,
                LightTexture.FULL_BRIGHT,
                packedOverlay
        );

        poseStack.popPose();
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
