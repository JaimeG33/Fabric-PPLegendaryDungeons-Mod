# Legendary Summon Ticker Integration

## Modified ticker

```text
src/main/java/porker/pp_legendarydungeons/summon/trigger/EntityLegendarySummonTicker.java
```

Once per second, it now performs two separate operations:

1. `DungeonCompletionTracker.tick(server)`
2. The existing nearby-player search for `pp_legendary_summon` armor stands

The completion call occurs before and outside the player/marker loops. Therefore, a watch does not require a player to stay near the summon stand.

The underlying entity/chunk must still be loaded for Minecraft to expose exact live entity state. If the relevant chunk is unloaded, the persistent watch waits and resumes later.

## Modified Rayquaza aftermath

```text
src/main/java/porker/pp_legendarydungeons/summon/aftermath/RayquazaAftermath.java
```

After successful entity setup, it calls:

```java
RayquazaDungeonCompletion.arm(context, spawnedPokemon);
```

This is deliberately in aftermath rather than the generic ticker because it guarantees:

- the summon succeeded,
- the exact entity UUID is available,
- the correct summon origin/context is available,
- an unsuccessful condition or failed spawn cannot arm a false completion watch.

## Why completion is not based on nearby species searches

A query for “any wild Rayquaza within 100 blocks” could be confused by:

- another naturally/mod-spawned Rayquaza,
- an owned Rayquaza sent out by a player,
- multiple dungeon instances,
- temporary chunk unloading.

UUID tracking avoids those ambiguities.

## Future defeated-battle hook

If a stable Cobblemon battle-result event is later selected, the Rayquaza condition can complete immediately from an explicit defeat event. The current death/removal/ownership/distance checks remain a durable fallback.
