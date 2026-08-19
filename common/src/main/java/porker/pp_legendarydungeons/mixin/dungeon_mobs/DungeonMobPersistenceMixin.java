package porker.pp_legendarydungeons.mixin.dungeon_mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import porker.pp_legendarydungeons.features.dungeon_mobs.DungeonMobDataHolder;
import porker.pp_legendarydungeons.features.dungeon_mobs.DungeonMobManager;
import porker.pp_legendarydungeons.features.dungeon_mobs.DungeonMobPersistentData;
import porker.pp_legendarydungeons.features.dungeon_mobs.VanillaMobAggressionBridge;

/** Adds one isolated persistent encounter compound to ordinary vanilla mobs. */
@Mixin(Mob.class)
public abstract class DungeonMobPersistenceMixin implements DungeonMobDataHolder {
    @Unique
    private DungeonMobPersistentData ppLegendaryDungeons$dungeonMobData;

    @Unique
    private boolean ppLegendaryDungeons$pendingDungeonMobRegistration;

    @Unique
    private boolean ppLegendaryDungeons$resolveHomeOnRegistration;

    @Override
    public DungeonMobPersistentData ppLegendaryDungeons$getDungeonMobData() {
        return ppLegendaryDungeons$dungeonMobData;
    }

    @Override
    public void ppLegendaryDungeons$setDungeonMobData(
            DungeonMobPersistentData data
    ) {
        ppLegendaryDungeons$dungeonMobData = data;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void ppLegendaryDungeons$saveDungeonMobData(
            CompoundTag tag,
            CallbackInfo callback
    ) {
        if (ppLegendaryDungeons$dungeonMobData == null) {
            return;
        }

        Mob mob = (Mob) (Object) this;
        ppLegendaryDungeons$resolvePendingHome(mob);

        tag.put(
                DungeonMobPersistentData.ROOT_NBT_KEY,
                ppLegendaryDungeons$dungeonMobData.save()
        );
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void ppLegendaryDungeons$loadDungeonMobData(
            CompoundTag tag,
            CallbackInfo callback
    ) {
        Mob mob = (Mob) (Object) this;
        ppLegendaryDungeons$pendingDungeonMobRegistration = false;
        ppLegendaryDungeons$resolveHomeOnRegistration = false;

        if (!tag.contains(DungeonMobPersistentData.ROOT_NBT_KEY)) {
            ppLegendaryDungeons$dungeonMobData = null;
            if (mob.level() instanceof ServerLevel) {
                DungeonMobManager.unregister(mob.getUUID());
            }
            return;
        }

        CompoundTag dungeonMobTag =
                tag.getCompound(DungeonMobPersistentData.ROOT_NBT_KEY);
        boolean hasSavedHome = dungeonMobTag.contains("Home");

        DungeonMobPersistentData.load(
                dungeonMobTag,
                BlockPos.ZERO
        ).ifPresentOrElse(data -> {
            ppLegendaryDungeons$dungeonMobData = data;

            /*
             * /summon applies the requested command coordinates after the custom
             * NBT read. Register on the first real mob tick instead, when both
             * position and UUID are final. Existing saved entities keep their
             * explicit Home value; only Home-less manual NBT resolves lazily.
             */
            if (mob.level() instanceof ServerLevel) {
                ppLegendaryDungeons$pendingDungeonMobRegistration = true;
                ppLegendaryDungeons$resolveHomeOnRegistration = !hasSavedHome;
            }
        }, () -> {
            ppLegendaryDungeons$dungeonMobData = null;
            if (mob.level() instanceof ServerLevel) {
                DungeonMobManager.unregister(mob.getUUID());
            }
        });
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void ppLegendaryDungeons$finishDungeonMobRegistration(
            CallbackInfo callback
    ) {
        if (!ppLegendaryDungeons$pendingDungeonMobRegistration
                || ppLegendaryDungeons$dungeonMobData == null) {
            return;
        }

        Mob mob = (Mob) (Object) this;
        if (!(mob.level() instanceof ServerLevel)) {
            return;
        }

        ppLegendaryDungeons$resolvePendingHome(mob);
        ppLegendaryDungeons$pendingDungeonMobRegistration = false;
        DungeonMobManager.registerLoaded(
                mob,
                ppLegendaryDungeons$dungeonMobData
        );
    }

    /**
     * Managed targets are retained between bounded manager updates. Vanilla
     * target selectors may still replace them with another valid target, but
     * they cannot immediately erase a manager target or intentionally select a
     * same-faction managed entity.
     */
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void ppLegendaryDungeons$protectDungeonMobTarget(
            LivingEntity target,
            CallbackInfo callback
    ) {
        Mob mob = (Mob) (Object) this;
        if (VanillaMobAggressionBridge.shouldCancelTargetChange(mob, target)) {
            callback.cancel();
        }
    }

    @Unique
    private void ppLegendaryDungeons$resolvePendingHome(Mob mob) {
        if (!ppLegendaryDungeons$resolveHomeOnRegistration
                || ppLegendaryDungeons$dungeonMobData == null) {
            return;
        }

        ppLegendaryDungeons$dungeonMobData =
                ppLegendaryDungeons$dungeonMobData.withHomePosition(
                        mob.blockPosition()
                );
        ppLegendaryDungeons$resolveHomeOnRegistration = false;
    }
}
