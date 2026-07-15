package porker.pp_legendarydungeons.mixin.worldgen;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import porker.pp_legendarydungeons.worldgen.structure.FixedJigsawRotationContext;

/**
 * Replaces only the root rotation chosen at the beginning of vanilla
 * JigsawPlacement.addPieces. When no FixedJigsawStructure is generating, the
 * context delegates back to vanilla Rotation.getRandom.
 */
@Mixin(JigsawPlacement.class)
public abstract class FixedJigsawRotationMixin {
    @Redirect(
            method = "addPieces",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Rotation;"
                            + "getRandom(Lnet/minecraft/util/RandomSource;)"
                            + "Lnet/minecraft/world/level/block/Rotation;"
            )
    )
    private static Rotation ppLegendaryDungeons$chooseRootRotation(
            RandomSource random
    ) {
        return FixedJigsawRotationContext.chooseRootRotation(random);
    }
}
