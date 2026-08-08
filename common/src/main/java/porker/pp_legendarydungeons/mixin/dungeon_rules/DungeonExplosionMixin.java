package porker.pp_legendarydungeons.mixin.dungeon_rules;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
    private ObjectArrayList<BlockPos> toBlow;

    /**
     * Wind charges and similar non-destructive effects use TRIGGER_BLOCK so
     * they can activate compatible buttons, levers, and other blocks. Their
     * affected-block list must remain intact for those triggers to fire.
     */
    private boolean ppLegendaryDungeons$isTriggerBlockExplosion() {
        Explosion explosion = (Explosion) (Object) this;
        return explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK;
    }

    /**
     * Preserve the explosion's entity damage and knockback, but remove any
     * affected block positions covered by an active EXPLOSIONS dungeon rule
     * before Minecraft performs the later block-destruction phase.
     *
     * <p>This works whether the explosion begins inside or outside the dungeon.
     * TRIGGER_BLOCK explosions are intentionally left unchanged so wind-charge
     * redstone mechanics continue to work.</p>
     */
    @Inject(method = "explode", at = @At("RETURN"))
    private void ppLegendaryDungeons$protectDungeonBlocks(CallbackInfo callback) {
        if (!(level instanceof ServerLevel serverLevel)
                || ppLegendaryDungeons$isTriggerBlockExplosion()) {
            return;
        }

        toBlow.removeIf(pos -> DungeonRuleManager.isRuleEnforced(
                serverLevel,
                pos,
                DungeonRule.EXPLOSIONS
        ));
    }
}
