package porker.pp_legendarydungeons.features.dungeon_pokemon;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.entity.PokemonEntityLoadEvent;
import com.cobblemon.mod.common.api.events.entity.PokemonEntitySaveEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import porker.pp_legendarydungeons.LegendaryDungeons;

import kotlin.Unit;

/**
 * Persists dungeon Pokémon metadata in the Pokémon entity's own NBT and removes
 * runtime state immediately after a successful capture.
 */
public final class DungeonPokemonEventRegistrar {
    private static final String ROOT_KEY =
            "pp_legendarydungeons:DungeonPokemon";
    private static final String PROFILE_KEY = "Profile";
    private static final String HOME_KEY = "Home";
    private static final String INSTANCE_KEY = "Instance";

    private static boolean registered = false;

    private DungeonPokemonEventRegistrar() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        CobblemonEvents.POKEMON_ENTITY_SAVE.subscribe(
                Priority.NORMAL,
                event -> {
                    onSave(event);
                    return Unit.INSTANCE;
                }
        );

        CobblemonEvents.POKEMON_ENTITY_LOAD.subscribe(
                Priority.NORMAL,
                event -> {
                    onLoad(event);
                    return Unit.INSTANCE;
                }
        );

        CobblemonEvents.POKEMON_CAPTURED.subscribe(
                Priority.NORMAL,
                event -> {
                    onCaptured(event);
                    return Unit.INSTANCE;
                }
        );

        LegendaryDungeons.LOGGER.info(
                "[Dungeon Pokemon] Registered Cobblemon save, load, and capture listeners."
        );
    }

    private static void onSave(PokemonEntitySaveEvent event) {
        PokemonEntity entity = event.getPokemonEntity();

        DungeonPokemonManager.getRecord(entity.getUUID()).ifPresent(record -> {
            CompoundTag data = new CompoundTag();
            data.putString(PROFILE_KEY, record.profileId().toString());
            data.putLong(HOME_KEY, record.homePosition().asLong());
            data.putString(INSTANCE_KEY, record.instanceId());
            event.getNbt().put(ROOT_KEY, data);
        });
    }

    private static void onLoad(PokemonEntityLoadEvent event) {
        PokemonEntity entity = event.getPokemonEntity();

        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }

        CompoundTag root = event.getNbt();

        if (!root.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag data = root.getCompound(ROOT_KEY);

        try {
            ResourceLocation profileId =
                    ResourceLocation.parse(data.getString(PROFILE_KEY));
            BlockPos home = BlockPos.of(data.getLong(HOME_KEY));
            String instanceId = data.getString(INSTANCE_KEY);

            DungeonPokemonManager.register(
                    entity,
                    profileId,
                    home,
                    instanceId
            );
        } catch (Exception exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[Dungeon Pokemon] Ignored invalid saved metadata on Pokémon entity {}: {}",
                    entity.getUUID(),
                    exception.getMessage()
            );
        }
    }

    private static void onCaptured(PokemonCapturedEvent event) {
        if (event.getPlayer().getServer() == null) {
            return;
        }

        DungeonPokemonManager.onCaptured(
                event.getPlayer().getServer(),
                event.getPokemon().getUuid()
        );
    }
}
