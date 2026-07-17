package porker.pp_legendarydungeons.features.wtraders.json;

import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Stores datapack-defined functional-map targets and random-map groups.
 */
public final class MapMetadataJsonRegistry {
    private static Map<ResourceLocation, MapTargetJson> mapTargets = Map.of();
    private static Map<ResourceLocation, MapGroupJson> mapGroups = Map.of();
    private static Map<ResourceLocation, List<MapTargetJson>> targetsByGroup = Map.of();
    private static Map<String, ResourceLocation> targetAliases = Map.of();
    private static Map<String, ResourceLocation> groupAliases = Map.of();

    private MapMetadataJsonRegistry() {
    }

    public static void replaceAll(
            Map<ResourceLocation, MapTargetJson> newMapTargets,
            Map<ResourceLocation, MapGroupJson> newMapGroups
    ) {
        mapTargets = immutableCopy(newMapTargets);
        mapGroups = immutableCopy(newMapGroups);
        targetAliases = buildTargetAliases(mapTargets);
        groupAliases = buildGroupAliases(mapGroups);
        targetsByGroup = buildTargetsByGroup(mapTargets, mapGroups);
    }

    public static Optional<MapTargetJson> getTarget(ResourceLocation id) {
        return Optional.ofNullable(mapTargets.get(id));
    }

    public static Optional<MapTargetJson> getTarget(String idOrAlias) {
        return resolveTargetId(idOrAlias).flatMap(MapMetadataJsonRegistry::getTarget);
    }

    public static Optional<MapGroupJson> getGroup(ResourceLocation id) {
        return Optional.ofNullable(mapGroups.get(id));
    }

    public static Optional<MapGroupJson> getGroup(String idOrAlias) {
        return resolveGroupId(idOrAlias).flatMap(MapMetadataJsonRegistry::getGroup);
    }

    public static Optional<ResourceLocation> resolveTargetId(String idOrAlias) {
        return resolveId(idOrAlias, targetAliases, mapTargets);
    }

    public static Optional<ResourceLocation> resolveGroupId(String idOrAlias) {
        return resolveId(idOrAlias, groupAliases, mapGroups);
    }

    public static List<MapTargetJson> getTargetsForGroup(ResourceLocation groupId) {
        return targetsByGroup.getOrDefault(groupId, List.of());
    }

    public static int mapTargetCount() {
        return mapTargets.size();
    }

    public static int mapGroupCount() {
        return mapGroups.size();
    }

    private static <T> Map<ResourceLocation, T> immutableCopy(
            Map<ResourceLocation, T> input
    ) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(input));
    }

    private static Map<String, ResourceLocation> buildTargetAliases(
            Map<ResourceLocation, MapTargetJson> targets
    ) {
        Map<String, ResourceLocation> aliases = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, MapTargetJson> entry : targets.entrySet()) {
            registerCanonicalAliases(aliases, entry.getKey());
        }

        for (Map.Entry<ResourceLocation, MapTargetJson> entry : targets.entrySet()) {
            for (String alias : entry.getValue().aliasesOrEmpty()) {
                registerAlias(aliases, alias, entry.getKey(), "map target");
            }
        }

        return Collections.unmodifiableMap(aliases);
    }

    private static Map<String, ResourceLocation> buildGroupAliases(
            Map<ResourceLocation, MapGroupJson> groups
    ) {
        Map<String, ResourceLocation> aliases = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, MapGroupJson> entry : groups.entrySet()) {
            registerCanonicalAliases(aliases, entry.getKey());
        }

        for (Map.Entry<ResourceLocation, MapGroupJson> entry : groups.entrySet()) {
            for (String alias : entry.getValue().aliasesOrEmpty()) {
                registerAlias(aliases, alias, entry.getKey(), "map group");
            }
        }

        return Collections.unmodifiableMap(aliases);
    }

    private static void registerCanonicalAliases(
            Map<String, ResourceLocation> aliases,
            ResourceLocation id
    ) {
        aliases.put(id.toString(), id);

        if (LegendaryDungeons.MOD_ID.equals(id.getNamespace())) {
            aliases.putIfAbsent(id.getPath(), id);
        }
    }

    private static void registerAlias(
            Map<String, ResourceLocation> aliases,
            String alias,
            ResourceLocation targetId,
            String typeName
    ) {
        String normalized = alias.trim();
        ResourceLocation previous = aliases.putIfAbsent(normalized, targetId);

        if (previous != null && !previous.equals(targetId)) {
            LegendaryDungeons.LOGGER.warn(
                    "[Map Metadata JSON] Alias {} for {} {} conflicts with {}. Keeping the first definition.",
                    normalized,
                    typeName,
                    targetId,
                    previous
            );
        }
    }

    private static <T> Optional<ResourceLocation> resolveId(
            String idOrAlias,
            Map<String, ResourceLocation> aliases,
            Map<ResourceLocation, T> definitions
    ) {
        if (idOrAlias == null || idOrAlias.isBlank()) {
            return Optional.empty();
        }

        String normalized = idOrAlias.trim();
        ResourceLocation aliased = aliases.get(normalized);

        if (aliased != null) {
            return Optional.of(aliased);
        }

        try {
            ResourceLocation parsed = ResourceLocation.parse(normalized);
            return definitions.containsKey(parsed)
                    ? Optional.of(parsed)
                    : Optional.empty();
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private static Map<ResourceLocation, List<MapTargetJson>> buildTargetsByGroup(
            Map<ResourceLocation, MapTargetJson> targets,
            Map<ResourceLocation, MapGroupJson> groups
    ) {
        Map<ResourceLocation, List<MapTargetJson>> mutable = new LinkedHashMap<>();

        for (ResourceLocation groupId : groups.keySet()) {
            mutable.put(groupId, new ArrayList<>());
        }

        for (MapTargetJson target : targets.values()) {
            Set<ResourceLocation> seenGroups = new HashSet<>();

            for (String rawGroupId : target.groupsOrEmpty()) {
                ResourceLocation groupId = ResourceLocation.parse(rawGroupId);

                if (!seenGroups.add(groupId)) {
                    continue;
                }

                List<MapTargetJson> groupTargets = mutable.get(groupId);

                if (groupTargets == null) {
                    LegendaryDungeons.LOGGER.warn(
                            "[Map Metadata JSON] Map target {} references missing map group {}.",
                            target.id,
                            groupId
                    );
                    continue;
                }

                groupTargets.add(target);
            }
        }

        Map<ResourceLocation, List<MapTargetJson>> frozen = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, List<MapTargetJson>> entry : mutable.entrySet()) {
            frozen.put(
                    entry.getKey(),
                    Collections.unmodifiableList(new ArrayList<>(entry.getValue()))
            );
        }

        return Collections.unmodifiableMap(frozen);
    }
}
