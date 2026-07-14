package porker.pp_legendarydungeons.summon;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerLevel;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

import java.util.Optional;

/**
 * LegendarySummonHelper handles the actual Cobblemon spawning.
 *
 * This is the part that replaces /spawnpokemon.
 *
 * It supports two spawn styles:
 *
 * 1. Simple spawn:
 *    species + level
 *
 * 2. Spec-string spawn:
 *    full Cobblemon-style spec string, such as:
 *    rayquaza level=70 shiny=true attack_iv=31 defence_iv=31 ...
 */
public final class LegendarySummonHelper {
    private LegendarySummonHelper() {
    }

    /**
     * Creates and spawns a simple Cobblemon Pokémon directly into the world.
     *
     * Use this for normal summons where species and level are enough.
     */
    public static Optional<PokemonEntity> spawnPokemon(
            ServerLevel level,
            String species,
            int pokemonLevel,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
        try {
            PokemonProperties properties = new PokemonProperties();
            properties.setSpecies(species);
            properties.setLevel(pokemonLevel);

            Pokemon pokemon = properties.create();

            return spawnPokemonEntity(
                    level,
                    pokemon,
                    species,
                    x,
                    y,
                    z,
                    yaw,
                    pitch
            );
        } catch (Throwable throwable) {
            ProfessorPorkersLegendaryDungeons.LOGGER.error(
                    "Failed to create/spawn Cobblemon Pokemon '{}'",
                    species,
                    throwable
            );
            return Optional.empty();
        }
    }

    /**
     * Creates and spawns a Cobblemon Pokémon from a full Cobblemon spec string.
     *
     * Use this for special forms, shiny Pokémon, IVs, held items, features, etc.
     *
     * Example:
     * rayquaza level=70 shiny=true attack_iv=31 defence_iv=31 hp_iv=31
     * special_attack_iv=31 special_defence_iv=31 speed_iv=31
     */
    public static Optional<PokemonEntity> spawnPokemonFromSpec(
            ServerLevel level,
            String pokemonSpec,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
        try {
            PokemonProperties properties = PokemonProperties.Companion.parse(pokemonSpec);

            Pokemon pokemon = properties.create();

            return spawnPokemonEntity(
                    level,
                    pokemon,
                    pokemonSpec,
                    x,
                    y,
                    z,
                    yaw,
                    pitch
            );
        } catch (Throwable throwable) {
            ProfessorPorkersLegendaryDungeons.LOGGER.error(
                    "Failed to create/spawn Cobblemon Pokemon from spec '{}'",
                    pokemonSpec,
                    throwable
            );
            return Optional.empty();
        }
    }

    /**
     * Shared internal entity creation logic.
     */
    private static Optional<PokemonEntity> spawnPokemonEntity(
            ServerLevel level,
            Pokemon pokemon,
            String debugName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
        PokemonEntity entity = new PokemonEntity(
                level,
                pokemon,
                CobblemonEntities.POKEMON
        );

        entity.moveTo(x, y, z, yaw, pitch);

        boolean spawned = level.addFreshEntity(entity);

        if (!spawned) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "Failed to spawn {} at {}, {}, {}",
                    debugName,
                    x,
                    y,
                    z
            );
            return Optional.empty();
        }

        return Optional.of(entity);
    }
}