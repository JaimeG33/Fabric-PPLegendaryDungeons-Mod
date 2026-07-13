package porker.pp_legendarydungeons.dungeon_rules.events;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRule;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleFeedback;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRulePermissions;
import porker.pp_legendarydungeons.dungeon_rules.network.DungeonRuleNetworking;
import porker.pp_legendarydungeons.setup.ModBlocks;
import porker.pp_legendarydungeons.setup.ModTags;

/**
 * Loader-neutral right-click restrictions for dungeon controller blocks,
 * existing utility blocks, and portable utility items.
 *
 * <p>The editor-opening call still delegates to the current Fabric networking
 * implementation. The event registration itself is shared Architectury code;
 * the networking call will become fully loader-neutral in Phase 5.</p>
 */
public final class DungeonRuleInteractionEvents {
    private static boolean registered = false;

    private DungeonRuleInteractionEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        InteractionEvent.RIGHT_CLICK_BLOCK.register(
                DungeonRuleInteractionEvents::onRightClickBlock
        );
        InteractionEvent.RIGHT_CLICK_ITEM.register(
                DungeonRuleInteractionEvents::onRightClickItem
        );
    }

    private static EventResult onRightClickBlock(
            Player player,
            InteractionHand hand,
            BlockPos pos,
            Direction face
    ) {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);

        /*
         * Preserve the previous controller-block behavior:
         * - consume the interaction on both logical sides;
         * - only creative operators open the editor;
         * - perform the actual screen/network action on the server.
         */
        if (state.is(ModBlocks.DUNGEON_RULE_PARENT.get())
                || state.is(ModBlocks.DUNGEON_RULE_ZONE.get())) {
            if (!level.isClientSide
                    && player instanceof ServerPlayer serverPlayer
                    && DungeonRulePermissions.canBypass(serverPlayer)) {
                DungeonRuleNetworking.openEditor(serverPlayer, pos);
            }

            return EventResult.interruptTrue();
        }

        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)
                || DungeonRulePermissions.canBypass(player)) {
            return EventResult.pass();
        }

        ItemStack heldStack = player.getItemInHand(hand);

        if (heldStack.is(ModTags.DUNGEON_PORTABLE_PC_ITEMS)
                && DungeonRuleManager.isRuleEnforced(
                serverLevel,
                player.blockPosition(),
                DungeonRule.PORTABLE_PC_USE
        )) {
            DungeonRuleFeedback.denied(
                    serverPlayer,
                    "Portable PC access is disabled inside this active dungeon."
            );
            return EventResult.interruptFalse();
        }

        if (heldStack.is(ModTags.DUNGEON_PORTABLE_HEALER_ITEMS)
                && DungeonRuleManager.isRuleEnforced(
                serverLevel,
                player.blockPosition(),
                DungeonRule.PORTABLE_HEALER_USE
        )) {
            DungeonRuleFeedback.denied(
                    serverPlayer,
                    "Portable healing is disabled inside this active dungeon."
            );
            return EventResult.interruptFalse();
        }

        if (state.is(BlockTags.BEDS)
                && DungeonRuleManager.isRuleEnforced(
                serverLevel,
                pos,
                DungeonRule.BED_USE
        )) {
            DungeonRuleFeedback.denied(
                    serverPlayer,
                    "Beds cannot be used inside this active dungeon."
            );
            return EventResult.interruptFalse();
        }

        if (state.is(ModTags.DUNGEON_PC_BLOCKS)
                && DungeonRuleManager.isRuleEnforced(
                serverLevel,
                pos,
                DungeonRule.PC_USE
        )) {
            DungeonRuleFeedback.denied(
                    serverPlayer,
                    "PC access is disabled inside this active dungeon."
            );
            return EventResult.interruptFalse();
        }

        if (state.is(ModTags.DUNGEON_HEALER_BLOCKS)
                && DungeonRuleManager.isRuleEnforced(
                serverLevel,
                pos,
                DungeonRule.HEALER_USE
        )) {
            DungeonRuleFeedback.denied(
                    serverPlayer,
                    "Healing stations are disabled inside this active dungeon."
            );
            return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }

    private static CompoundEventResult<ItemStack> onRightClickItem(
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        Level level = player.level();

        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)
                || DungeonRulePermissions.canBypass(player)) {
            return CompoundEventResult.pass();
        }

        if (stack.is(ModTags.DUNGEON_PORTABLE_PC_ITEMS)
                && DungeonRuleManager.isRuleEnforced(
                serverLevel,
                player.blockPosition(),
                DungeonRule.PORTABLE_PC_USE
        )) {
            DungeonRuleFeedback.denied(
                    serverPlayer,
                    "Portable PC access is disabled inside this active dungeon."
            );
            return CompoundEventResult.interruptFalse(stack);
        }

        if (stack.is(ModTags.DUNGEON_PORTABLE_HEALER_ITEMS)
                && DungeonRuleManager.isRuleEnforced(
                serverLevel,
                player.blockPosition(),
                DungeonRule.PORTABLE_HEALER_USE
        )) {
            DungeonRuleFeedback.denied(
                    serverPlayer,
                    "Portable healing is disabled inside this active dungeon."
            );
            return CompoundEventResult.interruptFalse(stack);
        }

        return CompoundEventResult.pass();
    }
}
