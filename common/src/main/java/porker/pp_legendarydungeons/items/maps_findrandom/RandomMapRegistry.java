package porker.pp_legendarydungeons.items.maps_findrandom;

import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.features.wtraders.json.MapGroupJson;
import porker.pp_legendarydungeons.features.wtraders.json.MapMetadataJsonRegistry;
import porker.pp_legendarydungeons.features.wtraders.json.MapTargetJson;
import porker.pp_legendarydungeons.items.maps.MapDescriptionComponents;

import java.util.List;
import java.util.Optional;

/**
 * Runtime accessor for datapack-defined random-map groups and targets.
 */
public final class RandomMapRegistry {
    private RandomMapRegistry() {
    }

    public static Optional<RandomMapGroup> getGroup(String groupId) {
        Optional<ResourceLocation> resolvedGroupId =
                MapMetadataJsonRegistry.resolveGroupId(groupId);

        if (resolvedGroupId.isEmpty()) {
            return Optional.empty();
        }

        Optional<MapGroupJson> group =
                MapMetadataJsonRegistry.getGroup(resolvedGroupId.get());

        if (group.isEmpty()) {
            return Optional.empty();
        }

        List<RandomMapTarget> targets = MapMetadataJsonRegistry
                .getTargetsForGroup(resolvedGroupId.get())
                .stream()
                .map(RandomMapRegistry::toRuntimeTarget)
                .flatMap(Optional::stream)
                .toList();

        return Optional.of(new RandomMapGroup(
                resolvedGroupId.get().toString(),
                group.get().displayName,
                MapDescriptionComponents.fromJson(
                        group.get().descriptionLinesOrEmpty()
                ),
                targets
        ));
    }

    private static Optional<RandomMapTarget> toRuntimeTarget(
            MapTargetJson target
    ) {
        if (target.structureTag == null || target.structureTag.isBlank()) {
            return Optional.empty();
        }

        try {
            ResourceLocation structureTagId =
                    ResourceLocation.parse(target.structureTag);

            return Optional.of(new RandomMapTarget(
                    target.id,
                    target.displayName,
                    structureTagId.toString(),
                    MapDescriptionComponents.fromJson(
                            target.descriptionLinesOrEmpty()
                    )
            ));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
