package porker.pp_legendarydungeons.blocks.animated_blocks.crystal_heart;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart.CrystalHeartBlockEntity;
import porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart.CrystalHeartMiningMode;

/**
 * Invisible one-block anchor for the rendered Crystal Heart.
 *
 * <p>The actual crystal is drawn by a block-entity renderer and may be many
 * blocks larger than this anchor. Keeping the anchor as a normal block lets
 * structure files place and preserve it naturally.</p>
 *
 * <p>Survival mining is controlled per instance through block-entity NBT. Hearts
 * are unmineable by default. When enabled, any pickaxe can mine the anchor and
 * the configured mining mode determines whether it drops itself or rolls an
 * arbitrary loot table.</p>
 */
public final class CrystalHeartBlock extends BaseEntityBlock {
    public static final MapCodec<CrystalHeartBlock> CODEC =
            simpleCodec(CrystalHeartBlock::new);

    /** XP is tied to the built-in default reward table, not arbitrary overrides. */
    private static final int DEFAULT_MINING_XP_MIN = 20;
    private static final int DEFAULT_MINING_XP_MAX = 40;

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
    protected float getDestroyProgress(
            BlockState state,
            Player player,
            BlockGetter level,
            BlockPos pos
    ) {
        if (player.isCreative()) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        if (!(level.getBlockEntity(pos) instanceof CrystalHeartBlockEntity heart)
                || !heart.isMiningEnabled()) {
            return 0.0F;
        }

        if (!player.getMainHandItem().is(ItemTags.PICKAXES)) {
            return 0.0F;
        }

        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity,
            ItemStack tool
    ) {
        /*
         * Retain vanilla mining stats/exhaustion. The block itself has no normal
         * loot table, so all survival drops below are intentionally explicit.
         */
        super.playerDestroy(level, player, pos, state, blockEntity, tool);

        if (!(level instanceof ServerLevel serverLevel)
                || player.isCreative()
                || !(blockEntity instanceof CrystalHeartBlockEntity heart)
                || !heart.isMiningEnabled()) {
            return;
        }

        CrystalHeartMiningMode mode = heart.getMiningMode();

        if (mode == CrystalHeartMiningMode.SELF) {
            dropSelf(serverLevel, pos);
            return;
        }

        if (mode == CrystalHeartMiningMode.SILK_SELF_ELSE_LOOT
                && hasSilkTouch(serverLevel, tool)) {
            dropSelf(serverLevel, pos);
            return;
        }

        dropConfiguredLoot(serverLevel, player, pos, state, heart, tool);
    }

    private void dropSelf(ServerLevel level, BlockPos pos) {
        popResource(level, pos, new ItemStack(asItem()));
    }

    private static void dropConfiguredLoot(
            ServerLevel level,
            Player player,
            BlockPos pos,
            BlockState state,
            CrystalHeartBlockEntity heart,
            ItemStack tool
    ) {
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.TOOL, tool)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, heart)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.BLOCK);

        LootTable lootTable = level.getServer()
                .reloadableRegistries()
                .getLootTable(heart.getMiningLootTable());

        lootTable.getRandomItems(params)
                .forEach(stack -> popResource(level, pos, stack));

        /*
         * Vanilla loot tables generate item stacks. The default Crystal Heart XP
         * reward is therefore emitted here when the built-in table is selected.
         * A custom MiningLootTable fully replaces that built-in reward behavior.
         */
        if (heart.usesDefaultMiningLootTable()) {
            int amount = DEFAULT_MINING_XP_MIN
                    + level.getRandom().nextInt(
                            DEFAULT_MINING_XP_MAX - DEFAULT_MINING_XP_MIN + 1
                    );
            ExperienceOrb.award(level, Vec3.atCenterOf(pos), amount);
        }
    }

    private static boolean hasSilkTouch(ServerLevel level, ItemStack tool) {
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var silkTouch = enchantments.getOrThrow(Enchantments.SILK_TOUCH);
        return EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) > 0;
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
