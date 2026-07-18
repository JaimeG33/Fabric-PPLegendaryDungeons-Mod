package porker.pp_legendarydungeons.pokemon.spawn;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Loader-neutral request passed into the shared Pokémon spawning service.
 */
public record PokemonSpawnRequest(
        ServerLevel level,
        ResourceLocation profileId,
        Vec3 position,
        float yaw,
        float pitch
) {
}
