
package porker.pp_legendarydungeons.blocks.dungeon_rules;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleParentBlockEntity;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;

public final class DungeonRuleParentBlock extends AbstractDungeonRuleBlock {
    public static final MapCodec<DungeonRuleParentBlock> CODEC =
            simpleCodec(DungeonRuleParentBlock::new);

    public DungeonRuleParentBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DungeonRuleParentBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonRuleParentBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (state.getBlock() != newState.getBlock()
                && level instanceof ServerLevel serverLevel) {
            DungeonRuleManager.removeParent(serverLevel, pos);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
