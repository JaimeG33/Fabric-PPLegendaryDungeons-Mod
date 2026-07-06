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
 * It creates:
 * PokemonProperties -> Pokemon -> PokemonEntity -> addFreshEntity(...)
 *
 * Everything else in the summon system should eventually route through this helper.
 */
public final class LegendarySummonHelper {
    private LegendarySummonHelper() {
    }

    /**
     * Creates and spawns a Cobblemon Pokémon directly into the world.
     *
     * @param level        server-side level/dimension
     * @param species      Cobblemon species string, such as "rayquaza"
     * @param pokemonLevel level of the Pokémon
     * @param x            spawn x
     * @param y            spawn y
     * @param z            spawn z
     * @param yaw          horizontal rotation
     * @param pitch        vertical rotation
     * @return Optional containing the spawned PokemonEntity if successful
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
            // PokemonProperties stores the desired Cobblemon data before the Pokemon object is created.
            PokemonProperties properties = new PokemonProperties();
            properties.setSpecies(species);
            properties.setLevel(pokemonLevel);

            // Create the actual Cobblemon Pokemon data object.
            Pokemon pokemon = properties.create();

            // Wrap that Pokemon data object inside a Minecraft entity.
            PokemonEntity entity = new PokemonEntity(
                    level,
                    pokemon,
                    CobblemonEntities.POKEMON
            );

            // Move the entity to the intended spawn position before spawning it into the world.
            entity.moveTo(x, y, z, yaw, pitch);

            // Actually add the entity to the server world.
            boolean spawned = level.addFreshEntity(entity);

            if (!spawned) {
                ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                        "Failed to spawn {} at {}, {}, {}",
                        species,
                        x,
                        y,
                        z
                );
                return Optional.empty();
            }

            return Optional.of(entity);
        } catch (Throwable throwable) {
            // Catching Throwable here prevents one bad summon from crashing the whole server.
            // Later, after testing, you could narrow this to Exception if desired.
            ProfessorPorkersLegendaryDungeons.LOGGER.error(
                    "Failed to create/spawn Cobblemon Pokemon '{}'",
                    species,
                    throwable
            );
            return Optional.empty();
        }
    }
}