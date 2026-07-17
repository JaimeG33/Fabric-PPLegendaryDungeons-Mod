package porker.pp_legendarydungeons.items.maps;

import porker.pp_legendarydungeons.features.wtraders.json.MapMetadataJsonRegistry;

import java.util.Optional;

/**
 * Runtime accessor for datapack-defined dedicated-map descriptions.
 */
public final class MapLoreRegistry {
    private MapLoreRegistry() {
    }

    public static Optional<MapLoreEntry> get(String targetId) {
        return MapMetadataJsonRegistry.getTarget(targetId)
                .map(target -> new MapLoreEntry(
                        target.id,
                        MapDescriptionComponents.fromJson(
                                target.descriptionLinesOrEmpty()
                        )
                ));
    }
}
