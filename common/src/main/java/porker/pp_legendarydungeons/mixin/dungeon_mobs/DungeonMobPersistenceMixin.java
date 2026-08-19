package porker.pp_legendarydungeons.mixin.dungeon_mobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import porker.pp_legendarydungeons.features.dungeon_mobs.DungeonMobDataHolder;
import porker.pp_legendarydungeons.features.dungeon_mobs.DungeonMobManager;
import porker.pp_legendarydungeons.features.dungeon_mobs.DungeonMobPersistentData;

/** Adds one isolated persistent encounter compound to ordinary vanilla mobs. */
@Mixin(Mob.class)
public abstract class DungeonMobPersistenceMixin implements DungeonMobDataHolder {
    @Unique
    private DungeonMobPersistentData ppLegendaryDungeons$dungeonMobData;

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

        if (!tag.contains(DungeonMobPersistentData.ROOT_NBT_KEY)) {
            ppLegendaryDungeons$dungeonMobData = null;
            if (mob.level() instanceof ServerLevel) {
                DungeonMobManager.unregister(mob.getUUID());
            }
            return;
        }

        DungeonMobPersistentData.load(
                tag.getCompound(DungeonMobPersistentData.ROOT_NBT_KEY),
                mob.blockPosition()
        ).ifPresentOrElse(data -> {
            ppLegendaryDungeons$dungeonMobData = data;
            if (mob.level() instanceof ServerLevel) {
                DungeonMobManager.registerLoaded(mob, data);
            }
        }, () -> {
            ppLegendaryDungeons$dungeonMobData = null;
            if (mob.level() instanceof ServerLevel) {
                DungeonMobManager.unregister(mob.getUUID());
            }
        });
    }
}
