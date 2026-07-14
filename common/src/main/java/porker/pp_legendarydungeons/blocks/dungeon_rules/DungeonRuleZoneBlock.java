
package porker.pp_legendarydungeons.blocks.dungeon_rules;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleZoneBlockEntity;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;

public final class DungeonRuleZoneBlock extends AbstractDungeonRuleBlock {
    public static final MapCodec<DungeonRuleZoneBlock> CODEC =
            simpleCodec(DungeonRuleZoneBlock::new);

    public DungeonRuleZoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DungeonRuleZoneBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonRuleZoneBlockEntity(pos, state);
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
            DungeonRuleManager.removeZone(serverLevel, pos);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
