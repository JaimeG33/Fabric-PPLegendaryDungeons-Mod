package porker.pp_legendarydungeons.features.custom_mobs;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.decoration.ArmorStand;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;
import porker.pp_legendarydungeons.features.FeatureContext;
import porker.pp_legendarydungeons.features.custom_mobs.mob_registry.CustomMobDefinition;
import porker.pp_legendarydungeons.features.custom_mobs.mob_registry.CustomMobRegistry;

import java.util.Optional;

/**
 * Spawns pre-defined custom dungeon mobs from registry entries.
 *
 * The first implementation intentionally runs summon commands, because the old
 * datapack already has working NBT for equipment, enchantments, attributes, and
 * persistence. Later, specific mobs can be migrated to direct Java entity setup
 * if needed.
 */
public final class CustomMobSpawner {
    private CustomMobSpawner() {
    }

    public static boolean spawnById(FeatureContext context, ArmorStand enemyMarker, String enemyId) {
        Optional<CustomMobDefinition> definition = CustomMobRegistry.get(enemyId);

        if (definition.isEmpty()) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "[Custom Mob] Unknown dungeon enemy id '{}' at {}.",
                    enemyId,
                    enemyMarker.blockPosition()
            );
            return false;
        }

        boolean commandSucceeded = runCommandAtMarker(
                context,
                enemyMarker,
                definition.get().summonCommand()
        );

        if (commandSucceeded) {
            ProfessorPorkersLegendaryDungeons.LOGGER.info(
                    "[Custom Mob] Spawned dungeon enemy '{}' at {}.",
                    definition.get().id(),
                    enemyMarker.blockPosition()
            );
        }

        return commandSucceeded;
    }

    private static boolean runCommandAtMarker(FeatureContext context, ArmorStand marker, String command) {
        try {
            CommandSourceStack source = context.level()
                    .getServer()
                    .createCommandSourceStack()
                    .withLevel(context.level())
                    .withPosition(marker.position())
                    .withPermission(4)
                    .withSuppressedOutput();

            int result = context.level().getServer().getCommands().performPrefixedCommand(source, command);
            return result > 0;
        } catch (Exception exception) {
            ProfessorPorkersLegendaryDungeons.LOGGER.error(
                    "Failed to run custom mob summon command at {}: {}",
                    marker.blockPosition(),
                    command,
                    exception
            );
            return false;
        }
    }
}
