package porker.pp_legendarydungeons.features.dungeon_factions;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import porker.pp_legendarydungeons.features.dungeon_mobs.DungeonMobManager;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonManager;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonProfileJson;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonProfileRegistry;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Shared encounter-faction relationship boundary.
 *
 * <p>Faction identity is shared by managed dungeon Pokémon and managed vanilla
 * mobs. Relationship rules stay here so neither runtime manager needs to know
 * how the other entity family stores its encounter state.</p>
 */
public final class DungeonFactionService {
    public static final String PLAYER_RELATION_LEGACY = "legacy";
    public static final String PLAYER_RELATION_HOSTILE = "hostile";
    public static final String PLAYER_RELATION_NEUTRAL = "neutral";
    public static final String PLAYER_RELATION_FACTION_RETALIATORY =
            "faction_retaliatory";
    public static final String PLAYER_RELATION_VANILLA = "vanilla";

    private DungeonFactionService() {
    }

    /**
     * Returns the configured faction for an entity currently understood by the
     * encounter system. Step 2 recognizes both managed dungeon Pokémon and
     * managed vanilla mobs.
     */
    public static Optional<ResourceLocation> factionOf(LivingEntity entity) {
        if (entity == null) {
            return Optional.empty();
        }

        if (entity instanceof PokemonEntity pokemon) {
            return DungeonPokemonManager
                    .getRecord(pokemon.getUUID())
                    .flatMap(record -> DungeonPokemonProfileRegistry.get(record.profileId()))
                    .flatMap(DungeonFactionService::configuredFaction);
        }

        return DungeonMobManager
                .getRecord(entity.getUUID())
                .map(record -> record.data().factionId());
    }

    public static boolean areAllies(LivingEntity first, LivingEntity second) {
        if (first == null || second == null) {
            return false;
        }

        Optional<ResourceLocation> firstFaction = factionOf(first);
        Optional<ResourceLocation> secondFaction = factionOf(second);

        return firstFaction.isPresent()
                && secondFaction.isPresent()
                && firstFaction.get().equals(secondFaction.get());
    }

    /**
     * Checks the source profile's faction definition against the target entity's
     * faction. Hostility is directional: each faction definition owns its own
     * hostile_factions list.
     */
    public static boolean isHostileTo(
            DungeonPokemonProfileJson sourceProfile,
            LivingEntity target
    ) {
        Optional<ResourceLocation> sourceFaction = configuredFaction(sourceProfile);
        Optional<ResourceLocation> targetFaction = factionOf(target);

        if (sourceFaction.isEmpty() || targetFaction.isEmpty()) {
            return false;
        }

        return isHostileTo(sourceFaction.get(), target);
    }

    /** Shared hostility lookup used by both the Pokémon and vanilla-mob managers. */
    public static boolean isHostileTo(
            ResourceLocation sourceFaction,
            LivingEntity target
    ) {
        if (sourceFaction == null || target == null) {
            return false;
        }

        Optional<ResourceLocation> targetFaction = factionOf(target);
        if (targetFaction.isEmpty()) {
            return false;
        }

        return DungeonFactionProfileRegistry
                .get(sourceFaction)
                .map(definition -> containsFaction(
                        definition.hostile_factions,
                        targetFaction.get()
                ))
                .orElse(false);
    }

    public static boolean hasFactionEnemies(DungeonPokemonProfileJson sourceProfile) {
        Optional<ResourceLocation> sourceFaction = configuredFaction(sourceProfile);

        if (sourceFaction.isEmpty()) {
            return false;
        }

        return hasFactionEnemies(sourceFaction.get());
    }

    public static boolean hasFactionEnemies(ResourceLocation sourceFaction) {
        if (sourceFaction == null) {
            return false;
        }

        return DungeonFactionProfileRegistry
                .get(sourceFaction)
                .map(definition ->
                        definition.hostile_factions != null
                                && !definition.hostile_factions.isEmpty())
                .orElse(false);
    }

    /**
     * Resolves the new player_relation field while preserving legacy profile
     * behavior by default.
     *
     * <p>faction_retaliatory intentionally returns false in Step 1. Step 4 will
     * add instance-scoped provocation state and turn it hostile only while the
     * player's faction anger record is active.</p>
     */
    public static boolean shouldTargetPlayer(
            DungeonPokemonProfileJson profile,
            DungeonPokemonProfileJson.Aggression aggression
    ) {
        String relation = normalized(profile.player_relation);

        return switch (relation) {
            case PLAYER_RELATION_HOSTILE -> true;
            case PLAYER_RELATION_NEUTRAL,
                 PLAYER_RELATION_FACTION_RETALIATORY -> false;
            case PLAYER_RELATION_LEGACY, "" ->
                    aggression.target_players || hasLegacyPlayerRule(profile);
            default -> false;
        };
    }

    public static Optional<ResourceLocation> configuredFaction(
            DungeonPokemonProfileJson profile
    ) {
        if (profile == null || profile.faction == null || profile.faction.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(ResourceLocation.parse(profile.faction.trim()));
        } catch (Exception ignored) {
            // DungeonPokemonProfileValidator reports malformed configured IDs.
            return Optional.empty();
        }
    }

    private static boolean containsFaction(
            List<String> configured,
            ResourceLocation targetFaction
    ) {
        if (configured == null || configured.isEmpty()) {
            return false;
        }

        for (String value : configured) {
            if (value == null || value.isBlank()) {
                continue;
            }

            try {
                if (ResourceLocation.parse(value.trim()).equals(targetFaction)) {
                    return true;
                }
            } catch (Exception ignored) {
                // DungeonFactionProfileValidator reports malformed configured IDs.
            }
        }

        return false;
    }

    private static boolean hasLegacyPlayerRule(DungeonPokemonProfileJson profile) {
        if (profile.targets == null) {
            return false;
        }

        return profile.targets.stream()
                .filter(java.util.Objects::nonNull)
                .map(rule -> normalized(rule.type))
                .anyMatch("player"::equals);
    }

    private static String normalized(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
