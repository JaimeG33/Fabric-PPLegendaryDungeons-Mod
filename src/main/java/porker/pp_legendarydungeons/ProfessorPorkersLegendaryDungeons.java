package porker.pp_legendarydungeons;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Species;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import porker.pp_legendarydungeons.summon.trigger.EntityLegendarySummonTicker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import porker.pp_legendarydungeons.setup.ModScoreboards;
import porker.pp_legendarydungeons.secrets.SecretTicker;
import static net.minecraft.commands.Commands.literal;

/**
 * Main mod initializer.
 *
 * Fabric runs this when the mod loads.
 */
public class ProfessorPorkersLegendaryDungeons implements ModInitializer {
    /**
     * Your mod ID.
     *
     * Useful for logging, identifiers, and later registry names.
     */
    public static final String MOD_ID = "pp_legendarydungeons";

    /**
     * Logger for debugging.
     *
     * LegendarySummonHelper uses this to report spawn failures.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Turns on the entity-based legendary summon scanner.
        EntityLegendarySummonTicker.register();

        // Calls the setup file I made to add teams / scoreboards and stuff
        ServerLifecycleEvents.SERVER_STARTED.register(ModScoreboards::setup);

        // Calls the secret based files I made
        SecretTicker.register();

        // Your existing test command.
        // This can stay for now.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("test").executes(context -> {
                Species species = PokemonSpecies.getByIdentifier(ResourceLocation.tryParse("cobblemon:bidoof"));
                context.getSource().sendSystemMessage(
                        Component.literal("Got species: ")
                                .withStyle(Style.EMPTY.withColor(0x03e3fc))
                                .append(species.getTranslatedName())
                );

                return 0;
            }));
        });
    }
}