package porker.pp_legendarydungeons.client.animated_blocks;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import porker.pp_legendarydungeons.client.animated_blocks.crystal_heart.CrystalHeartBlockEntityRenderer;
import porker.pp_legendarydungeons.setup.ModBlockEntities;

/**
 * Central client-only registration point for animated block-entity renderers.
 * Add future animated blocks here while keeping each implementation in its own
 * subpackage.
 */
public final class AnimatedBlockEntityRenderers {
    private static boolean registered = false;

    private AnimatedBlockEntityRenderers() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        BlockEntityRendererRegistry.register(
                ModBlockEntities.CRYSTAL_HEART.get(),
                CrystalHeartBlockEntityRenderer::new
        );
    }
}
