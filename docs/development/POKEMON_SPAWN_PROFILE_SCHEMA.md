# Pokémon Spawn Profile Schema

Reusable Pokémon generation profiles are server-data resources loaded from:

```text
data/<namespace>/pokemon_spawn_profiles/<path>.json
```

The file path becomes the resource ID. For example:

```text
data/pp_legendarydungeons/pokemon_spawn_profiles/dungeon_enemy/standard/carbink_guard.json
```

becomes:

```text
pp_legendarydungeons:dungeon_enemy/standard/carbink_guard
```

Folders are organizational only. Legendary, standard enemy, advanced enemy,
non-hostile, and miscellaneous profiles all use the same runtime system.

## Complete example

```json
{
  "base_properties": "carbink",
  "generation": {
    "level": {
      "min": 28,
      "max": 36
    },
    "nature": {
      "mode": "weighted_pool",
      "entries": [
        {
          "id": "cobblemon:bold",
          "weight": 4
        },
        {
          "id": "cobblemon:impish",
          "weight": 4
        }
      ]
    },
    "shiny": {
      "mode": "default"
    },
    "ivs": {
      "mode": "weighted_ranges",
      "minimum_perfect": 0,
      "ranges": [
        {
          "min": 0,
          "max": 15,
          "weight": 1
        },
        {
          "min": 16,
          "max": 25,
          "weight": 3
        },
        {
          "min": 26,
          "max": 30,
          "weight": 5
        },
        {
          "min": 31,
          "max": 31,
          "weight": 1
        }
      ]
    }
  },
  "entity": {
    "persistent": true,
    "counts_towards_spawn_cap": false
  }
}
```

`base_properties` uses Cobblemon's ordinary property-string syntax and may
include species, form, ability, held item, moves, aspects, and other registered
properties. Structured generation values are appended afterward and therefore
override conflicting ordinary properties.

## Level

Omit `level` to preserve the base-property/default result.

```json
"level": {
  "min": 20,
  "max": 30
}
```

Bounds are inclusive. Use the same value for a fixed level.

## Nature

Default Cobblemon behavior:

```json
"nature": {
  "mode": "default"
}
```

Fixed:

```json
"nature": {
  "mode": "fixed",
  "id": "cobblemon:adamant"
}
```

Uniform pool:

```json
"nature": {
  "mode": "pool",
  "entries": [
    {
      "id": "cobblemon:adamant"
    },
    {
      "id": "cobblemon:jolly"
    }
  ]
}
```

Weighted pool:

```json
"nature": {
  "mode": "weighted_pool",
  "entries": [
    {
      "id": "cobblemon:adamant",
      "weight": 5
    },
    {
      "id": "cobblemon:jolly",
      "weight": 2
    }
  ]
}
```

Weights are relative.

## Shiny generation

Use Cobblemon's configured chance and event hooks:

```json
"shiny": {
  "mode": "default"
}
```

Other modes:

```json
{ "mode": "never" }
{ "mode": "always" }
{ "mode": "one_in", "denominator": 512 }
```

`one_in` performs one roll per created Pokémon and explicitly sets the result.

## IV generation

Cobblemon default:

```json
"ivs": {
  "mode": "default",
  "minimum_perfect": 0
}
```

A positive `minimum_perfect` in default mode is passed through as
`min_perfect_ivs`.

Fixed values:

```json
"ivs": {
  "mode": "fixed",
  "minimum_perfect": 0,
  "values": {
    "hp": 31,
    "attack": 20,
    "defence": 31,
    "special_attack": 10,
    "special_defence": 31,
    "speed": 15
  }
}
```

Uniform per-stat range:

```json
"ivs": {
  "mode": "range",
  "min": 20,
  "max": 31,
  "minimum_perfect": 1
}
```

Weighted per-stat ranges:

```json
"ivs": {
  "mode": "weighted_ranges",
  "minimum_perfect": 1,
  "ranges": [
    {
      "min": 0,
      "max": 15,
      "weight": 1
    },
    {
      "min": 16,
      "max": 25,
      "weight": 3
    },
    {
      "min": 26,
      "max": 30,
      "weight": 6
    },
    {
      "min": 31,
      "max": 31,
      "weight": 2
    }
  ]
}
```

Each stat selects a range independently. The system then guarantees the
requested minimum number of perfect IVs.

## Entity settings

```json
"entity": {
  "persistent": true,
  "counts_towards_spawn_cap": false
}
```

Dungeon Pokémon force both of these values regardless of the reusable profile.
Other callers may choose ordinary entity behavior.

## Reload and overrides

Profiles reload through the server resource manager. Use:

```mcfunction
/reload
```

Datapacks may add new files or replace a built-in file at the same resource
location. Existing Pokémon are not regenerated after a reload, but managed
dungeon behavior reads the current dungeon profile on later updates.

Malformed files are logged and ignored independently.
