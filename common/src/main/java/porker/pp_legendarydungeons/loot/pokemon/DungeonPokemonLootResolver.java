package porker.pp_legendarydungeons.loot.pokemon;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonManager;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonProfileJson;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonProfileRegistry;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonRecord;

import java.util.Locale;
import java.util.Optional;

/**
 * Resolves the one Minecraft loot table, if any, that should be run for a
 * defeated Pokémon.
 *
 * <p>Dungeon-profile configuration takes precedence over the older
 * species-level supplemental mapping. A blank/default dungeon configuration
 * falls through to the species mapping so existing Klefki behavior remains
 * unchanged.</p>
 */
public final class DungeonPokemonLootResolver {
    private DungeonPokemonLootResolver() {
    }

    public static Optional<Selection> resolve(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return Optional.empty();
        }

        Optional<DungeonPokemonRecord> dungeonRecord =
                DungeonPokemonManager.getRecordByPokemonUuid(
                        pokemon.getUuid()
                );

        if (dungeonRecord.isPresent()) {
            DungeonPokemonRecord record = dungeonRecord.get();
            Optional<DungeonPokemonProfileJson> profile =
                    DungeonPokemonProfileRegistry.get(record.profileId());

            if (profile.isPresent()) {
                Optional<Selection> profileSelection =
                        fromProfile(record, profile.get());

                if (profileSelection.isPresent()) {
                    return profileSelection;
                }
            }
        }

        return PokemonLootTableRunner
                .findLootTable(pokemon)
                .map(table -> new Selection(
                        Mode.ADDITIONAL,
                        table,
                        "species:"
                                + pokemon.getSpecies().getResourceIdentifier()
                ));
    }

    private static Optional<Selection> fromProfile(
            DungeonPokemonRecord record,
            DungeonPokemonProfileJson profile
    ) {
        DungeonPokemonProfileJson.Loot loot = profile.loot;

        if (loot == null) {
            return Optional.empty();
        }

        String tableValue = loot.table == null
                ? ""
                : loot.table.trim();

        if (tableValue.isBlank()
                || tableValue.equalsIgnoreCase("default")) {
            return Optional.empty();
        }

        try {
            ResourceLocation tableId =
                    ResourceLocation.parse(tableValue);
            Mode mode = normalized(loot.mode).equals("replace")
                    ? Mode.REPLACE
                    : Mode.ADDITIONAL;

            return Optional.of(new Selection(
                    mode,
                    ResourceKey.create(Registries.LOOT_TABLE, tableId),
                    "dungeon_profile:" + record.profileId()
            ));
        } catch (Exception ignored) {
            /*
             * Invalid values are rejected during datapack reload validation.
             * Returning empty here keeps runtime behavior safe if a profile is
             * replaced between validation and use.
             */
            return Optional.empty();
        }
    }

    private static String normalized(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    public enum Mode {
        ADDITIONAL,
        REPLACE
    }

    public record Selection(
            Mode mode,
            ResourceKey<LootTable> table,
            String source
    ) {
    }
}
