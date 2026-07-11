package porker.pp_legendarydungeons.dungeon_rules.instance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleZoneBlockEntity;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Server-persistent dungeon instance and child-zone registry.
 *
 * Stored in the overworld's DimensionDataStorage so all dimensions share one
 * authoritative table. Persisting child zones means a large rule box continues
 * to work even if the chunk containing its invisible controller block unloads.
 */
public final class DungeonRuleSavedData extends SavedData {
    private static final String DATA_NAME = "pp_legendarydungeons_dungeon_rules";
    private static final Factory<DungeonRuleSavedData> FACTORY =
            new Factory<>(
                    DungeonRuleSavedData::new,
                    DungeonRuleSavedData::load,
                    null
            );

    private final Map<String, DungeonInstanceRecord> instances = new LinkedHashMap<>();
    private final Map<String, DungeonZoneRecord> zones = new LinkedHashMap<>();

    public static DungeonRuleSavedData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(FACTORY, DATA_NAME);
    }

    public Collection<DungeonInstanceRecord> all() {
        return instances.values();
    }

    public Collection<DungeonZoneRecord> allZones() {
        return new ArrayList<>(zones.values());
    }

    public Optional<DungeonInstanceRecord> get(String instanceId) {
        return Optional.ofNullable(instances.get(instanceId));
    }

    public Optional<DungeonZoneRecord> getZone(String zoneKey) {
        return Optional.ofNullable(zones.get(zoneKey));
    }

    public DungeonInstanceRecord upsertParent(
            String instanceId,
            ResourceKey<Level> dimension,
            BlockPos parentPos,
            ResourceLocation presetId,
            int linkChannel,
            boolean enabled,
            DungeonRuleSet parentOverrides
    ) {
        DungeonInstanceRecord record = instances.get(instanceId);

        if (record == null) {
            record = new DungeonInstanceRecord(
                    instanceId,
                    dimension.location().toString(),
                    parentPos.asLong(),
                    presetId.toString(),
                    linkChannel,
                    enabled,
                    DungeonInstanceState.ACTIVE,
                    parentOverrides.toPackedInt()
            );
            instances.put(instanceId, record);
        } else {
            record.updateConfiguration(
                    dimension.location().toString(),
                    parentPos,
                    presetId,
                    linkChannel,
                    enabled,
                    parentOverrides
            );
        }

        setDirty();
        return record;
    }

    public DungeonZoneRecord upsertZone(
            String zoneKey,
            ResourceKey<Level> dimension,
            DungeonRuleZoneBlockEntity zone
    ) {
        DungeonZoneRecord record = zones.get(zoneKey);

        if (record == null) {
            record = new DungeonZoneRecord(
                    zoneKey,
                    dimension.location().toString(),
                    zone.getBlockPos().asLong(),
                    zone.getPresetId(),
                    zone.getLinkChannel(),
                    zone.getOffsetX(),
                    zone.getOffsetY(),
                    zone.getOffsetZ(),
                    zone.getSizeX(),
                    zone.getSizeY(),
                    zone.getSizeZ(),
                    zone.getPriority(),
                    (int) zone.getMaximumParentDistance(),
                    zone.getRuleOverrides().toPackedInt(),
                    ""
            );
            zones.put(zoneKey, record);
        } else {
            record.update(
                    dimension.location().toString(),
                    zone.getBlockPos(),
                    zone.getPresetResourceLocation(),
                    zone.getLinkChannel(),
                    zone.getOffsetX(),
                    zone.getOffsetY(),
                    zone.getOffsetZ(),
                    zone.getSizeX(),
                    zone.getSizeY(),
                    zone.getSizeZ(),
                    zone.getPriority(),
                    (int) zone.getMaximumParentDistance(),
                    zone.getRuleOverrides()
            );
        }

        setDirty();
        return record;
    }

    public boolean removeParent(String instanceId) {
        if (instances.remove(instanceId) == null) {
            return false;
        }

        for (DungeonZoneRecord zone : zones.values()) {
            if (instanceId.equals(zone.resolvedInstanceId())) {
                zone.setResolvedInstanceId("");
            }
        }

        setDirty();
        return true;
    }

    public boolean removeZone(String zoneKey) {
        if (zones.remove(zoneKey) == null) {
            return false;
        }

        setDirty();
        return true;
    }

    public void setZoneResolvedInstance(String zoneKey, String instanceId) {
        DungeonZoneRecord zone = zones.get(zoneKey);

        if (zone == null) {
            return;
        }

        String normalized = instanceId == null ? "" : instanceId;
        if (normalized.equals(zone.resolvedInstanceId())) {
            return;
        }

        zone.setResolvedInstanceId(normalized);
        setDirty();
    }

    public Optional<DungeonInstanceRecord> findNearest(
            ResourceKey<Level> dimension,
            BlockPos origin,
            ResourceLocation expectedPreset,
            int linkChannel,
            double maximumDistance
    ) {
        return findNearestInternal(
                dimension,
                origin,
                expectedPreset,
                linkChannel,
                maximumDistance,
                false
        );
    }

    public Optional<DungeonInstanceRecord> findNearestActive(
            ResourceKey<Level> dimension,
            BlockPos origin,
            ResourceLocation expectedPreset,
            int linkChannel,
            double maximumDistance
    ) {
        return findNearestInternal(
                dimension,
                origin,
                expectedPreset,
                linkChannel,
                maximumDistance,
                true
        );
    }

    private Optional<DungeonInstanceRecord> findNearestInternal(
            ResourceKey<Level> dimension,
            BlockPos origin,
            ResourceLocation expectedPreset,
            int linkChannel,
            double maximumDistance,
            boolean activeOnly
    ) {
        String dimensionId = dimension.location().toString();
        double maximumDistanceSquared = maximumDistance * maximumDistance;
        DungeonInstanceRecord closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (DungeonInstanceRecord record : instances.values()) {
            if (activeOnly && !record.rulesAreActive()) {
                continue;
            }

            if (!record.dimensionId().equals(dimensionId)) {
                continue;
            }

            if (!record.presetId().equals(expectedPreset.toString())) {
                continue;
            }

            if (record.linkChannel() != linkChannel) {
                continue;
            }

            double distance = record.parentPos().distSqr(origin);

            if (distance <= maximumDistanceSquared && distance < closestDistance) {
                closest = record;
                closestDistance = distance;
            }
        }

        return Optional.ofNullable(closest);
    }

    public Optional<DungeonInstanceRecord> findNearestAnyPreset(
            ResourceKey<Level> dimension,
            BlockPos origin,
            double maximumDistance
    ) {
        String dimensionId = dimension.location().toString();
        double maximumDistanceSquared = maximumDistance * maximumDistance;
        DungeonInstanceRecord closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (DungeonInstanceRecord record : instances.values()) {
            if (!record.dimensionId().equals(dimensionId)) {
                continue;
            }

            double distance = record.parentPos().distSqr(origin);

            if (distance <= maximumDistanceSquared && distance < closestDistance) {
                closest = record;
                closestDistance = distance;
            }
        }

        return Optional.ofNullable(closest);
    }

    public boolean setState(String instanceId, DungeonInstanceState state) {
        DungeonInstanceRecord record = instances.get(instanceId);

        if (record == null) {
            return false;
        }

        record.setState(state);
        setDirty();
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag instanceList = new ListTag();

        for (DungeonInstanceRecord record : instances.values()) {
            instanceList.add(record.save());
        }

        ListTag zoneList = new ListTag();

        for (DungeonZoneRecord record : zones.values()) {
            zoneList.add(record.save());
        }

        tag.put("Instances", instanceList);
        tag.put("Zones", zoneList);
        return tag;
    }

    private static DungeonRuleSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DungeonRuleSavedData data = new DungeonRuleSavedData();
        ListTag instanceList = tag.getList("Instances", Tag.TAG_COMPOUND);

        for (int index = 0; index < instanceList.size(); index++) {
            DungeonInstanceRecord record = DungeonInstanceRecord.load(
                    instanceList.getCompound(index)
            );

            if (!record.instanceId().isBlank()) {
                data.instances.put(record.instanceId(), record);
            }
        }

        ListTag zoneList = tag.getList("Zones", Tag.TAG_COMPOUND);

        for (int index = 0; index < zoneList.size(); index++) {
            DungeonZoneRecord record = DungeonZoneRecord.load(
                    zoneList.getCompound(index)
            );

            if (!record.zoneKey().isBlank()) {
                data.zones.put(record.zoneKey(), record);
            }
        }

        return data;
    }
}
