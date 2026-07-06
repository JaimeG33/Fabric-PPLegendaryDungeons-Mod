package porker.pp_legendarydungeons.summon;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerLevel;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

import java.util.Optional;

public final class LegendarySummonHelper {
    private LegendarySummonHelper() {
    }

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
                        species,
                        x,
                        y,
                        z
                );
                return Optional.empty();
            }

            return Optional.of(entity);
        } catch (Throwable throwable) {
            ProfessorPorkersLegendaryDungeons.LOGGER.error(
                    "Failed to create/spawn Cobblemon Pokemon '{}'",
                    species,
                    throwable
            );
            return Optional.empty();
        }
    }
}