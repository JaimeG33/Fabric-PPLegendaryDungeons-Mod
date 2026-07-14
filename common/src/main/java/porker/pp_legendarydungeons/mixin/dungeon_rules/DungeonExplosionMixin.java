package porker.pp_legendarydungeons.mixin.dungeon_rules;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRule;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;

@Mixin(Explosion.class)
public abstract class DungeonExplosionMixin {
    @Shadow
    @Final
    private Level level;

    @Shadow
    @Final
    private double x;

    @Shadow
    @Final
    private double y;

    @Shadow
    @Final
    private double z;

    @Shadow
    @Final
    private ObjectArrayList<BlockPos> toBlow;

    /**
     * If the explosion itself starts inside a protected box, cancel the whole
     * explosion before it can damage blocks or entities.
     */
    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void ppLegendaryDungeons$blockDungeonExplosion(CallbackInfo callback) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos center = new BlockPos(
                Mth.floor(x),
                Mth.floor(y),
                Mth.floor(z)
        );

        if (DungeonRuleManager.isRuleEnforced(
                serverLevel,
                center,
                DungeonRule.EXPLOSIONS
        )) {
            callback.cancel();
        }
    }

    /**
     * An explosion can begin outside a protected box but still reach blocks
     * inside it. Remove those protected block positions from the final list so
     * the blast cannot destroy them. Entity damage from an outside-origin blast
     * is intentionally left unchanged.
     */
    @Inject(method = "explode", at = @At("RETURN"))
    private void ppLegendaryDungeons$protectDungeonBlocksFromOutsideBlast(CallbackInfo callback) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        toBlow.removeIf(pos -> DungeonRuleManager.isRuleEnforced(
                serverLevel,
                pos,
                DungeonRule.EXPLOSIONS
        ));
    }
}
