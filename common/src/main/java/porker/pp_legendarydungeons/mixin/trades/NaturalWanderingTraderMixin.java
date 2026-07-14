package porker.pp_legendarydungeons.mixin.trades;

import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import porker.pp_legendarydungeons.features.wtraders.natural.NaturalWanderingTraderService;

/**
 * Runs after vanilla has assembled a wandering trader's normal offers.
 *
 * <p>The service verifies that this entity is the UUID currently tracked by
 * vanilla's natural wandering-trader spawner. Command-spawned, spawn-egg,
 * structure-spawned, and other mods' traders are ignored in production.</p>
 */
@Mixin(WanderingTrader.class)
public abstract class NaturalWanderingTraderMixin {
    @Inject(
            method = "updateTrades",
            at = @At("TAIL")
    )
    private void ppLegendaryDungeons$applyNaturalTraderSelection(
            CallbackInfo callbackInfo
    ) {
        NaturalWanderingTraderService.applyAfterVanillaOffers(
                (WanderingTrader) (Object) this
        );
    }
}
