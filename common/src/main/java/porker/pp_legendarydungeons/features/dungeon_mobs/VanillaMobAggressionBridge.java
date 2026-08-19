package porker.pp_legendarydungeons.features.dungeon_mobs;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Small compatibility boundary around vanilla mob targeting.
 *
 * <p>The encounter system chooses a target but intentionally does not replace
 * the mob's attack goals. Pillagers still use their crossbow logic, drowned
 * still use their native melee/trident logic, and guardians still use their
 * native beam goal when those goals accept the selected target.</p>
 */
public final class VanillaMobAggressionBridge {
    private VanillaMobAggressionBridge() {
    }

    public static void setTarget(Mob mob, LivingEntity target) {
        mob.setTarget(target);
    }

    public static void clearTarget(Mob mob) {
        mob.setTarget(null);
    }
}
