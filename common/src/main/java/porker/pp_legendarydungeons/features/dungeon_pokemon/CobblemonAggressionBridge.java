package porker.pp_legendarydungeons.features.dungeon_pokemon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * Small Cobblemon-facing compatibility boundary for aggression state.
 *
 * Fight or Flight also calls the inherited target APIs, so this class does not
 * reference that optional mod or inject into Cobblemon's Brain construction.
 * If Cobblemon 1.8 changes the required memories, this is the primary class to
 * adapt.
 */
public final class CobblemonAggressionBridge {
    private CobblemonAggressionBridge() {
    }

    public static void setTarget(
            PokemonEntity pokemon,
            LivingEntity target
    ) {
        pokemon.setTarget(target);

        Brain<?> brain = pokemon.getBrain();

        if (brain.checkMemory(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.REGISTERED
        )) {
            brain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
        }

        if (brain.checkMemory(
                MemoryModuleType.ANGRY_AT,
                MemoryStatus.REGISTERED
        )) {
            brain.setMemory(MemoryModuleType.ANGRY_AT, target.getUUID());
        }
    }

    public static void clearTarget(PokemonEntity pokemon) {
        pokemon.setTarget(null);

        Brain<?> brain = pokemon.getBrain();

        if (brain.checkMemory(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.REGISTERED
        )) {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }

        if (brain.checkMemory(
                MemoryModuleType.ANGRY_AT,
                MemoryStatus.REGISTERED
        )) {
            brain.eraseMemory(MemoryModuleType.ANGRY_AT);
        }
    }
}
