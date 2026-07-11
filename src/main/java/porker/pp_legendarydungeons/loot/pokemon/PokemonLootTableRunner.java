package porker.pp_legendarydungeons.loot.pokemon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves and executes ordinary Minecraft loot tables for defeated Pokémon.
 *
 * <p>This class does not alter Cobblemon's native DropTable or its selected
 * drops. It runs an additional Minecraft loot table and spawns the resulting
 * ItemStacks at the defeated Pokémon's position.</p>
 *
 * <h2>Adding another Pokémon</h2>
 *
 * <p>Add another entry to {@link #SPECIES_LOOT_TABLES}. The left side is the
 * Cobblemon species ID. The right side is a Minecraft loot table owned by this
 * mod.</p>
 *
 * <pre>
 * ResourceLocation.fromNamespaceAndPath("cobblemon", "klefki"),
 * modLootTable("loot_injections/pokemon/klefki")
 * </pre>
 */
public final class PokemonLootTableRunner {
    /*
     * A scalable species -> injected Minecraft loot-table mapping.
     *
     * This avoids hardcoding separate event handlers for every Pokémon.
     */
    private static final Map<ResourceLocation, ResourceKey<LootTable>> SPECIES_LOOT_TABLES = Map.of(
            ResourceLocation.fromNamespaceAndPath("cobblemon", "klefki"),
            modLootTable("loot_injections/pokemon/klefki")
    );

    private PokemonLootTableRunner() {
    }

    /**
     * Finds the additional Minecraft loot table configured for this Pokémon.
     */
    public static Optional<ResourceKey<LootTable>> findLootTable(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return Optional.empty();
        }

        ResourceLocation speciesId = pokemon.getSpecies().getResourceIdentifier();
        return Optional.ofNullable(SPECIES_LOOT_TABLES.get(speciesId));
    }

    /**
     * Executes an injected Minecraft entity loot table at a Pokémon's position.
     *
     * <p>The generated loot context contains:</p>
     *
     * <ul>
     *     <li>The defeated Pokémon as THIS_ENTITY.</li>
     *     <li>The Pokémon's position as ORIGIN.</li>
     *     <li>The responsible player when one is known.</li>
     *     <li>The responsible player's Luck value.</li>
     * </ul>
     *
     * @return the number of non-empty ItemStacks spawned
     */
    public static int run(
            ServerLevel level,
            PokemonEntity pokemonEntity,
            ServerPlayer player,
            ResourceKey<LootTable> lootTableKey
    ) {
        try {
            LootParams.Builder parameters = new LootParams.Builder(level)
                    .withParameter(
                            LootContextParams.ORIGIN,
                            pokemonEntity.position()
                    )
                    .withParameter(
                            LootContextParams.THIS_ENTITY,
                            pokemonEntity
                    )
                    /*
                     * ENTITY loot contexts require a damage source. The injected
                     * table is an additional reward table, so a synthetic source
                     * is sufficient.
                     */
                    .withParameter(
                            LootContextParams.DAMAGE_SOURCE,
                            pokemonEntity.damageSources().magic()
                    )
                    .withOptionalParameter(
                            LootContextParams.DIRECT_ATTACKING_ENTITY,
                            player
                    )
                    .withOptionalParameter(
                            LootContextParams.ATTACKING_ENTITY,
                            player
                    )
                    .withOptionalParameter(
                            LootContextParams.LAST_DAMAGE_PLAYER,
                            player
                    )
                    .withLuck(player == null ? 0.0F : player.getLuck());

            LootParams lootParams = parameters.create(
                    LootContextParamSets.ENTITY
            );

            LootTable lootTable = level
                    .getServer()
                    .reloadableRegistries()
                    .getLootTable(lootTableKey);

            List<ItemStack> generatedStacks = lootTable.getRandomItems(
                    lootParams
            );

            int spawnedStacks = 0;

            for (ItemStack generatedStack : generatedStacks) {
                if (generatedStack == null || generatedStack.isEmpty()) {
                    continue;
                }

                ItemEntity itemEntity = new ItemEntity(
                        level,
                        pokemonEntity.getX(),
                        pokemonEntity.getY(),
                        pokemonEntity.getZ(),
                        generatedStack.copy()
                );

                itemEntity.setDefaultPickUpDelay();

                if (level.addFreshEntity(itemEntity)) {
                    spawnedStacks++;
                }
            }

            ProfessorPorkersLegendaryDungeons.LOGGER.info(
                    "[Pokemon Loot] Generated {} stack(s) from {} for species {}.",
                    spawnedStacks,
                    lootTableKey.location(),
                    pokemonEntity.getPokemon()
                            .getSpecies()
                            .getResourceIdentifier()
            );

            return spawnedStacks;
        } catch (Throwable throwable) {
            ProfessorPorkersLegendaryDungeons.LOGGER.error(
                    "[Pokemon Loot] Failed to execute loot table {} for species {}.",
                    lootTableKey.location(),
                    pokemonEntity.getPokemon()
                            .getSpecies()
                            .getResourceIdentifier(),
                    throwable
            );

            return 0;
        }
    }

    private static ResourceKey<LootTable> modLootTable(String path) {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(
                        ProfessorPorkersLegendaryDungeons.MOD_ID,
                        path
                )
        );
    }
}