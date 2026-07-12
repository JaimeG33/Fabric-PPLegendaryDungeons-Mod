# Compatibility

## Declared runtime range

```text
Minecraft: 1.21.1
Cobblemon: >=1.6.1 <1.8.0
Java: 21
Fabric Loader: 0.17.2+
Fabric API: 0.116.6+1.21.1+
```

The project compiles against Cobblemon 1.6.1. Compiling against the oldest supported version is the safer direction for a multi-version artifact: code that only exists in 1.7.x cannot accidentally enter the build.

## Cobblemon APIs used directly

The current Java code directly depends on:

```text
CobblemonEntities.POKEMON
PokemonProperties
PokemonProperties.Companion.parse(...)
PokemonProperties.create()
Pokemon
PokemonEntity
PokemonEntity.getPokemon()
PokemonEntity.getOwnerUUID()
CobblemonEvents.BATTLE_FAINTED
CobblemonEvents.LOOT_DROPPED
BattleFaintedEvent
LootDroppedEvent
Priority
```

The reviewed 1.7-era source retains the event fields and entity/property classes used by the project.

## 1.6.1 versus 1.7.3 behavior

Cobblemon 1.7.2 fixed the timing at which a winning player is assigned as the killer of a wild Pokémon. The mod’s battle-faint UUID cache remains useful for 1.6.1 and harmless on 1.7.2/1.7.3.

Cobblemon 1.7.2 also added an extra `pnx` property to `BattleFaintedEvent`. The fields used here—battle, killed Pokémon, and context—remain available, so the addition does not require a code change.

Cobblemon 1.7.3’s published developer notes do not list a removal or replacement of `BATTLE_FAINTED`, `LOOT_DROPPED`, `PokemonEntity`, or `PokemonProperties`.

## Known cross-version caveats

### Deprecated ownership accessor

`RayquazaCompletionCondition` currently calls:

```java
pokemon.getOwnerUUID()
```

This compiles against 1.6.1 and is present in the reviewed later source, but the Minecraft/IDE build reports a deprecated API warning. It is not a 1.0.0 blocker. Replace it only after verifying the exact preferred ownership API on both supported endpoints.

### Multiplayer battle attribution

The battle fallback currently takes the first server player in the battle:

```java
event.getBattle().getPlayers().stream().findFirst()
```

This is correct for normal one-player wild battles. In cooperative, raid, or multi-player battle formats, Luck and `killed_by_player` context may be attributed to the first participant rather than the precise participant responsible for the faint.

### Modded drop cancellation

Another Cobblemon addon can cancel `LOOT_DROPPED`. If it intentionally cancels a trainer or special battle’s native loot, the additional Minecraft table may also be skipped depending on event ordering. The current listener does not cancel or remove other mods’ drops.

## Required confidence level

The source/API sweep found no obvious 1.6.1-only or 1.7.3-only call that should prevent startup on the other supported endpoint. This is not a substitute for launching both exact versions. The final release should be considered verified only after both smoke tests pass.
