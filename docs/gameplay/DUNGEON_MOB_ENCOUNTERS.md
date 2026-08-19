# Dungeon Vanilla-Mob Encounters

## Purpose

Step 2 of the dungeon-faction roadmap adds a managed vanilla-mob layer beside
the existing managed dungeon-Pokémon layer. The goal is to let ordinary
Minecraft mobs such as Pillagers, Drowned, and Guardians participate in the
same encounter factions as Cobblemon Pokémon without replacing their native
combat implementations.

This is the foundation needed for the Kyogre raid scene:

- pirate Pillagers can recognize Kyogre-defender Pokémon as enemies;
- defender Drowned and Guardians can recognize pirate Pokémon as enemies;
- Pokémon can recognize managed vanilla mobs through the same shared faction
  service;
- same-faction entities are not intentionally selected by the encounter
  managers;
- the vanilla mob still decides *how* to attack after the encounter system
  decides *who* its target should be.

Step 2 does **not** yet add structure markers or datapackable vanilla-mob
profiles. Those arrive in Step 3. Friendly-fire damage cancellation and shared
player provocation remain Step 4.

## Player-facing behavior

A managed vanilla mob has three important encounter concepts:

1. **Faction** — who counts as an ally or enemy.
2. **Player relation** — whether the encounter manager should make the mob
   hostile toward players, neutral toward them, or leave player hostility to
   vanilla Minecraft AI.
3. **Home/aggression bounds** — how far the mob looks for encounter enemies and
   how far it is allowed to chase them away from its saved home point.

For the Kyogre encounter, the intended Step 2 setup is:

| Mob | Faction | Player relation | Intended behavior |
|---|---|---|---|
| Pillager pirate | `pirate_raiders` | `hostile` | Attacks players and Kyogre defenders |
| Drowned defender | `kyogre_defenders` | `vanilla` | Keeps vanilla player hostility and attacks pirates |
| Guardian defender | `kyogre_defenders` | `vanilla` | Keeps vanilla player hostility and attacks pirates |

`vanilla` does not mean the faction manager is disabled. It means the manager
will not proactively add players to its own candidate list, but it will allow a
valid player target chosen by the mob's normal Minecraft AI to remain while the
player stays inside the configured chase area.

## Technical architecture

```text
managed vanilla Mob
        |
        +-- persistent entity NBT
        |       pp_legendarydungeons:DungeonMob
        |
        v
DungeonMobManager
        |
        +---------------------+
        |                     |
        v                     v
DungeonFactionService   VanillaMobAggressionBridge
        |                     |
        v                     v
faction relationship      Mob#setTarget(...)
                              |
                              v
                    native Pillager/Drowned/
                       Guardian attack goals
```

`DungeonMobManager` intentionally parallels `DungeonPokemonManager`:

- it stores runtime records only for loaded managed entities;
- it uses deterministic UUID buckets instead of updating every mob every tick;
- target searches are bounded by `detectionRange`;
- chase validity is bounded around the saved home position;
- idle managed mobs can navigate back toward home;
- inactive dungeon instances can suspend encounter aggression;
- runtime records are cleared at server shutdown and rebuilt from entity NBT
  when the entity loads again.

`DungeonFactionService.factionOf(LivingEntity)` is now the shared identity
boundary for both managed Pokémon and managed vanilla mobs. This means the
existing Pokémon manager does not need a separate special case for Pillagers,
Drowned, or Guardians.

## Persistent NBT

Managed vanilla mobs use this root compound:

```text
pp_legendarydungeons:DungeonMob
```

Conceptual shape:

```nbt
{
  "pp_legendarydungeons:DungeonMob": {
    Profile: "pp_legendarydungeons:manual",
    Faction: "pp_legendarydungeons:pirate_raiders",
    PlayerRelation: "hostile",
    Instance: "",
    Home: {X: 0, Y: 64, Z: 0},
    Aggression: {
      Enabled: 1b,
      ExcludeCreative: 1b,
      ExcludeSpectators: 1b,
      RequiresActiveDungeonInstance: 0b,
      DetectionRange: 32.0d,
      ChaseRange: 48.0d,
      HomeRadius: 32.0d,
      ReturnSpeed: 1.0d,
      UpdateIntervalTicks: 10
    }
  }
}
```

The shared `Mob` mixin writes this compound during normal Minecraft entity save
and reads it during normal entity load. When `Home` is present, that saved value
is preserved. When `Home` is absent (the normal manual `/summon` test case), the
mixin now defers registration until the mob's first real server tick and then
uses the mob's final placed block position as home.

This deferral is important because `/summon` reads custom entity NBT before the
command has finished applying the requested `~ ~ ~` coordinates. Earlier Step 2
builds therefore resolved a missing home as `(0, 0, 0)` and caused every managed
mob to reject nearby targets and navigate toward world origin. Existing test
mobs that already saved `Home:{X:0,Y:0,Z:0}` should be removed and resummoned
after applying the Step 2 hotfix.

`Profile` is already reserved in Step 2 even though Step 3 is where actual
`dungeon_mob_profiles` become datapack resources. Manual Step 2 registrations
use `pp_legendarydungeons:manual` when no profile is supplied. Keeping the field
now avoids changing the persistent format when Step 3 arrives.

## Player relation values for managed vanilla mobs

Step 2 accepts:

```text
hostile
neutral
faction_retaliatory
vanilla
```

Behavior:

- `hostile` — the manager proactively searches for eligible Survival players.
- `neutral` — the manager does not accept players as encounter targets.
- `faction_retaliatory` — behaves like neutral until Step 4 adds shared
  instance-scoped provocation.
- `vanilla` — the manager does not proactively search for players, but a player
  selected by the mob's native AI is allowed to remain the target while valid.

Pokémon continue using their existing `legacy`, `hostile`, `neutral`, and
`faction_retaliatory` profile values. `vanilla` is specifically useful for
ordinary Minecraft mobs whose built-in player aggression should remain intact.

### Factions are not scoreboard teams

`pp_legendarydungeons:pirate_raiders` and
`pp_legendarydungeons:kyogre_defenders` are datapack faction IDs handled by
`DungeonFactionService`. They are intentionally **not** created by `/team` and
will not appear in `/team list`. Existing Minecraft scoreboard teams remain a
separate compatibility/content feature and are not required for this encounter.

## Current testing method before Step 3

Step 3 will provide `pp_dungeon_mob` structure markers and datapackable mob
profiles. Until then, Step 2 can be tested directly with summon NBT.

### Pirate Pillager

```mcfunction
/summon minecraft:pillager ~ ~ ~ {PersistenceRequired:1b,"pp_legendarydungeons:DungeonMob":{Faction:"pp_legendarydungeons:pirate_raiders",PlayerRelation:"hostile",Aggression:{DetectionRange:32.0d,ChaseRange:48.0d,HomeRadius:32.0d,ReturnSpeed:1.0d,UpdateIntervalTicks:10}}}
```

### Kyogre-defender Drowned

```mcfunction
/summon minecraft:drowned ~ ~ ~ {PersistenceRequired:1b,"pp_legendarydungeons:DungeonMob":{Faction:"pp_legendarydungeons:kyogre_defenders",PlayerRelation:"vanilla",Aggression:{DetectionRange:32.0d,ChaseRange:48.0d,HomeRadius:32.0d,ReturnSpeed:1.0d,UpdateIntervalTicks:10}}}
```

### Kyogre-defender Guardian

```mcfunction
/summon minecraft:guardian ~ ~ ~ {PersistenceRequired:1b,"pp_legendarydungeons:DungeonMob":{Faction:"pp_legendarydungeons:kyogre_defenders",PlayerRelation:"vanilla",Aggression:{DetectionRange:32.0d,ChaseRange:48.0d,HomeRadius:32.0d,ReturnSpeed:1.0d,UpdateIntervalTicks:10}}}
```

After summoning a mob, wait at least one tick and verify its resolved home:

```mcfunction
/data get entity @e[tag=pp_dungeon_mob,sort=nearest,limit=1] "pp_legendarydungeons:DungeonMob".Home
```

The coordinates should match the mob's actual summon area, not `(0, 0, 0)`
unless it was deliberately summoned there.

Step 2 also includes two dedicated Pokémon faction-test profiles:

```text
pp_legendarydungeons:example/faction_test/pirate_ariados
pp_legendarydungeons:example/faction_test/defender_gastly
```

Spawn either through the normal `pp_dungeon_pokemon` marker system. For example:

```mcfunction
/summon minecraft:armor_stand ~ ~ ~ {Invisible:1b,Marker:1b,NoGravity:1b,Tags:["pp_feature","pp_dungeon_pokemon"],CustomName:'{"text":"pp_legendarydungeons:example/faction_test/pirate_ariados"}',CustomNameVisible:0b}
```

```mcfunction
/summon minecraft:armor_stand ~ ~ ~ {Invisible:1b,Marker:1b,NoGravity:1b,Tags:["pp_feature","pp_dungeon_pokemon"],CustomName:'{"text":"pp_legendarydungeons:example/faction_test/defender_gastly"}',CustomNameVisible:0b}
```

The manager retains an encounter-assigned target between its bounded update
ticks. Native AI may still replace it with another valid target, but a vanilla
target selector cannot immediately erase the retained target, and a managed mob
cannot intentionally select a same-faction managed entity.

Step 2 is considered behaviorally validated only after Pillager crossbow logic,
Drowned combat, and Guardian beam logic are each observed attacking a valid
hostile Pokémon target. If one attack implementation still cannot act on a
retained `PokemonEntity` target, document that exact mob and add only the
narrowest compatibility bridge required for that mob.

## Persistence tests

For each of the three initial mob types:

1. summon the managed mob;
2. confirm the `pp_dungeon_mob` entity tag exists;
3. unload and reload the chunk;
4. save and restart the server;
5. confirm faction targeting still works;
6. use `/data get entity <target>` if needed to inspect the saved
   `pp_legendarydungeons:DungeonMob` compound.

The manager does not force chunks to remain loaded. The runtime UUID record may
be discarded while an entity is unloaded; normal entity NBT restores the record
when the mob loads again.

## What remains for Step 3

Step 2 deliberately does not make content authors hand-write the NBT above in
production structures. Step 3 will add:

- `pp_dungeon_mob` marker handling;
- datapackable dungeon mob profiles;
- validation and reload registries;
- controlled mob spawning/registration;
- entity type and equipment/customization data;
- one-shot marker consumption after successful spawn.

The manual NBT format is therefore primarily a development/testing bridge and a
stable persistence format, not the intended final content-authoring workflow.
