# Legendary Summons and Dungeon Rules

## Central summon flow

```text
Trigger context
→ LegendarySummonService
→ registered summon definitions
→ condition prepares context
→ LegendarySummonHelper creates a Cobblemon Pokémon
→ aftermath applies locks, tags, sounds, effects, and completion tracking
```

`LegendarySummonHelper` supports:

```text
simple species + level
full Cobblemon property/spec string
```

The normal and secret Rayquaza summons are separate definitions with separate one-time locks.

## Rayquaza completion

After a normal Rayquaza is successfully created, the mod stores a persistent completion watch using the spawned entity UUID.

Sky Pillar completes when the tracked Rayquaza:

```text
is no longer alive
becomes owned/captured
moves beyond the configured completion radius
is confirmed missing while its last known chunk is loaded
```

Unloaded chunks are not treated as a defeat.

## Dungeon-rule system

Dungeon rules are attached to active dungeon instances rather than global gamerules. The system can control:

```text
block breaking
block placement
explosions
interaction restrictions
instance completion and cleanup
```

Block and zone data is registered before worlds load. Server lifecycle callbacks initialize scoreboards and dungeon saved data.

## Multiplayer principles

```text
per-instance records
persistent saved data
UUID-based encounter tracking
server-authoritative changes
no global “last player” or “last dungeon” state
```

## Crystal Caves

The Crystal Caves gameplay assets may remain in the source tree, but the dungeon is not registered or advertised for 1.0.0. Do not create active summon markers or map routes to it in release content.
