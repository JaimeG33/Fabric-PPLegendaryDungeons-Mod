package porker.pp_legendarydungeons.features.custom_mobs;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;
import porker.pp_legendarydungeons.features.FeatureContext;

import java.util.List;
import java.util.Set;

/**
 * Handles dungeon enemy markers found near a broad pp_feature marker.
 *
 * This is the Java replacement for the old datapack flow:
 * - other/dungeon/mc_enemies.mcfunction
 * - other/dungeon/enemy/find.mcfunction
 * - other/dungeon/enemy/<enemy>.mcfunction
 *
 * Supported marker behavior:
 * - Armor stand tag: scs_dungeon_enemy
 * - Armor stand CustomName: enemy id, such as "bogged_sentry"
 *
 * Structures should either:
 * - have a nearby separate armor stand tagged pp_feature, or
 * - put both pp_feature and scs_dungeon_enemy on the same enemy marker.
 */
public final class CustomMobFeature {
    public static final String DUNGEON_ENEMY_MARKER_TAG = "scs_dungeon_enemy";

    /**
     * Matches the old datapack enemy scan distance.
     */
    private static final double DUNGEON_ENEMY_MARKER_DISTANCE = 80.0D;
    private static final double DUNGEON_ENEMY_MARKER_DISTANCE_SQUARED =
            DUNGEON_ENEMY_MARKER_DISTANCE * DUNGEON_ENEMY_MARKER_DISTANCE;

    private CustomMobFeature() {
    }

    public static void check(FeatureContext context) {
        AABB searchBox = context.featureMarker().getBoundingBox().inflate(DUNGEON_ENEMY_MARKER_DISTANCE);

        List<ArmorStand> enemyMarkers = context.level().getEntitiesOfClass(
                ArmorStand.class,
                searchBox,
                armorStand ->
                        armorStand.isAlive()
                                && isDungeonEnemyMarker(armorStand)
                                && armorStand.distanceToSqr(context.featureMarker()) <= DUNGEON_ENEMY_MARKER_DISTANCE_SQUARED
        );

        for (ArmorStand enemyMarker : enemyMarkers) {
            boolean spawned = spawnForMarker(context, enemyMarker);

            if (spawned) {
                enemyMarker.discard();
            }
        }
    }

    private static boolean isDungeonEnemyMarker(ArmorStand armorStand) {
        Set<String> tags = armorStand.getTags();
        return tags.contains(DUNGEON_ENEMY_MARKER_TAG);
    }

    private static boolean spawnForMarker(FeatureContext context, ArmorStand enemyMarker) {
        String enemyId = enemyIdFromCustomName(enemyMarker);

        if (enemyId.isBlank()) {
            warnMarkerAndPlayer(
                    context,
                    enemyMarker,
                    "Dungeon enemy marker is missing a CustomName enemy id. Example: bogged_sentry"
            );
            return false;
        }

        boolean spawned = CustomMobSpawner.spawnById(context, enemyMarker, enemyId);

        if (!spawned) {
            warnMarkerAndPlayer(
                    context,
                    enemyMarker,
                    "Could not spawn dungeon enemy id {}.",
                    enemyId
            );
        }

        return spawned;
    }

    private static String enemyIdFromCustomName(ArmorStand enemyMarker) {
        Component customName = enemyMarker.getCustomName();

        if (customName == null || customName.getString().isBlank()) {
            return "";
        }

        return customName.getString().trim();
    }

    private static void warnMarkerAndPlayer(
            FeatureContext context,
            ArmorStand marker,
            String message,
            Object... args
    ) {
        ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                "[Custom Mob Marker] {} at {}",
                formatMessage(message, args),
                marker.blockPosition()
        );

        if (context.player() != null) {
            context.player().sendSystemMessage(Component.literal(
                    "[PP Legendary Dungeons] " + formatMessage(message, args)
            ));
        }
    }

    private static String formatMessage(String message, Object... args) {
        String formatted = message;

        for (Object arg : args) {
            formatted = formatted.replaceFirst("\\{}", java.util.regex.Matcher.quoteReplacement(String.valueOf(arg)));
        }

        return formatted;
    }
}
