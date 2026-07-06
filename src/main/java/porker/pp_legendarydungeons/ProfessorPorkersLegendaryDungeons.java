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
import porker.pp_legendarydungeons.summon.EventStarterSummonTicker;

import static net.minecraft.commands.Commands.literal;

public class ProfessorPorkersLegendaryDungeons implements ModInitializer {
    public static final String MOD_ID = "pp_legendarydungeons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        EventStarterSummonTicker.register();

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