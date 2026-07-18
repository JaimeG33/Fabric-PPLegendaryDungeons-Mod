package porker.pp_legendarydungeons.pokemon.spawn;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.resources.ResourceLocation;

/**
 * Successful result from a profile-backed Pokémon spawn.
 */
public record PokemonSpawnResult(
        ResourceLocation profileId,
        String resolvedProperties,
        PokemonEntity entity
) {
}
