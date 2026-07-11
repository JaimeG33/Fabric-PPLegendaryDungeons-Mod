
package porker.pp_legendarydungeons.mixin.dungeon_rules;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRule;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleFeedback;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRulePermissions;

@Mixin(ServerPlayerGameMode.class)
public abstract class DungeonBlockBreakMixin {
    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void ppLegendaryDungeons$blockDungeonBreaking(
            BlockPos pos,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (DungeonRulePermissions.canBypass(player)) {
            return;
        }

        if (DungeonRuleManager.isRuleEnforced(
                player.serverLevel(),
                pos,
                DungeonRule.BLOCK_BREAKING
        )) {
            DungeonRuleFeedback.denied(
                    player,
                    "Blocks cannot be broken inside this active dungeon."
            );
            callback.setReturnValue(false);
        }
    }
}
