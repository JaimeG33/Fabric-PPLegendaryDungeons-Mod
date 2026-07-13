package porker.pp_legendarydungeons.dungeon_rules.events;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRule;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleFeedback;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRulePermissions;
import porker.pp_legendarydungeons.setup.ModTags;

/**
 * Loader-neutral block breaking and placement restrictions.
 *
 * <p>These Architectury events replace the former direct mixins into
 * {@code ServerPlayerGameMode} and {@code BlockItem}. The allowlist is checked
 * before the dungeon rule and only bypasses this mod's restriction.</p>
 */
public final class DungeonRuleBlockEvents {
    private static boolean registered = false;

    private DungeonRuleBlockEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        BlockEvent.BREAK.register(DungeonRuleBlockEvents::onBreakBlock);
        BlockEvent.PLACE.register(DungeonRuleBlockEvents::onPlaceBlock);
    }

    private static EventResult onBreakBlock(
            Level level,
            BlockPos pos,
            BlockState state,
            ServerPlayer player,
            dev.architectury.utils.value.IntValue experience
    ) {
        if (!(level instanceof ServerLevel serverLevel)
                || DungeonRulePermissions.canBypass(player)
                || state.is(ModTags.DUNGEON_ALLOWED_BREAKS)) {
            return EventResult.pass();
        }

        if (DungeonRuleManager.isRuleEnforced(
                serverLevel,
                pos,
                DungeonRule.BLOCK_BREAKING
        )) {
            DungeonRuleFeedback.denied(
                    player,
                    "Blocks cannot be broken inside this active dungeon."
            );
            return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }

    private static EventResult onPlaceBlock(
            Level level,
            BlockPos pos,
            BlockState state,
            Entity placer
    ) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(placer instanceof ServerPlayer serverPlayer)
                || DungeonRulePermissions.canBypass(serverPlayer)
                || state.is(ModTags.DUNGEON_ALLOWED_PLACES)) {
            return EventResult.pass();
        }

        /*
         * Architectury supplies the actual future block state and placement
         * position. This is more precise than checking only the BlockItem's
         * default block and the originally clicked position.
         */
        if (DungeonRuleManager.isRuleEnforced(
                serverLevel,
                pos,
                DungeonRule.BLOCK_PLACEMENT
        )) {
            DungeonRuleFeedback.denied(
                    serverPlayer,
                    "Blocks cannot be placed inside this active dungeon."
            );
            return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }
}
