package porker.pp_legendarydungeons.features.dungeon_pokemon;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.features.FeatureContext;

import java.util.List;

/**
 * Marker-driven deterministic Pokémon encounters for structures.
 *
 * Marker format:
 * - tag: pp_dungeon_pokemon
 * - CustomName: dungeon profile resource ID
 *
 * A nearby broad pp_feature marker activates the scan. The marker is discarded
 * only after the Pokémon spawns and registers successfully.
 */
public final class DungeonPokemonFeature {
    public static final String DUNGEON_POKEMON_MARKER_TAG =
            "pp_dungeon_pokemon";

    private static final double MARKER_DISTANCE = 80.0D;
    private static final double MARKER_DISTANCE_SQUARED =
            MARKER_DISTANCE * MARKER_DISTANCE;

    private DungeonPokemonFeature() {
    }

    public static void check(FeatureContext context) {
        AABB searchBox = context
                .featureMarker()
                .getBoundingBox()
                .inflate(MARKER_DISTANCE);

        List<ArmorStand> markers = context.level().getEntitiesOfClass(
                ArmorStand.class,
                searchBox,
                marker ->
                        marker.isAlive()
                                && marker.getTags().contains(DUNGEON_POKEMON_MARKER_TAG)
                                && marker.distanceToSqr(context.featureMarker())
                                <= MARKER_DISTANCE_SQUARED
        );

        for (ArmorStand marker : markers) {
            ResourceLocation profileId = profileId(marker);

            if (profileId == null) {
                warnMarker(
                        context,
                        marker,
                        "Marker is missing a valid CustomName profile ID."
                );
                continue;
            }

            if (DungeonPokemonSpawner.spawn(context, marker, profileId)) {
                marker.discard();
            } else {
                warnMarker(
                        context,
                        marker,
                        "Could not spawn dungeon Pokémon profile " + profileId + "."
                );
            }
        }
    }

    private static ResourceLocation profileId(ArmorStand marker) {
        Component name = marker.getCustomName();

        if (name == null || name.getString().isBlank()) {
            return null;
        }

        try {
            return ResourceLocation.parse(name.getString().trim());
        } catch (Exception exception) {
            return null;
        }
    }

    private static void warnMarker(
            FeatureContext context,
            ArmorStand marker,
            String message
    ) {
        LegendaryDungeons.LOGGER.warn(
                "[Dungeon Pokemon Marker] {} at {}",
                message,
                marker.blockPosition()
        );

        if (context.player() != null) {
            context.player().sendSystemMessage(Component.literal(
                    "[PP Legendary Dungeons] " + message
            ));
        }
    }
}
