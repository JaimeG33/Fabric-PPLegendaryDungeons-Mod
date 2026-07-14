package porker.pp_legendarydungeons.features.custom_mobs.mob_registry;

/**
 * A named custom mob spawn definition.
 *
 * id:
 * - The armor stand CustomName used by structures, for example bogged_sentry.
 *
 * summonCommand:
 * - A command without a leading slash.
 * - Runs at the dungeon enemy marker position.
 */
public record CustomMobDefinition(
        String id,
        String summonCommand
) {
}
