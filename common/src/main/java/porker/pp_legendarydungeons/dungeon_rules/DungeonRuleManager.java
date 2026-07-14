package porker.pp_legendarydungeons.dungeon_rules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleParentBlockEntity;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleZoneBlockEntity;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonInstanceRecord;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonRuleSavedData;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonZoneRecord;
import porker.pp_legendarydungeons.dungeon_rules.preset.DungeonRulePreset;
import porker.pp_legendarydungeons.dungeon_rules.preset.DungeonRulePresetRegistry;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Runtime coordinator for parent controllers and child box zones.
 *
 * Child block entities persist their definitions into SavedData. The runtime
 * chunk index is rebuilt from those records at server start, so a large rule box
 * does not stop working merely because its invisible anchor chunk unloads.
 */
public final class DungeonRuleManager {
    public static final double DEFAULT_PARENT_LINK_DISTANCE = 512.0D;

    private static final Map<ResourceKey<Level>, DungeonRuleSpatialIndex> INDEXES = new HashMap<>();
    private static final Map<String, DungeonRuleZoneBlockEntity> LOADED_ZONE_SOURCES = new HashMap<>();
    private static final Map<String, DungeonRuleZone> ACTIVE_ZONES = new HashMap<>();
    private static final Map<String, String> RESOLVED_INSTANCE_BY_ZONE = new HashMap<>();

    private DungeonRuleManager() {
    }

    public static void bootstrap(MinecraftServer server) {
        clearRuntimeIndexesOnly();

        for (DungeonZoneRecord record : DungeonRuleSavedData.get(server).allZones()) {
            refreshZoneRecord(server, record);
        }
    }

    public static String instanceId(ServerLevel level, BlockPos parentPos) {
        return level.dimension().location()
                + "@"
                + parentPos.getX()
                + ","
                + parentPos.getY()
                + ","
                + parentPos.getZ();
    }

    public static String zoneKey(ServerLevel level, BlockPos zonePos) {
        return level.dimension().location() + "@" + zonePos.asLong();
    }

    public static void registerParent(DungeonRuleParentBlockEntity parent) {
        if (!(parent.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ResourceLocation presetId = parent.getPresetResourceLocation();
        String instanceId = instanceId(level, parent.getBlockPos());

        DungeonRuleSavedData.get(level.getServer()).upsertParent(
                instanceId,
                level.dimension(),
                parent.getBlockPos(),
                presetId,
                parent.getLinkChannel(),
                parent.isEnabled(),
                parent.getRuleOverrides()
        );

        refreshMatchingSavedZones(
                level.getServer(),
                level.dimension(),
                presetId,
                parent.getLinkChannel()
        );
    }

    public static void refreshParent(DungeonRuleParentBlockEntity parent) {
        if (!(parent.getLevel() instanceof ServerLevel level)) {
            return;
        }

        registerParent(parent);
        onInstanceStateChanged(
                level.getServer(),
                instanceId(level, parent.getBlockPos())
        );
    }

    public static void removeParent(ServerLevel level, BlockPos parentPos) {
        String instanceId = instanceId(level, parentPos);
        DungeonRuleSavedData data = DungeonRuleSavedData.get(level.getServer());

        if (data.removeParent(instanceId)) {
            for (DungeonZoneRecord record : data.allZones()) {
                refreshZoneRecord(level.getServer(), record);
            }
        }
    }

    public static void registerZone(DungeonRuleZoneBlockEntity zoneBlockEntity) {
        if (!(zoneBlockEntity.getLevel() instanceof ServerLevel level)) {
            return;
        }

        String key = zoneKey(level, zoneBlockEntity.getBlockPos());
        LOADED_ZONE_SOURCES.put(key, zoneBlockEntity);

        DungeonZoneRecord record = DungeonRuleSavedData
                .get(level.getServer())
                .upsertZone(key, level.dimension(), zoneBlockEntity);

        refreshZoneRecord(level.getServer(), record);
    }

    /**
     * A block entity becoming removed can mean its chunk unloaded. Keep the
     * persistent/runtime rule zone active and only drop the editable live source.
     */
    public static void unregisterZone(DungeonRuleZoneBlockEntity zoneBlockEntity) {
        if (!(zoneBlockEntity.getLevel() instanceof ServerLevel level)) {
            return;
        }

        LOADED_ZONE_SOURCES.remove(zoneKey(level, zoneBlockEntity.getBlockPos()));
    }

    public static void removeZone(ServerLevel level, BlockPos zonePos) {
        String key = zoneKey(level, zonePos);
        removeActiveZone(level.dimension(), key);
        LOADED_ZONE_SOURCES.remove(key);
        RESOLVED_INSTANCE_BY_ZONE.remove(key);
        DungeonRuleSavedData.get(level.getServer()).removeZone(key);
    }

    public static void refreshZone(DungeonRuleZoneBlockEntity zoneBlockEntity) {
        if (!(zoneBlockEntity.getLevel() instanceof ServerLevel level)) {
            return;
        }

        String key = zoneKey(level, zoneBlockEntity.getBlockPos());
        LOADED_ZONE_SOURCES.put(key, zoneBlockEntity);

        DungeonZoneRecord record = DungeonRuleSavedData
                .get(level.getServer())
                .upsertZone(key, level.dimension(), zoneBlockEntity);

        refreshZoneRecord(level.getServer(), record);
    }

    private static void refreshZoneRecord(
            MinecraftServer server,
            DungeonZoneRecord record
    ) {
        ResourceKey<Level> dimension = dimensionKey(record.dimensionId());
        removeActiveZone(dimension, record.zoneKey());
        RESOLVED_INSTANCE_BY_ZONE.remove(record.zoneKey());

        Optional<DungeonInstanceRecord> parent = DungeonRuleSavedData
                .get(server)
                .findNearest(
                        dimension,
                        record.anchorPos(),
                        record.presetId(),
                        record.linkChannel(),
                        record.maximumParentDistance()
                );

        if (parent.isEmpty()) {
            DungeonRuleSavedData
                    .get(server)
                    .setZoneResolvedInstance(record.zoneKey(), "");

            LegendaryDungeons.LOGGER.debug(
                    "[Dungeon Rules] Zone at {} is waiting for a {} parent on channel {}.",
                    record.anchorPos(),
                    record.presetId(),
                    record.linkChannel()
            );
            return;
        }

        DungeonInstanceRecord instance = parent.get();
        DungeonRuleSavedData
                .get(server)
                .setZoneResolvedInstance(record.zoneKey(), instance.instanceId());
        RESOLVED_INSTANCE_BY_ZONE.put(record.zoneKey(), instance.instanceId());

        if (!instance.rulesAreActive()) {
            return;
        }

        DungeonRulePreset preset = DungeonRulePresetRegistry.getOrDefault(
                parsePresetId(instance.presetId())
        );

        EnumSet<DungeonRule> parentRules = instance
                .parentOverrides()
                .resolve(preset.defaultRules());

        EnumSet<DungeonRule> resolvedRules = record
                .ruleOverrides()
                .resolve(parentRules);

        DungeonRuleZone runtimeZone = new DungeonRuleZone(
                record.zoneKey(),
                instance.instanceId(),
                dimension,
                record.anchorPos(),
                record.minPos(),
                record.maxPos(),
                record.priority(),
                resolvedRules
        );

        index(dimension).add(runtimeZone);
        ACTIVE_ZONES.put(record.zoneKey(), runtimeZone);
    }

    public static boolean isRuleEnforced(ServerLevel level, BlockPos pos, DungeonRule rule) {
        List<DungeonRuleZone> candidates = index(level.dimension()).zonesAt(pos);
        int highestPriority = Integer.MIN_VALUE;
        boolean foundAtHighestPriority = false;
        boolean enforced = false;

        for (DungeonRuleZone zone : candidates) {
            if (!zone.contains(pos)) {
                continue;
            }

            if (zone.priority() > highestPriority) {
                highestPriority = zone.priority();
                foundAtHighestPriority = true;
                enforced = zone.enforces(rule);
            } else if (zone.priority() == highestPriority) {
                foundAtHighestPriority = true;
                enforced |= zone.enforces(rule);
            }
        }

        return foundAtHighestPriority && enforced;
    }

    public static Optional<String> findInstanceAt(ServerLevel level, BlockPos pos) {
        List<DungeonRuleZone> candidates = index(level.dimension()).zonesAt(pos);
        DungeonRuleZone selected = null;

        for (DungeonRuleZone zone : candidates) {
            if (!zone.contains(pos)) {
                continue;
            }

            if (selected == null || zone.priority() > selected.priority()) {
                selected = zone;
            }
        }

        return selected == null
                ? Optional.empty()
                : Optional.of(selected.instanceId());
    }

    public static Optional<String> getResolvedInstance(ServerLevel level, BlockPos zonePos) {
        String key = zoneKey(level, zonePos);
        String runtime = RESOLVED_INSTANCE_BY_ZONE.get(key);

        if (runtime != null && !runtime.isBlank()) {
            return Optional.of(runtime);
        }

        return DungeonRuleSavedData
                .get(level.getServer())
                .getZone(key)
                .map(DungeonZoneRecord::resolvedInstanceId)
                .filter(value -> !value.isBlank());
    }

    public static Optional<DungeonRuleZone> getActiveZone(ServerLevel level, BlockPos zonePos) {
        return Optional.ofNullable(ACTIVE_ZONES.get(zoneKey(level, zonePos)));
    }

    public static void onInstanceStateChanged(MinecraftServer server, String instanceId) {
        for (DungeonZoneRecord record : DungeonRuleSavedData.get(server).allZones()) {
            if (instanceId.equals(record.resolvedInstanceId())) {
                refreshZoneRecord(server, record);
            }
        }
    }

    private static void refreshMatchingSavedZones(
            MinecraftServer server,
            ResourceKey<Level> dimension,
            ResourceLocation presetId,
            int linkChannel
    ) {
        for (DungeonZoneRecord record : DungeonRuleSavedData.get(server).allZones()) {
            if (!record.dimensionId().equals(dimension.location().toString())) {
                continue;
            }

            if (!record.presetId().equals(presetId)) {
                continue;
            }

            if (record.linkChannel() != linkChannel) {
                continue;
            }

            refreshZoneRecord(server, record);
        }
    }

    private static ResourceLocation parsePresetId(String presetId) {
        try {
            return ResourceLocation.parse(presetId);
        } catch (Exception ignored) {
            return DungeonRulePresetRegistry.STANDARD_DUNGEON_ID;
        }
    }

    private static ResourceKey<Level> dimensionKey(String dimensionId) {
        ResourceLocation id;

        try {
            id = ResourceLocation.parse(dimensionId);
        } catch (Exception ignored) {
            id = Level.OVERWORLD.location();
        }

        return ResourceKey.create(Registries.DIMENSION, id);
    }

    private static DungeonRuleSpatialIndex index(ResourceKey<Level> dimension) {
        return INDEXES.computeIfAbsent(dimension, ignored -> new DungeonRuleSpatialIndex());
    }

    private static void removeActiveZone(ResourceKey<Level> dimension, String zoneKey) {
        DungeonRuleZone existing = ACTIVE_ZONES.remove(zoneKey);

        if (existing != null) {
            index(dimension).remove(existing);
        }
    }

    private static void clearRuntimeIndexesOnly() {
        for (DungeonRuleSpatialIndex index : INDEXES.values()) {
            index.clear();
        }

        INDEXES.clear();
        ACTIVE_ZONES.clear();
        RESOLVED_INSTANCE_BY_ZONE.clear();
    }

    public static void clear() {
        clearRuntimeIndexesOnly();
        LOADED_ZONE_SOURCES.clear();
        DungeonRuleFeedback.clear();
    }
}
