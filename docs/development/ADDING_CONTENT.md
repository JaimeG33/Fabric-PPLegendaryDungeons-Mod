# Adding Content

## Add a vanilla loot injection

1. Create a table under:

   ```text
   data/pp_legendarydungeons/loot_table/loot_injections/
   ```

2. Add a `ResourceKey<LootTable>` in `LootTableInjectionRegistrar`.
3. Add a target `Set<ResourceKey<LootTable>>`.
4. Add an `InjectionGroup`.
5. Test the nested table directly and through the real target.

## Add a Pokémon reward

1. Create:

   ```text
   loot_injections/pokemon/species_name.json
   ```

2. Add the Cobblemon species ID and table key to `PokemonLootTableRunner`.
3. Test:
   - wild battle defeat;
   - direct player kill;
   - non-player death;
   - multiplayer attribution when relevant.

## Add a villager trade

1. Create a trade pool with `villager_only`.
2. Ensure it contains one direct item or loot-table entry.
3. Add a direct pool listing to `VillagerTradeInjectionRegistrar`.
4. Register it for the profession and level through `TradeOfferHelper`.
5. Test only with a newly generated or newly leveled villager.

## Add a natural custom wandering-trader profile

1. Create or reuse a profile.
2. Add the profile to:

   ```text
   vanilla_wtrader_spawns.json
   ```

3. Assign a weight in the correct profile list/group.
4. Verify every generated map and loot-table trade succeeds before allowing the profile in release.

## Add a random map target

1. Register the worldgen structure and structure set.
2. Create a structure tag.
3. Add a `RandomMapTarget` to `RandomMapRegistry`.
4. Add a weighted entry to the appropriate map loot table.
5. Store the matching `pp_random_map_resolved_target`.
6. Test `/locate`, direct `/loot`, cartographer generation, and natural WTrader generation.

Do not add a target to Java lore data without also providing a valid worldgen destination, and do not add a loot-table destination without a matching resolver target.
