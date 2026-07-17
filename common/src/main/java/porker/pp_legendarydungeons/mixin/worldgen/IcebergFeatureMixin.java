package porker.pp_legendarydungeons.mixin.worldgen;

import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.IcebergFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import porker.pp_legendarydungeons.worldgen.frozen_adventurers.FrozenAdventurerFeatureSubstitution;

/**
 * Gives every invocation of Minecraft's vanilla IcebergFeature a small chance
 * to become a prebuilt frozen-adventurer iceberg.
 *
 * <p>The mixin is deliberately attached to IcebergFeature rather than to the
 * vanilla frozen-ocean biomes. Any modded biome or configured feature that
 * reuses the vanilla implementation is therefore eligible. The configured
 * iceberg block must still belong to the mod's iceberg-block tag. If the custom
 * replacement cannot be prepared, vanilla generation proceeds normally.</p>
 */
@Mixin(IcebergFeature.class)
public abstract class IcebergFeatureMixin {
    @Inject(
            method = "place",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ppLegendaryDungeons$tryFrozenAdventurerIceberg(
            FeaturePlaceContext<BlockStateConfiguration> context,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (FrozenAdventurerFeatureSubstitution.tryPlaceIceberg(context)) {
            callback.setReturnValue(true);
        }
    }
}
