# Dungeon Faction and Kyogre Raid Plan

## Purpose

This document is the technical source of truth for the planned dungeon-faction
and raid-like encounter system. It is written both for maintainers and for
programming assistance that needs to understand why the system is structured
this way before changing it.

The first intended encounter is the Kyogre structure. It should look as though
the player has arrived while two groups are already fighting:

- `pp_legendarydungeons:pirate_raiders`
  - pillagers or other pirate-styled vanilla mobs;
  - pirate/Team-inspired Pokémon;
  - hostile toward the player;
  - hostile toward Kyogre defenders;
  - later able to advance toward raid objectives inside the structure.
- `pp_legendarydungeons:kyogre_defenders`
  - drowned;
  - guardians;
  - aquatic or temple-themed Pokémon;
  - hostile toward pirate raiders;
  - same-faction entities should not intentionally attack one another;
  - defender Pokémon are intended to be neutral to the player until the player
    attacks a defender, while drowned and guardians may retain their ordinary
    hostile player behavior.

This is intentionally **not** implemented as a vanilla village Raid. The mod
needs mixed Pokémon/vanilla factions, per-dungeon player relations, custom
spawning, and later objective movement. A small encounter controller can supply
those rules without inheriting village, Bad Omen, wave, bell, or raid-center
assumptions.

## Current implementation status

### Step 1 — implemented by this patch

Step 1 establishes faction identity and faction-aware targeting for managed
dungeon Pokémon.

Implemented pieces:

1. New reloadable faction resources under:

   ```text
   data/<namespace>/dungeon_factions/<path>.json
   ```

2. New built-in faction IDs:

   ```text
   pp_legendarydungeons:kyogre_defenders
   pp_legendarydungeons:pirate_raiders
   ```

3. New top-level dungeon Pokémon profile fields:

   ```json
   "faction": "pp_legendarydungeons:kyogre_defenders",
   "player_relation": "faction_retaliatory"
   ```

4. `DungeonFactionService`, which is the shared relationship boundary rather
   than putting faction rules directly into `DungeonPokemonManager`.
5. Faction-vs-faction target discovery for managed dungeon Pokémon.
6. Same-faction managed dungeon Pokémon are excluded from automatic target
   selection.
7. Existing `targets` rules continue to work.
8. Profiles with omitted faction fields continue using the old behavior.
9. `faction_retaliatory` is recognized now but behaves as neutral until Step 4
   provides the instance-scoped provocation state it requires.

Step 1 does **not** yet make pillagers, drowned, or guardians faction members.
It also does not block damage between allies. Those are deliberate later steps.

## Conceptual model

Three concepts must remain separate:

### Spawn identity

The reusable spawn profile answers:

> What Pokémon is this and how is it generated?

Examples include species, level, moves, IVs, persistence, and spawn-cap rules.

### Encounter behavior

The dungeon Pokémon profile answers:

> What role does this particular Pokémon play in this structure?

It controls aggression ranges, effects, capture, loot, faction membership,
player relation, and legacy explicit target selectors.

### Faction relationship

The faction resource answers:

> Which other factions does this faction consider hostile?

For example:

```json
{
  "hostile_factions": [
    "pp_legendarydungeons:pirate_raiders"
  ]
}
```

Player relationships do **not** belong in the faction resource. That is
intentional. A drowned and a Kyogre-defender Pokémon may share one faction but
still react differently to the player.

## Step 1 data flow

```text
pp_dungeon_pokemon marker
        |
        v
DungeonPokemonSpawner
        |
        v
DungeonPokemonProfileJson
        |
        +---- faction --------------------+
        |                                 |
        v                                 v
DungeonPokemonManager             DungeonFactionService
        |                                 |
        |                     DungeonFactionProfileRegistry
        |                                 |
        +----------- target decision <----+
        |
        v
CobblemonAggressionBridge
        |
        v
PokemonEntity target + Brain memories
```

`DungeonFactionService` currently obtains a Pokémon's faction by resolving its
existing `DungeonPokemonRecord` to the loaded dungeon profile. Faction data is
therefore not duplicated into a second persistent entity field. The existing
profile ID is already persisted in Pokémon NBT and is sufficient to recover the
current faction definition after reload.

## Faction resources

Faction files are server-data resources and are datapackable:

```text
data/<namespace>/dungeon_factions/<path>.json
```

The path becomes the resource ID. For example:

```text
data/pp_legendarydungeons/dungeon_factions/kyogre_defenders.json
```

becomes:

```text
pp_legendarydungeons:kyogre_defenders
```

`hostile_factions` is directional. If two factions should attack each other,
each definition should list the other. Self-hostility, malformed IDs, duplicate
IDs inside one definition, and blank entries are rejected during reload.

The validator does not require every referenced faction to exist during the
same listener's validation pass. This keeps datapack replacement and reload
ordering loosely coupled. A missing referenced faction simply cannot produce a
matching target until a valid definition exists.

## Dungeon Pokémon faction fields

### `faction`

Optional namespaced resource ID:

```json
"faction": "pp_legendarydungeons:kyogre_defenders"
```

Blank or omitted means that the Pokémon is outside the faction system. Legacy
targeting continues normally.

### `player_relation`

Supported Step 1 values:

```text
legacy
hostile
neutral
faction_retaliatory
```

Behavior:

- `legacy` — default. Preserves the old `aggression.target_players` and
  `targets: [{"type":"player"}]` behavior.
- `hostile` — player is eligible as a target whenever the normal aggression,
  range, creative/spectator, dungeon-instance, and battle checks allow it.
- `neutral` — this manager does not proactively select players.
- `faction_retaliatory` — neutral in Step 1. Step 4 will make the player
  eligible while that player has an active provocation record for the faction
  in this dungeon instance.

For non-`legacy` values, `player_relation` intentionally takes precedence over
the old player flags. This lets an existing profile be migrated without having
to delete every old player target entry immediately.

## Legacy target compatibility

The following target types remain supported:

```text
player
entity_tag
scoreboard_team
entity_type_tag
```

Faction targeting is additive for non-player entities. A candidate can be a
valid target because it matches a legacy target rule **or** because its faction
is hostile to the source Pokémon's faction.

One intentional safety rule is stronger than legacy matching: two managed
dungeon Pokémon in the same nonblank faction are not automatically selected as
targets even if a broad old target rule would otherwise match them.

Step 1 only resolves faction membership for managed dungeon Pokémon. Therefore
an ordinary pillager can still be targeted through an existing entity tag,
scoreboard team, or entity-type tag, but it will not become a true faction
member until Step 2/3.

## Relationship to scoreboard teams

Minecraft scoreboard teams remain useful compatibility tools, but they are not
the faction source of truth.

Reasons:

- scoreboard team names are limited to 16 characters;
- faction IDs are namespaced and datapack-friendly;
- different members of one faction need different player relations;
- future provocation must be scoped to one dungeon instance, not one global
  scoreboard team;
- future faction membership includes both Cobblemon and vanilla entities.

Existing `scoreboard_team` profile behavior is preserved.

## Six-step implementation plan

### Step 1 — faction-aware dungeon Pokémon

**Status: implemented by this patch.**

Add/change:

- Add reloadable `dungeon_factions` resources and atomic registry replacement.
- Add the two initial Kyogre encounter faction definitions.
- Add optional `faction` to `DungeonPokemonProfileJson`.
- Add `player_relation` with a compatibility-first `legacy` default.
- Add validation for the new fields.
- Add `DungeonFactionService` as a shared relationship layer.
- Make `DungeonPokemonManager` ask the service whether a player is eligible.
- Make the manager search hostile managed Pokémon factions in addition to old
  explicit target rules.
- Prevent same-faction managed Pokémon from selecting each other.
- Keep capture, NBT persistence, combat effects, home/chase behavior, and
  existing target rules unchanged.
- Document that `faction_retaliatory` is only the policy declaration in this
  step; actual shared anger comes later.

Validation goals:

- old profiles behave exactly as before;
- two managed Pokémon in opposing built-in factions can acquire each other;
- two managed Pokémon in the same faction do not acquire each other;
- `hostile`, `neutral`, and `legacy` player relations behave independently of
  faction-vs-faction hostility;
- `/reload` replaces faction definitions without duplicate registration.

### Step 2 — vanilla dungeon mob manager and targeting bridge

**Status: planned.**

Add/change:

- Add `DungeonMobManager` parallel to `DungeonPokemonManager`.
- Add a runtime record for loaded managed vanilla mobs containing at least:
  entity UUID, dimension, encounter profile ID, home position, faction, and
  dungeon instance ID.
- Add persistent NBT for managed vanilla mob state so chunk unload/server
  restart does not lose encounter membership.
- Extend `DungeonFactionService.factionOf(LivingEntity)` to recognize managed
  vanilla mobs as well as managed Pokémon.
- Add a small `VanillaMobAggressionBridge` that sets/clears a vanilla `Mob`
  target while leaving the mob's native attack goal responsible for crossbows,
  melee attacks, drowned attacks, or guardian beams.
- Initially test/support Pillager, Drowned, and Guardian specifically.
- Confirm each vanilla attack goal accepts a Cobblemon `PokemonEntity` target.
- Only add a narrow mixin or replacement goal if a specific vanilla mob rejects
  otherwise-valid Pokémon targets.
- Give the vanilla manager the same bounded detection/chase/home scheduling
  principles as the Pokémon manager.
- Add a future `vanilla` player-relation option for mob profiles so drowned and
  guardians can retain ordinary player hostility while sharing the defender
  faction.

Validation goals:

- pillager can attack defender Pokémon;
- drowned and guardian can attack pirate Pokémon;
- vanilla mobs still use their own combat style;
- no global all-entity scan is introduced;
- faction membership survives unload/restart.

### Step 3 — datapackable vanilla mob profiles and structure markers

**Status: planned.**

Add/change:

- Add a new `pp_dungeon_mob` structure marker handled through `FeatureService`.
- Add `DungeonMobFeature`, `DungeonMobSpawner`, profile JSON, registry, reload
  listener, and validator.
- Separate reusable spawn/equipment configuration from encounter behavior where
  practical, following the existing Pokémon spawn-profile/dungeon-profile split.
- Allow profiles to specify entity type, persistence, equipment/customization
  hooks, faction, player relation, aggression ranges, and future objective data.
- Preserve the old `scs_dungeon_enemy`/`CustomMobRegistry` system for existing
  content instead of breaking it immediately.
- Prefer the new profile system for new pirates, drowned, and guardians.
- Make successful marker resolution one-shot just like `pp_dungeon_pokemon`:
  the marker is discarded only after successful spawn/registration.
- Ensure outside datapacks can add or override mob profiles.

Validation goals:

- one marker creates one managed mob;
- failed profiles leave the marker for diagnosis;
- multiple structures do not share runtime state;
- custom datapacks can add a new encounter mob without Java changes.

### Step 4 — friendly-fire protection and shared faction provocation

**Status: planned.**

Add/change:

- Add a shared damage-event boundary for managed encounter members.
- Block same-faction damage where configured so defender Pokémon, drowned, and
  guardians cannot harm one another and pirates cannot intentionally damage
  pirates.
- Keep faction target filtering and damage filtering separate: target selection
  prevents intentional attacks, while damage filtering protects against stray
  projectiles/AOE/effects.
- Add per-player, per-faction, per-dungeon-instance provocation state.
- When a player attacks a qualifying Kyogre defender, mark that player as having
  provoked `kyogre_defenders` in that specific dungeon instance.
- Make `faction_retaliatory` consult that state and become hostile only while the
  record is active.
- Define configurable anger duration/reset rules. Initial implementation should
  favor a bounded timed value and can later support leave-zone or dungeon-reset
  policies.
- Do not make one Kyogre dungeon anger every Kyogre faction member globally.
- Decide which player-caused indirect damage counts (projectiles, owned Pokémon,
  pets, effects) and document the attribution rules.

Validation goals:

- attacking any qualifying defender activates defender Pokémon retaliation;
- a player who does not provoke the faction remains neutral to those Pokémon;
- anger in Dungeon A does not affect Dungeon B;
- same-faction damage is blocked without blocking hostile-faction damage.

### Step 5 — minimal Kyogre encounter integration test

**Status: planned.**

Add/change:

- Build a deliberately small test scene before filling the real structure.
- Include at minimum:
  - one Pillager;
  - one Drowned;
  - one Guardian;
  - one pirate-raider Pokémon;
  - one Kyogre-defender Pokémon.
- Give each entity explicit faction/profile configuration.
- Verify the full relationship matrix:
  pirates hostile to player and defenders; defender vanilla mobs retain chosen
  player behavior; defender Pokémon are neutral until provoked; same-faction
  members do not attack/damage each other.
- Test direct overworld combat and Cobblemon interactions.
- Test chunk unload/reload, server restart, `/reload`, capture, death, structure
  re-entry, two players, and two simultaneous dungeon instances.
- Record performance with multiple combatants before increasing population.
- Only after this matrix passes should the profiles/markers be distributed
  throughout the production Kyogre structure.

Validation goals:

- every row/column of the intended relationship matrix behaves predictably;
- no faction state leaks across players or instances;
- capture/death removes stale manager state;
- Fabric and NeoForge show the same behavior.

### Step 6 — raid objective movement

**Status: planned after combat relationships are stable.**

Add/change:

- Add encounter objective markers such as entrances, breach points, treasure
  rooms, or other raid destinations.
- Give pirate encounter profiles an optional objective reference or objective
  behavior mode.
- When a raider has no valid combat target, navigate toward the selected raid
  objective instead of simply standing at its spawn/home point.
- When an enemy becomes valid, combat targeting temporarily overrides objective
  movement.
- After disengagement, decide whether the raider returns to an invasion route,
  current objective, or home point according to profile configuration.
- Support multiple staged objectives later so pirates can appear to progress
  from shore/ship to courtyard to entrance to interior.
- Keep objective movement separate from faction hostility so later encounters
  can reuse factions without being raids.
- Do not implement recurring pirate ships, uniforms/accessories, nightly wave
  scheduling, or Team Aqua/Magma/Rocket visual theming as part of this step;
  those are later presentation/spawn systems that can reuse the completed
  encounter foundation.

Validation goals:

- idle pirates visibly advance toward the structure;
- combat interrupts movement cleanly;
- pirates resume the correct objective after combat;
- navigation cannot pull entities unbounded distances from their encounter;
- objective logic remains scoped to the correct dungeon instance.

## Intended Kyogre behavior after Steps 1–5

| Entity | Player | Pirate raiders | Kyogre defenders |
|---|---|---|---|
| Pirate Pillager | hostile | friendly | hostile |
| Pirate Pokémon | hostile | friendly | hostile |
| Drowned defender | vanilla/hostile | hostile | friendly |
| Guardian defender | vanilla/hostile | hostile | friendly |
| Kyogre defender Pokémon | neutral until provoked | hostile | friendly |

Step 1 only implements the Pokémon/faction portion of that matrix. The table is
the target behavior for the completed core encounter work, not a claim that all
rows work immediately after this patch.

## Non-goals for Step 1

Do not add these while troubleshooting this patch:

- custom pirate accessories or models;
- ship reinforcement scheduling;
- actual vanilla Raid objects/waves;
- shared retaliation state;
- friendly-fire damage cancellation;
- vanilla mob faction registration;
- raid objective navigation.

Keeping these out of Step 1 makes it possible to validate the faction boundary
without simultaneously debugging spawning, vanilla AI, damage events, and path
objectives.
