package porker.pp_legendarydungeons.features.dungeon_pokemon;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Minimal server-side identity needed to manage one loaded dungeon Pokémon.
 */
public record DungeonPokemonRecord(
        UUID entityUuid,
        UUID pokemonUuid,
        ResourceKey<Level> dimension,
        ResourceLocation profileId,
        BlockPos homePosition,
        String instanceId
) {
}
