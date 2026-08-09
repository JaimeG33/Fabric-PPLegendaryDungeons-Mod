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
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart.CrystalHeartBlockEntity;
import porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart.CrystalHeartPreset;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renders the Crystal Heart as a large, slowly rotating textured crystal.
 * Animation is derived from client world time and requires no server ticking or
 * per-tick network packets.
 */
public final class CrystalHeartBlockEntityRenderer
        implements BlockEntityRenderer<CrystalHeartBlockEntity> {
    /**
     * Texture selection is preset-driven. All current presets point to the active
     * original/base artwork; a future shape can opt into dedicated art by changing
     * CrystalHeartPreset without adding block-ID-specific renderer branches.
     */
    private static final Map<CrystalHeartPreset, TexturePair> PRESET_TEXTURES =
            createPresetTextures();

    /** 0.5 degrees/tick = 10 degrees/second = one turn every 36 seconds. */
    private static final float DEGREES_PER_TICK = 0.5F;

    /** Slow, subtle hover: roughly one full bob cycle every 7.85 seconds. */
    private static final float BOB_RADIANS_PER_TICK = 0.04F;

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
        float bobOffset = Mth.sin(time * BOB_RADIANS_PER_TICK)
                * blockEntity.getBobAmplitudeBlocks();
        CrystalHeartPreset preset = blockEntity.getPreset();

        poseStack.pushPose();

        // Center over the anchor, add the subtle hover, then rotate around Y.
        poseStack.translate(0.5D, BOTTOM_POINT_OFFSET + bobOffset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

        // Every normalized preset still uses the same width/height NBT scaling.
        poseStack.scale(
                blockEntity.getWidthBlocks(),
                blockEntity.getHeightBlocks(),
                blockEntity.getWidthBlocks()
        );

        TexturePair textures = resolveTextures(preset);

        /*
         * Top and bottom halves deliberately use separate 128x128 textures.
         * Each physical face can therefore sample an entire high-detail triangle
         * instead of sharing a tiny cell in an eight-face atlas.
         */
        VertexConsumer topConsumer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(textures.top())
        );

        CrystalHeartGeometry.renderTop(
                poseStack.last(),
                topConsumer,
                preset,
                LightTexture.FULL_BRIGHT,
                packedOverlay
        );

        VertexConsumer bottomConsumer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(textures.bottom())
        );

        CrystalHeartGeometry.renderBottom(
                poseStack.last(),
                bottomConsumer,
                preset,
                LightTexture.FULL_BRIGHT,
                packedOverlay
        );

        poseStack.popPose();
    }

    private static Map<CrystalHeartPreset, TexturePair> createPresetTextures() {
        EnumMap<CrystalHeartPreset, TexturePair> textures =
                new EnumMap<>(CrystalHeartPreset.class);

        for (CrystalHeartPreset preset : CrystalHeartPreset.values()) {
            textures.put(
                    preset,
                    texturePair(preset.textureFolder(), preset.textureBaseName())
            );
        }

        return textures;
    }

    private static TexturePair resolveTextures(CrystalHeartPreset preset) {
        TexturePair textures = PRESET_TEXTURES.get(preset);
        return textures != null
                ? textures
                : PRESET_TEXTURES.get(CrystalHeartPreset.BASE);
    }

    private static TexturePair texturePair(String folder, String baseName) {
        String base = "textures/entity/animated_blocks/" + folder + "/" + baseName;

        return new TexturePair(
                ResourceLocation.fromNamespaceAndPath(
                        LegendaryDungeons.MOD_ID,
                        base + "_top.png"
                ),
                ResourceLocation.fromNamespaceAndPath(
                        LegendaryDungeons.MOD_ID,
                        base + "_bottom.png"
                )
        );
    }

    @Override
    public boolean shouldRenderOffScreen(CrystalHeartBlockEntity blockEntity) {
        // Required because the visual can be far larger than the anchor section.
        return true;
    }

    /**
     * Diagnostic culling mode.
     *
     * <p>The previous calculated AABB did not eliminate the look-angle
     * disappearance. The common source set cannot reference NeoForge's
     * loader-added {@code AABB.INFINITE} constant, so this diagnostic uses a
     * very large finite vanilla AABB instead. This does not load chunks and does
     * not override
     * {@link #getViewDistance()}; the block entity still has to exist client-side
     * before it can render.</p>
     *
     * <p>The common/Fabric compile does not expose NeoForge's renderer extension,
     * so this intentionally has no {@code @Override} annotation.</p>
     */
    private static final AABB DIAGNOSTIC_RENDER_BOUNDS = new AABB(
            -60_000_000.0D,
            -60_000_000.0D,
            -60_000_000.0D,
            60_000_000.0D,
            60_000_000.0D,
            60_000_000.0D
    );

    @SuppressWarnings("unused")
    public AABB getRenderBoundingBox(CrystalHeartBlockEntity blockEntity) {
        return DIAGNOSTIC_RENDER_BOUNDS;
    }

    @Override
    public int getViewDistance() {
        /*
         * Match the player's configured chunk render distance instead of using
         * Minecraft's short default block-entity distance. This does NOT force
         * chunks to load; if the crystal's chunk is not rendered/loaded, the
         * block entity is not available to draw.
         */
        int renderDistanceChunks = Minecraft.getInstance().options.renderDistance().get();
        return Math.max(64, renderDistanceChunks * 16 + 32);
    }

    private record TexturePair(ResourceLocation top, ResourceLocation bottom) {
    }
}
