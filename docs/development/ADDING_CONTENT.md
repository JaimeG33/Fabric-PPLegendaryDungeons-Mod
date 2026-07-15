# Adding Content

## Common-first rule

New gameplay and resources belong in `common` unless a loader-specific API is
unavoidable.

Use:

```text
common/src/main/java/
common/src/main/resources/
```

Do not create duplicate Fabric and NeoForge copies of the same loot table,
function, structure, registry, service, or gameplay rule.

When a loader adapter is required, keep the business logic in common and place
only the minimal hook in:

```text
fabric/src/main/java/
neoforge/src/main/java/
```

## Add a vanilla loot injection

1. Create the nested table under:

   ```text
   common/src/main/resources/data/pp_legendarydungeons/loot_table/loot_injections/
   ```

2. Add the shared table key and target definitions in the common loot
   registration system.
3. Make the injection additive rather than replacing the target’s vanilla loot.
4. Ensure reload registration cannot duplicate the injected pool.
5. Test the nested table directly and through the real target.
6. Test `/reload` on Fabric and NeoForge.

## Add a Pokémon reward

1. Create the table under:

   ```text
   common/src/main/resources/data/pp_legendarydungeons/loot_table/loot_injections/pokemon/
   ```

2. Register the Cobblemon species ID and loot-table key in the common Pokémon
   loot service.
3. Preserve native Cobblemon drops and other addons’ drops.
4. Carry the actual responsible player UUID when the event provides it; do not
   use a global last-player field.
5. Test:
   - wild battle defeat;
   - direct player kill;
   - non-player death;
   - duplicate prevention;
   - multiplayer attribution when relevant;
   - Fabric and NeoForge.

## Add a villager trade

1. Create or reuse a shared trade-pool resource.
2. Keep data parsing and trade selection in common.
3. Register the profession and level through the shared Architectury trade hook.
4. Add trades without removing vanilla offers.
5. Test only with a newly generated or newly leveled villager.
6. Repeat after `/reload` and confirm offers are not duplicated.

## Add a wandering-trader profile

1. Create or reuse a profile under the shared resource tree.
2. Add it to the shared `vanilla_wtrader_spawns.json` configuration.
3. Assign a weight in the correct list or group.
4. Make replacement a persistent one-roll decision for each natural trader.
5. Avoid restart loops and duplicate llama or trader creation.
6. Verify every item, map, and loot-table trade before enabling the profile.

## Add a random map target

1. Register the shared worldgen structure and structure set.
2. Create a shared structure tag.
3. Add the target to the common map registry.
4. Add the weighted entry to the shared map loot table.
5. Store the matching resolved-target identifier.
6. Test:
   - `/locate`;
   - direct `/loot`;
   - cartographer generation;
   - wandering-trader generation;
   - restart;
   - Fabric and NeoForge.

Do not add Java lore data without a valid worldgen destination. Do not add a
loot-table destination without a matching resolver target.

## Add a block, item, or controller

1. Register the public ID in common.
2. Put shared behavior, data, persistence, and validation in common.
3. Put models, blockstates, loot tables, language entries, and recipes in
   `common/src/main/resources`.
4. Register screens and renderers through the shared client initializer where
   possible.
5. Use a loader client adapter only when a shared client API is unavailable.
6. Test placement, save/restart, dedicated-server startup, and both loaders.

## Add networking

1. Define the payload and handler in common.
2. Treat the packet as a request, not an authoritative state update.
3. Validate sender, permission, dimension, distance, target type, target state,
   and numeric bounds on the server.
4. Keep UI-only behavior in client-safe code.
5. Avoid saving unrelated edited fields when the action is Preview, Complete,
   Reactivate, or another non-Save operation.
6. Test an operator, an ordinary player, invalid distance, stale targets,
   restart, and both dedicated servers.

## Add a repeated or scheduled system

1. Prefer one shared scheduler or event listener.
2. Store task context by world and stable IDs rather than direct global entity
   references.
3. Use a suitable interval instead of every tick when possible.
4. Prevent duplicate registration after reload.
5. Remove completed and invalid tasks.
6. Test two players and two dungeon instances at the same time.

## Final review

Before committing new content:

- Shared files are under `common`.
- Loader modules contain no duplicated gameplay rules.
- State is server-authoritative and correctly scoped.
- Values used for loops or area calculations are bounded.
- Registration and cleanup are idempotent.
- Stable IDs remain unchanged.
- Fabric and NeoForge produce the same visible result.
