package porker.pp_legendarydungeons.setup;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

/**
 * Handles scoreboard and team setup for Professor Porker's Legendary Dungeons.
 *
 * This replaces the old datapack on_load scoreboard/team setup.
 *
 * Old datapack setup:
 *
 * scoreboard objectives add event_triggered dummy
 * scoreboard objectives add spawn_once dummy
 * scoreboard objectives add scs_secrets dummy
 *
 * team add rayquaza_team
 * team add team1
 * team add team2
 *
 * team modify rayquaza_team friendlyFire false
 * team modify team1 friendlyFire false
 * team modify team2 friendlyFire false
 *
 * Updated Java setup:
 *
 * scoreboard objectives add event_triggered dummy
 * scoreboard objectives add spawn_once dummy
 * scoreboard objectives add scs_secrets dummy
 *
 * team add rayquaza_team
 * team add pp_team1
 * team add pp_team2
 *
 * team modify rayquaza_team friendlyFire false
 * team modify pp_team1 friendlyFire false
 * team modify pp_team2 friendlyFire false
 */
public final class ModScoreboards {
    /**
     * Scoreboard objectives used by the mod/datapack systems.
     */
    public static final String EVENT_TRIGGERED = "event_triggered";
    public static final String SPAWN_ONCE = "spawn_once";
    public static final String SCS_SECRETS = "scs_secrets";

    /**
     * Teams used by the mod/datapack systems.
     *
     * pp_team1 and pp_team2 use the pp_ prefix to avoid conflicting with
     * generic team names from other mods, datapacks, or server systems.
     */
    public static final String RAYQUAZA_TEAM = "rayquaza_team";
    public static final String TEAM_1 = "pp_team1";
    public static final String TEAM_2 = "pp_team2";

    private ModScoreboards() {
    }

    /**
     * Main setup method.
     *
     * Call this once when the server starts.
     *
     * This method is safe to run multiple times because it checks whether each
     * objective/team already exists before creating it.
     */
    public static void setup(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        setupObjectives(scoreboard);
        setupTeams(scoreboard);

        ProfessorPorkersLegendaryDungeons.LOGGER.info(
                "Professor Porker's Legendary Dungeons scoreboard/team setup complete."
        );
    }

    /**
     * Creates missing scoreboard objectives.
     */
    private static void setupObjectives(Scoreboard scoreboard) {
        addDummyObjectiveIfMissing(scoreboard, EVENT_TRIGGERED);
        addDummyObjectiveIfMissing(scoreboard, SPAWN_ONCE);
        addDummyObjectiveIfMissing(scoreboard, SCS_SECRETS);
    }

    /**
     * Creates missing teams and applies team settings.
     */
    private static void setupTeams(Scoreboard scoreboard) {
        PlayerTeam rayquazaTeam = addTeamIfMissing(scoreboard, RAYQUAZA_TEAM);
        PlayerTeam team1 = addTeamIfMissing(scoreboard, TEAM_1);
        PlayerTeam team2 = addTeamIfMissing(scoreboard, TEAM_2);

        configureTeam(rayquazaTeam);
        configureTeam(team1);
        configureTeam(team2);
    }

    /**
     * Adds a dummy scoreboard objective if it does not already exist.
     *
     * This is the Java equivalent of:
     *
     * scoreboard objectives add <name> dummy
     */
    private static void addDummyObjectiveIfMissing(Scoreboard scoreboard, String objectiveName) {
        Objective existingObjective = scoreboard.getObjective(objectiveName);

        if (existingObjective != null) {
            return;
        }

        scoreboard.addObjective(
                objectiveName,

                // Same as the "dummy" criteria in commands.
                ObjectiveCriteria.DUMMY,

                // Display name. Keeping it the same as the objective name for now.
                Component.literal(objectiveName),

                // Normal integer-style scoreboard display.
                ObjectiveCriteria.RenderType.INTEGER,

                // displayAutoUpdate. False is fine for normal dummy objectives.
                false,

                // No display slot by default.
                null
        );

        ProfessorPorkersLegendaryDungeons.LOGGER.info(
                "Created scoreboard objective '{}'",
                objectiveName
        );
    }

    /**
     * Adds a team if it does not already exist.
     *
     * This is the Java equivalent of:
     *
     * team add <teamName>
     */
    private static PlayerTeam addTeamIfMissing(Scoreboard scoreboard, String teamName) {
        PlayerTeam existingTeam = scoreboard.getPlayerTeam(teamName);

        if (existingTeam != null) {
            return existingTeam;
        }

        PlayerTeam newTeam = scoreboard.addPlayerTeam(teamName);

        ProfessorPorkersLegendaryDungeons.LOGGER.info(
                "Created team '{}'",
                teamName
        );

        return newTeam;
    }

    /**
     * Applies shared settings to the teams.
     *
     * This is the Java equivalent of:
     *
     * team modify <teamName> friendlyFire false
     */
    private static void configureTeam(PlayerTeam team) {
        team.setAllowFriendlyFire(false);

        /*
         * Optional:
         *
         * You can uncomment this later if you want teammates to see invisible
         * teammates as translucent.
         */
        // team.setSeeFriendlyInvisibles(true);
    }
}