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

- Call Minecraft's persistence API.
- Do not count toward Cobblemon's ordinary wild spawn cap.
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

These rules make the Pokémon target matching entities. They do not
automatically make illagers target the Pokémon. A future raid feature must add a
reciprocal target goal or bounded controller to tagged dungeon raiders.

## Combat effects

Effects are short-lived and refreshed only while a valid combat target exists.
They naturally expire after disengagement and are explicitly removed when the
capture event can still resolve the entity.

Ordinary Minecraft effects belong to the world entity and are not copied into
the player's party Pokémon.

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
