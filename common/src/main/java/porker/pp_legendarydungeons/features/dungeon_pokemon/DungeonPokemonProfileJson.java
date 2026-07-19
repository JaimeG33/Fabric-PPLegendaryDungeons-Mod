package porker.pp_legendarydungeons.features.dungeon_pokemon;

import java.util.ArrayList;
import java.util.List;

/**
 * Datapack-backed behavior applied to a Pokémon spawned from a dungeon marker.
 */
public final class DungeonPokemonProfileJson {
    public String spawn_profile = "";

    /**
     * Optional Minecraft scoreboard team. Missing teams are created on spawn.
     */
    public String scoreboard_team = "";

    /**
     * Weighted Cobblemon persistent status pool. Empty means no forced status.
     */
    public List<SpawnStatus> spawn_statuses = new ArrayList<>();

    public Aggression aggression = new Aggression();
    public List<CombatEffect> combat_effects = new ArrayList<>();
    public Capture capture = new Capture();
    public Loot loot = new Loot();
    public List<TargetRule> targets = new ArrayList<>();

    public static final class SpawnStatus {
        /**
         * none, a built-in persistent status name, or a namespaced custom status.
         */
        public String status = "none";
        public int weight = 1;
    }

    public static final class Aggression {
        public boolean enabled = true;
        public boolean target_players = true;
        public boolean exclude_creative = true;
        public boolean exclude_spectators = true;
        public boolean requires_active_dungeon_instance = false;
        public double detection_range = 16.0D;
        public double chase_range = 28.0D;
        public double home_radius = 12.0D;
        public double return_speed = 1.0D;
        public int update_interval_ticks = 10;
    }

    public static final class CombatEffect {
        public String effect = "";
        public int amplifier = 0;
        public int duration_ticks = 60;
        public int refresh_interval_ticks = 20;
        public boolean ambient = false;
        public boolean show_particles = true;
        public boolean show_icon = true;

        /**
         * none or vanilla_killed.
         *
         * <p>vanilla_killed asks the dungeon compatibility bridge to invoke this
         * active effect's ordinary Minecraft KILLED removal callback when
         * Cobblemon reaches its final LOOT_DROPPED stage.</p>
         */
        public String death_callback = "none";
    }

    public static final class Capture {
        public boolean allowed = true;
        public boolean remove_dungeon_state = true;
    }

    public static final class Loot {
        /**
         * default, additional, or replace.
         */
        public String mode = "default";
        public String table = "";
    }

    public static final class TargetRule {
        /**
         * player, entity_tag, scoreboard_team, or entity_type_tag.
         */
        public String type = "player";
        public String value = "";
    }
}
