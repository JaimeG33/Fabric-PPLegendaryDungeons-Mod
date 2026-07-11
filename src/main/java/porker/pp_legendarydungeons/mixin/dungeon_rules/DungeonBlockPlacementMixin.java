package porker.pp_legendarydungeons.mixin.dungeon_rules;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRule;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleFeedback;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRulePermissions;
import porker.pp_legendarydungeons.setup.ModTags;

@Mixin(BlockItem.class)
public abstract class DungeonBlockPlacementMixin {
    @Inject(
            method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ppLegendaryDungeons$blockDungeonPlacement(
            BlockPlaceContext context,
            CallbackInfoReturnable<InteractionResult> callback
    ) {
        Player player = context.getPlayer();

        if (!(player instanceof ServerPlayer serverPlayer)
                || DungeonRulePermissions.canBypass(player)) {
            return;
        }

        /*
         * This mixin runs on BlockItem, so the item being used can identify its
         * primary placed block without performing another world lookup.
         *
         * StandingAndWallBlockItem entries such as torches report their standing
         * block here, which is why tagging minecraft:torch and
         * minecraft:soul_torch allows both floor and wall placement.
         */
        BlockItem blockItem = (BlockItem) (Object) this;

        if (blockItem.getBlock()
                .defaultBlockState()
                .is(ModTags.DUNGEON_ALLOWED_PLACES)) {
            return;
        }

        if (DungeonRuleManager.isRuleEnforced(
                serverPlayer.serverLevel(),
                context.getClickedPos(),
                DungeonRule.BLOCK_PLACEMENT
        )) {
            DungeonRuleFeedback.denied(
                    serverPlayer,
                    "Blocks cannot be placed inside this active dungeon."
            );
            callback.setReturnValue(InteractionResult.FAIL);
        }
    }
}
