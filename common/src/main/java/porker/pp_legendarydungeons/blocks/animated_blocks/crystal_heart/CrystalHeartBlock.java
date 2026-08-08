package porker.pp_legendarydungeons.blocks.animated_blocks.crystal_heart;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart.CrystalHeartBlockEntity;

/**
 * Invisible one-block anchor for the rendered Crystal Heart.
 *
 * <p>The actual crystal is drawn by a block-entity renderer and may be many
 * blocks larger than this anchor. Keeping the anchor as a normal block lets
 * structure files place and preserve it naturally.</p>
 */
public final class CrystalHeartBlock extends BaseEntityBlock {
    public static final MapCodec<CrystalHeartBlock> CODEC =
            simpleCodec(CrystalHeartBlock::new);

    public CrystalHeartBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends CrystalHeartBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalHeartBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return Shapes.block();
    }
}
