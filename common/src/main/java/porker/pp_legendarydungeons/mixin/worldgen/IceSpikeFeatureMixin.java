package porker.pp_legendarydungeons.mixin.worldgen;

import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.IceSpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import porker.pp_legendarydungeons.worldgen.frozen_adventurers.FrozenAdventurerFeatureSubstitution;

/**
 * Gives every invocation of Minecraft's vanilla IceSpikeFeature a small chance
 * to become a frozen-adventurer spike.
 *
 * <p>This intentionally targets the feature implementation instead of editing
 * selected biome generation lists. Consequently, vanilla and modded biomes or
 * dimensions that reuse IceSpikeFeature are covered automatically. On a failed
 * roll, missing template, or invalid placement, the callback does not cancel and
 * the original vanilla feature continues unchanged.</p>
 */
@Mixin(IceSpikeFeature.class)
public abstract class IceSpikeFeatureMixin {
    @Inject(
            method = "place",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ppLegendaryDungeons$tryFrozenAdventurerSpike(
            FeaturePlaceContext<NoneFeatureConfiguration> context,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (FrozenAdventurerFeatureSubstitution.tryPlaceIceSpike(context)) {
            callback.setReturnValue(true);
        }
    }
}
