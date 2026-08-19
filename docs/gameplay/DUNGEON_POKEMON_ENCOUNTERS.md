# Dungeon Pokémon Encounters

## Purpose

The dungeon Pokémon system creates fresh Cobblemon entities from structure
markers and applies persistent, per-entity combat behavior without requiring
Fight or Flight.

Use this system for:

- Fixed guards.
- Minibosses.
- Scripted room encounters.
- Pokémon with a home area or combat effects.
- Pokémon that must always exist at a specific marker.

Use Cobblemon structure-conditioned natural spawn JSON for ambient populations
that may appear randomly around a structure.

## Structure markers

A structure needs a broad feature marker:

```text
tag: pp_feature
```

It also needs one armor stand per Pokémon spawn:

```text
tag: pp_dungeon_pokemon
CustomName: <dungeon profile resource ID>
```

Example:

```text
CustomName: pp_legendarydungeons:example/carbink_guard
```

### Summoning a test marker

For a quick in-game test, one armor stand can carry both the broad feature tag
and the dungeon Pokémon marker tag:

```mcfunction
/summon minecraft:armor_stand ~ ~ ~ {Invisible:1b,Marker:1b,NoGravity:1b,Tags:["pp_feature","pp_dungeon_pokemon"],CustomName:'{"text":"pp_legendarydungeons:example/common/ariados"}',CustomNameVisible:0b}
```

Replace only the resource ID inside the `text` value to test another dungeon
profile. For example:

```text
pp_legendarydungeons:example/common/gastly
pp_legendarydungeons:example/common/honedge
pp_legendarydungeons:example/common/mawile
pp_legendarydungeons:example/advanced/ceruledge
```

Run the command from chat as one complete line. Do not add backslashes before
the quotation marks. The marker is discarded after the Pokémon spawns and
registers successfully. If the profile is missing or invalid, the armor stand
remains and the server log reports the error.

The same armor stand may carry both tags for a small test. In larger structures,
one broad `pp_feature` marker can activate multiple nearby Pokémon markers.

The Pokémon marker is discarded only after the spawn and manager registration
succeed. A malformed or missing profile leaves the marker in place and logs an
error.

## Dungeon profile resources

Profiles load from:

```text
data/<namespace>/dungeon_pokemon_profiles/<path>.json
```

Example:

```json
{
  "spawn_profile": "pp_legendarydungeons:dungeon_enemy/standard/carbink_guard",
  "aggression": {
    "enabled": true,
    "target_players": true,
    "exclude_creative": true,
    "exclude_spectators": true,
    "requires_active_dungeon_instance": false,
    "detection_range": 16.0,
    "chase_range": 28.0,
    "home_radius": 12.0,
    "return_speed": 1.0,
    "update_interval_ticks": 10
  },
  "combat_effects": [
    {
      "effect": "minecraft:resistance",
      "amplifier": 0,
      "duration_ticks": 60,
      "refresh_interval_ticks": 20,
      "ambient": false,
      "show_particles": true,
      "show_icon": true
    }
  ],
  "capture": {
    "allowed": true,
    "remove_dungeon_state": true
  },
  "targets": [
    {
      "type": "player",
      "value": ""
    }
  ]
}
```

## Persistence

Dungeon Pokémon:

- Call Minecraft's persistence API when requested by the spawn profile.
- Respect the spawn profile's `counts_towards_spawn_cap` setting.
- Save their profile ID, home position, and dungeon instance ID into their own
  Cobblemon entity NBT.
- Re-register when their chunk or world reloads.

The runtime manager tracks loaded entities by UUID and never scans every
Pokémon in the world.

## Aggression

`detection_range` controls how far the Pokémon searches from its current
position.

`chase_range` is measured from the saved home marker. Targets beyond this
distance are invalid, preventing players from pulling guards across the world.

`home_radius` controls when an idle Pokémon navigates back toward the marker.

The manager refreshes the inherited target plus the registered
`ATTACK_TARGET` and `ANGRY_AT` Brain memories. This bridge is intentionally
isolated so Cobblemon 1.8 compatibility changes remain localized.

## Factions and player relations

Dungeon Pokémon can optionally join a datapack-defined encounter faction:

```json
"faction": "pp_legendarydungeons:kyogre_defenders",
"player_relation": "faction_retaliatory"
```

Faction definitions load from:

```text
data/<namespace>/dungeon_factions/<path>.json
```

For example, the built-in Kyogre defender definition is conceptually:

```json
{
  "hostile_factions": [
    "pp_legendarydungeons:pirate_raiders"
  ]
}
```

The built-in Step 1 faction IDs are:

```text
pp_legendarydungeons:kyogre_defenders
pp_legendarydungeons:pirate_raiders
```

The two definitions list one another as hostile, so managed dungeon Pokémon in
those factions can acquire each other as targets without every Pokémon profile
duplicating explicit scoreboard-team target rules.

Faction hostility and player hostility are separate concepts. This is needed
for the planned Kyogre encounter: drowned, guardians, and defender Pokémon can
eventually share one faction while still having different behavior toward the
player.

Supported Pokémon `player_relation` values are:

```text
legacy
hostile
neutral
faction_retaliatory
```

- `legacy` is the default and preserves the old `target_players` plus explicit
  `player` target-rule behavior.
- `hostile` lets the aggression manager proactively select eligible players.
- `neutral` prevents proactive player selection by this manager.
- `faction_retaliatory` is reserved for Piglin-like shared retaliation. In
  **Step 1 it behaves as neutral**. The planned Step 4 will make it become
  hostile after the player attacks a member of that faction in the same dungeon
  instance.

For any non-`legacy` value, `player_relation` takes precedence over old player
target settings. This makes profile migration explicit while keeping omitted
fields backward-compatible.

### What Steps 1 and 2 can and cannot do

After Step 2:

- managed dungeon Pokémon can recognize other managed dungeon Pokémon by
  faction;
- opposing factions can target each other;
- same-faction managed dungeon Pokémon are excluded from automatic targeting;
- old `entity_tag`, `scoreboard_team`, and `entity_type_tag` targeting still
  works for ordinary mobs;
- a vanilla mob carrying managed dungeon-mob state is a true faction member,
  so Pokémon can recognize it through the same faction service;
- managed Pillagers, Drowned, and Guardians can reciprocally target hostile
  faction Pokémon while keeping their native attack implementation;
- same-faction damage is **not yet globally cancelled**;
- `faction_retaliatory` does **not yet share anger**.

The vanilla-mob manager is now implemented in Step 2. Datapackable vanilla mob
markers/profiles are Step 3, and friendly-fire plus shared retaliation are Step
4. The complete technical roadmap is documented in:

```text
docs/development/DUNGEON_FACTION_AND_RAID_PLAN.md
```

The vanilla-mob behavior, persistent NBT format, and Step 2 manual test commands
are documented in `docs/gameplay/DUNGEON_MOB_ENCOUNTERS.md`.

### Conceptual Kyogre encounter

The finished encounter is intended to feel like the player arrived during an
ongoing assault rather than triggering a normal village Raid. Pirate raiders
will fight both the player and the Kyogre defenders. Kyogre defender Pokémon
will fight raiders but initially tolerate the player. Attacking a defender will
eventually provoke the defender Pokémon for that dungeon instance. Drowned and
guardians may keep their ordinary hostile behavior toward players even though
they belong to the same defender faction.

## Target rules

Supported target types:

```text
player
entity_tag
scoreboard_team
entity_type_tag
```

Examples:

```json
{
  "type": "entity_tag",
  "value": "pp_dungeon_raider"
}
```

```json
{
  "type": "scoreboard_team",
  "value": "pp_illagers"
}
```

```json
{
  "type": "entity_type_tag",
  "value": "pp_legendarydungeons:dungeon_raiders"
}
```

These legacy rules still make the Pokémon target matching entities. Faction
hostility is now an additional non-player target source for managed dungeon
Pokémon. Step 2 extends faction identity to managed vanilla mobs, so an ordinary
Pillager, Drowned, Guardian, or other `Mob` becomes a reciprocal faction target
when it carries dungeon-mob encounter state. `DungeonMobManager` assigns the
vanilla mob's target without replacing its native attack implementation.

## Combat effects

Effects are short-lived and refreshed only while a valid combat target exists.
They naturally expire after disengagement and are explicitly removed when the
capture event can still resolve the entity.

Ordinary Minecraft effects belong to the world entity and are not copied into
the player's party Pokémon.

### Death-triggered effects

Cobblemon owns the Pokémon faint/death lifecycle and may remove a fainted entity
without Minecraft's ordinary `KILLED` removal callback. Effects such as
`minecraft:wind_charged` and `minecraft:oozing` therefore need an explicit,
opt-in compatibility callback:

```json
{
  "effect": "minecraft:wind_charged",
  "amplifier": 1,
  "duration_ticks": 80,
  "refresh_interval_ticks": 20,
  "ambient": true,
  "show_particles": false,
  "show_icon": false,
  "death_callback": "vanilla_killed"
}
```

Supported values are:

```text
none
vanilla_killed
```

`none` is the default. `vanilla_killed` arms the effect after Minecraft accepts
it on the managed dungeon Pokémon. The armed callback is independent of the
remaining potion duration, allowing it to survive a long Cobblemon battle after
the visible world effect expires.

When the Pokémon is defeated, the callback is queued and executed at death tick
59, immediately before Cobblemon 1.7.3 removes the entity at tick 60. The bridge
reconstructs a one-tick `MobEffectInstance` with the configured amplifier and
invokes the registered effect implementation's own `KILLED` removal callback.
It does not recreate Wind Charged, Oozing, or custom effect behavior itself.

The bridge is restricted to managed dungeon Pokémon, does not hardcode species
or effect implementations, and removes any still-active copy after processing to
prevent a duplicate callback during Cobblemon's final killed removal.

A death callback is only armed after the matching effect was successfully active
at least once. Capturing, ownership changes, chunk unloading, or server shutdown
clear runtime callback state without invoking it.

## Capture

`allowed: true` keeps normal Cobblemon capture behavior.

`allowed: false` applies Cobblemon's `uncatchable` property through its shared
property parser.

On successful capture, the runtime record, target state, managed tag, and
configured combat effects are cleaned up. No Fight or Flight API is used.

## Fight or Flight compatibility

The system:

- Does not require Fight or Flight.
- Does not call Fight or Flight classes.
- Does not inject into Cobblemon's Brain construction.
- Uses inherited targeting that Fight or Flight also delegates to.
- Reasserts the dungeon target only at a bounded interval.

Fight or Flight may still change damage, move execution, particles, or forced
battle behavior according to the modpack's global configuration.

## Dungeon-rule integration

When `requires_active_dungeon_instance` is true, the marker's location is linked
to the highest-priority dungeon rule zone at spawn time. Aggression pauses if
that instance is not active.

Leave it false for standalone structures that do not use the dungeon rule
controller.


## Scoreboard team assignment

Use the top-level `scoreboard_team` field to place the spawned Pokémon on a
Minecraft scoreboard team:

```json
"scoreboard_team": "rayquaza_team",
```

The team is created with Minecraft's default team settings if it does not
already exist. Existing team settings such as friendly fire, collision rules,
name-tag visibility, and color are preserved. Team names must be at most 16
characters and cannot contain whitespace or control characters.

The Pokémon is added using its entity scoreboard name, so normal Minecraft team
commands and `scoreboard_team` target rules can recognize it.

## Spawn status conditions

`spawn_statuses` is a weighted pool of Cobblemon persistent battle statuses. An
empty or omitted list applies no forced status. A single entry creates a fixed
status:

```json
"spawn_statuses": [
  {
    "status": "sleep",
    "weight": 1
  }
],
```

A weighted example with a no-status chance is:

```json
"spawn_statuses": [
  {
    "status": "sleep",
    "weight": 6
  },
  {
    "status": "paralysis",
    "weight": 2
  },
  {
    "status": "none",
    "weight": 2
  }
],
```

That example produces 60% Sleep, 20% Paralysis, and 20% no status. Weights are
relative positive integers; they do not need to total 100.

Canonical built-in names are:

```text
burn
frozen
paralysis
poison
poison_badly
sleep
none
```

Common Showdown-style aliases such as `brn`, `frz`, `par`, `psn`, `tox`, and
`slp` are also accepted. A modded persistent status may be referenced with its
full namespaced ID, for example `other_mod:custom_status`, if that status is
registered with Cobblemon before datapack profiles load.

Only one persistent status can exist on a Pokémon, so the pool selects exactly
one entry. The selected status is stored on Cobblemon's Pokémon data and is
present when battle begins. Sleep is visually special in the overworld because
Cobblemon's entity AI and poses explicitly react to the Sleep status. Burn,
Frozen, Paralysis, Poison, and Badly Poisoned generally have no equivalent sleep
idle animation, but they still enter battle as real status conditions.

The complete copyable example is:

```text
data/pp_legendarydungeons/dungeon_pokemon_profiles/example/advanced/team_status_ceruledge.json
```
