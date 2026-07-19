package porker.pp_legendarydungeons.features.dungeon_pokemon;

import com.cobblemon.mod.common.api.pokemon.status.Statuses;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Applies optional spawn-time state that belongs to a dungeon profile rather
 * than the reusable Pokémon generation profile.
 */
public final class DungeonPokemonSpawnOptions {
    private static final String NONE = "none";
    private static final Set<String> BUILT_IN_STATUS_NAMES = Set.of(
            "burn",
            "brn",
            "frozen",
            "freeze",
            "frz",
            "paralysis",
            "paralyzed",
            "par",
            "poison",
            "psn",
            "poison_badly",
            "badly_poisoned",
            "toxic",
            "tox",
            "sleep",
            "asleep",
            "slp"
    );

    private DungeonPokemonSpawnOptions() {
    }

    public static void apply(
            ServerLevel level,
            PokemonEntity pokemon,
            ResourceLocation profileId,
            DungeonPokemonProfileJson profile
    ) {
        applyScoreboardTeam(level, pokemon, profileId, profile.scoreboard_team);
        chooseStatus(profile.spawn_statuses, level.getRandom()).ifPresent(
                configured -> resolvePersistentStatus(configured.status)
                        .ifPresentOrElse(
                                status -> {
                                    pokemon.getPokemon().applyStatus(status);
                                    LegendaryDungeons.LOGGER.info(
                                            "[Dungeon Pokemon] Applied spawn status {} to entity {} from profile {}.",
                                            configured.status,
                                            pokemon.getUUID(),
                                            profileId
                                    );
                                },
                                () -> LegendaryDungeons.LOGGER.warn(
                                        "[Dungeon Pokemon] Could not resolve persistent spawn status {} for entity {} from profile {}.",
                                        configured.status,
                                        pokemon.getUUID(),
                                        profileId
                                )
                        )
        );
    }

    public static boolean isValidTeamName(String value) {
        if (value == null || value.isBlank() || value.length() > 16) {
            return false;
        }

        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);

            if (Character.isWhitespace(current)
                    || Character.isISOControl(current)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidStatusName(String value) {
        String normalized = normalized(value);

        if (normalized.equals(NONE)
                || BUILT_IN_STATUS_NAMES.contains(normalized)) {
            return true;
        }

        try {
            ResourceLocation.parse(value.trim());
            return value.contains(":");
        } catch (Exception exception) {
            return false;
        }
    }

    private static void applyScoreboardTeam(
            ServerLevel level,
            PokemonEntity pokemon,
            ResourceLocation profileId,
            String configuredTeam
    ) {
        String teamName = configuredTeam == null
                ? ""
                : configuredTeam.trim();

        if (teamName.isBlank()) {
            return;
        }

        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);

        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            LegendaryDungeons.LOGGER.info(
                    "[Dungeon Pokemon] Created missing scoreboard team {} for profile {}.",
                    teamName,
                    profileId
            );
        }

        scoreboard.addPlayerToTeam(pokemon.getScoreboardName(), team);
        LegendaryDungeons.LOGGER.info(
                "[Dungeon Pokemon] Added entity {} to scoreboard team {} for profile {}.",
                pokemon.getUUID(),
                teamName,
                profileId
        );
    }

    private static Optional<DungeonPokemonProfileJson.SpawnStatus> chooseStatus(
            List<DungeonPokemonProfileJson.SpawnStatus> configured,
            RandomSource random
    ) {
        if (configured == null || configured.isEmpty()) {
            return Optional.empty();
        }

        int totalWeight = 0;

        for (DungeonPokemonProfileJson.SpawnStatus entry : configured) {
            if (entry != null && entry.weight > 0) {
                totalWeight += entry.weight;
            }
        }

        if (totalWeight <= 0) {
            return Optional.empty();
        }

        int roll = random.nextInt(totalWeight);

        for (DungeonPokemonProfileJson.SpawnStatus entry : configured) {
            if (entry == null || entry.weight <= 0) {
                continue;
            }

            if (roll < entry.weight) {
                return normalized(entry.status).equals(NONE)
                        ? Optional.empty()
                        : Optional.of(entry);
            }

            roll -= entry.weight;
        }

        return Optional.empty();
    }

    private static Optional<PersistentStatus> resolvePersistentStatus(
            String configured
    ) {
        String normalized = normalized(configured);

        return switch (normalized) {
            case "burn", "brn" -> Optional.of(Statuses.BURN);
            case "frozen", "freeze", "frz" -> Optional.of(Statuses.FROZEN);
            case "paralysis", "paralyzed", "par" ->
                    Optional.of(Statuses.PARALYSIS);
            case "poison", "psn" -> Optional.of(Statuses.POISON);
            case "poison_badly", "badly_poisoned", "toxic", "tox" ->
                    Optional.of(Statuses.POISON_BADLY);
            case "sleep", "asleep", "slp" -> Optional.of(Statuses.SLEEP);
            default -> resolveNamespacedStatus(configured);
        };
    }

    private static Optional<PersistentStatus> resolveNamespacedStatus(
            String configured
    ) {
        try {
            Object resolved = Statuses.getStatus(
                    ResourceLocation.parse(configured.trim())
            );

            if (resolved instanceof PersistentStatus persistentStatus) {
                return Optional.of(persistentStatus);
            }
        } catch (Exception ignored) {
            // Validation and the caller's warning cover malformed/unknown IDs.
        }

        return Optional.empty();
    }

    private static String normalized(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
