package porker.pp_legendarydungeons.pokemon.spawn;

import java.util.ArrayList;
import java.util.List;

/**
 * Datapack-backed description of one reusable Pokémon generation profile.
 *
 * The resource path supplies the profile ID:
 * data/<namespace>/pokemon_spawn_profiles/<path>.json
 * becomes <namespace>:<path>.
 */
public final class PokemonSpawnProfileJson {
    public String base_properties = "";
    public Generation generation = new Generation();
    public EntitySettings entity = new EntitySettings();

    public static final class Generation {
        public IntRange level;
        public NatureSettings nature = new NatureSettings();
        public ShinySettings shiny = new ShinySettings();
        public IvSettings ivs = new IvSettings();
    }

    public static final class EntitySettings {
        public boolean persistent = false;
        public boolean counts_towards_spawn_cap = true;
    }

    public static final class IntRange {
        public int min;
        public int max;
    }

    public static final class NatureSettings {
        public String mode = "default";
        public String id = "";
        public List<WeightedId> entries = new ArrayList<>();
    }

    public static final class ShinySettings {
        public String mode = "default";
        public int denominator = 4096;
    }

    public static final class IvSettings {
        public String mode = "default";
        public int min = 0;
        public int max = 31;
        public int minimum_perfect = 0;
        public IvValues values = new IvValues();
        public List<WeightedRange> ranges = new ArrayList<>();
    }

    public static final class IvValues {
        public Integer hp;
        public Integer attack;
        public Integer defence;
        public Integer special_attack;
        public Integer special_defence;
        public Integer speed;
    }

    public static final class WeightedId {
        public String id = "";
        public int weight = 1;
    }

    public static final class WeightedRange {
        public int min;
        public int max;
        public int weight = 1;
    }
}
