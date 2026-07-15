package porker.pp_legendarydungeons.blocks.dungeon_rules;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleZoneBlockEntity;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;

/**
 * Invisible child-zone controller.
 *
 * <p>The horizontal facing is intentionally stored in the block state. Structure
 * templates rotate block states during placement, allowing the linked block entity
 * to reconstruct how its local X/Z zone definition should be rotated in world
 * space.</p>
 */
public final class DungeonRuleZoneBlock extends AbstractDungeonRuleBlock {
    public static final MapCodec<DungeonRuleZoneBlock> CODEC =
            simpleCodec(DungeonRuleZoneBlock::new);

    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    public DungeonRuleZoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected MapCodec<? extends DungeonRuleZoneBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(
                FACING,
                rotation.rotate(state.getValue(FACING))
        );
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(
                mirror.getRotation(state.getValue(FACING))
        );
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
