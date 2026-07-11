
package porker.pp_legendarydungeons.dungeon_rules.events;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRule;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleFeedback;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRulePermissions;
import porker.pp_legendarydungeons.dungeon_rules.network.DungeonRuleNetworking;
import porker.pp_legendarydungeons.setup.ModBlocks;
import porker.pp_legendarydungeons.setup.ModTags;

/**
 * Interaction-level restrictions for existing blocks and portable items.
 */
public final class DungeonRuleInteractionEvents {
    private DungeonRuleInteractionEvents() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockState state = world.getBlockState(hitResult.getBlockPos());

            if (state.is(ModBlocks.DUNGEON_RULE_PARENT)
                    || state.is(ModBlocks.DUNGEON_RULE_ZONE)) {
                if (!world.isClientSide
                        && player instanceof ServerPlayer serverPlayer
                        && DungeonRulePermissions.canBypass(serverPlayer)) {
                    DungeonRuleNetworking.openEditor(serverPlayer, hitResult.getBlockPos());
                }

                return InteractionResult.SUCCESS;
            }

            if (!(world instanceof ServerLevel level)
                    || !(player instanceof ServerPlayer serverPlayer)
                    || DungeonRulePermissions.canBypass(player)) {
                return InteractionResult.PASS;
            }

            ItemStack heldStack = player.getItemInHand(hand);

            if (heldStack.is(ModTags.DUNGEON_PORTABLE_PC_ITEMS)
                    && DungeonRuleManager.isRuleEnforced(
                    level,
                    player.blockPosition(),
                    DungeonRule.PORTABLE_PC_USE
            )) {
                DungeonRuleFeedback.denied(
                        serverPlayer,
                        "Portable PC access is disabled inside this active dungeon."
                );
                return InteractionResult.FAIL;
            }

            if (heldStack.is(ModTags.DUNGEON_PORTABLE_HEALER_ITEMS)
                    && DungeonRuleManager.isRuleEnforced(
                    level,
                    player.blockPosition(),
                    DungeonRule.PORTABLE_HEALER_USE
            )) {
                DungeonRuleFeedback.denied(
                        serverPlayer,
                        "Portable healing is disabled inside this active dungeon."
                );
                return InteractionResult.FAIL;
            }

            if (state.is(BlockTags.BEDS)
                    && DungeonRuleManager.isRuleEnforced(
                    level,
                    hitResult.getBlockPos(),
                    DungeonRule.BED_USE
            )) {
                DungeonRuleFeedback.denied(
                        serverPlayer,
                        "Beds cannot be used inside this active dungeon."
                );
                return InteractionResult.FAIL;
            }

            if (state.is(ModTags.DUNGEON_PC_BLOCKS)
                    && DungeonRuleManager.isRuleEnforced(
                    level,
                    hitResult.getBlockPos(),
                    DungeonRule.PC_USE
            )) {
                DungeonRuleFeedback.denied(
                        serverPlayer,
                        "PC access is disabled inside this active dungeon."
                );
                return InteractionResult.FAIL;
            }

            if (state.is(ModTags.DUNGEON_HEALER_BLOCKS)
                    && DungeonRuleManager.isRuleEnforced(
                    level,
                    hitResult.getBlockPos(),
                    DungeonRule.HEALER_USE
            )) {
                DungeonRuleFeedback.denied(
                        serverPlayer,
                        "Healing stations are disabled inside this active dungeon."
                );
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);

            if (!(world instanceof ServerLevel level)
                    || !(player instanceof ServerPlayer serverPlayer)
                    || DungeonRulePermissions.canBypass(player)) {
                return InteractionResultHolder.pass(stack);
            }

            if (stack.is(ModTags.DUNGEON_PORTABLE_PC_ITEMS)
                    && DungeonRuleManager.isRuleEnforced(
                    level,
                    player.blockPosition(),
                    DungeonRule.PORTABLE_PC_USE
            )) {
                DungeonRuleFeedback.denied(
                        serverPlayer,
                        "Portable PC access is disabled inside this active dungeon."
                );
                return InteractionResultHolder.fail(stack);
            }

            if (stack.is(ModTags.DUNGEON_PORTABLE_HEALER_ITEMS)
                    && DungeonRuleManager.isRuleEnforced(
                    level,
                    player.blockPosition(),
                    DungeonRule.PORTABLE_HEALER_USE
            )) {
                DungeonRuleFeedback.denied(
                        serverPlayer,
                        "Portable healing is disabled inside this active dungeon."
                );
                return InteractionResultHolder.fail(stack);
            }

            return InteractionResultHolder.pass(stack);
        });
    }
}
