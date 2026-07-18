# Dungeon Pokémon Loot Modes and Advanced Examples

Dungeon Pokémon profiles can now choose whether a Minecraft loot table is
supplemental or replaces the Pokémon's native Cobblemon drops.

## Loot configuration

Add this object to a file under
`data/<namespace>/dungeon_pokemon_profiles/<path>.json`:

```json
"loot": {
  "mode": "additional",
  "table": "example:loot_injections/dungeon/ceruledge"
}
```

Supported modes:

- `default`: preserve the existing behavior. Cobblemon native drops remain, and
  any existing species-level supplemental mapping (currently Klefki) can still
  run.
- `additional`: preserve Cobblemon native drops and run the configured
  Minecraft loot table once.
- `replace`: clear the native drops selected by Cobblemon and run the
  configured Minecraft loot table once.

Omitting `loot`, leaving `table` blank, or setting `table` to `default` all use
default behavior. An explicit resource ID requires `mode` to be `additional` or
`replace`.

The configured table is used for both ordinary entity death and defeat through
a Cobblemon battle. Battle defeat context is cached, but the custom table is
executed only from the final loot-drop event, preventing duplicate rewards.

## Advanced example profile IDs

The included examples were reconstructed from the supplied structure NBT:

| Pokémon | Dungeon profile ID |
| --- | --- |
| Ceruledge | `pp_legendarydungeons:example/advanced/ceruledge` |
| Flutter Mane | `pp_legendarydungeons:example/advanced/flutter_mane` |
| Mismagius | `pp_legendarydungeons:example/advanced/mismagius` |
| Naganadel | `pp_legendarydungeons:example/advanced/naganadel` |

The three supplied Ceruledge structures used the same Pokémon data, so they
share one reusable profile.

Each spawn profile includes:

- Level 35–40.
- One-in-1024 shiny odds.
- At least two perfect IVs.
- Persistent entities that count toward the spawn cap.
- The NBT species, ability, Tera type, and four moves.
- Weighted aggressive natures: Adamant has weight 9, while Brave, Rash, and
  Naughty each have weight 1. This makes Adamant 75% and the other aggressive
  natures 25% combined.

Each dungeon profile:

- Keeps player targeting/aggression enabled.
- Preserves the potion effects found in the structure NBT.
- Refreshes those effects only while the Pokémon has a valid target.
- Hides both effect particles and HUD icons.
- Uses native/default loot until a dungeon author selects another loot mode.
- Leaves physical attack execution to Cobblemon or an installed AI addon.

## Marker examples

Ceruledge:

```mcfunction
/summon minecraft:armor_stand ~ ~ ~ {Invisible:1b,Marker:1b,NoGravity:1b,Tags:["pp_feature","pp_dungeon_pokemon"],CustomName:'{"text":"pp_legendarydungeons:example/advanced/ceruledge"}',CustomNameVisible:0b}
```

Flutter Mane:

```mcfunction
/summon minecraft:armor_stand ~ ~ ~ {Invisible:1b,Marker:1b,NoGravity:1b,Tags:["pp_feature","pp_dungeon_pokemon"],CustomName:'{"text":"pp_legendarydungeons:example/advanced/flutter_mane"}',CustomNameVisible:0b}
```

Mismagius:

```mcfunction
/summon minecraft:armor_stand ~ ~ ~ {Invisible:1b,Marker:1b,NoGravity:1b,Tags:["pp_feature","pp_dungeon_pokemon"],CustomName:'{"text":"pp_legendarydungeons:example/advanced/mismagius"}',CustomNameVisible:0b}
```

Naganadel:

```mcfunction
/summon minecraft:armor_stand ~ ~ ~ {Invisible:1b,Marker:1b,NoGravity:1b,Tags:["pp_feature","pp_dungeon_pokemon"],CustomName:'{"text":"pp_legendarydungeons:example/advanced/naganadel"}',CustomNameVisible:0b}
```

## Example: additional rewards

```json
"loot": {
  "mode": "additional",
  "table": "pp_legendarydungeons:loot_injections/dungeon/advanced_reward"
}
```

## Example: full replacement

```json
"loot": {
  "mode": "replace",
  "table": "pp_legendarydungeons:loot_injections/dungeon/advanced_reward"
}
```

Replacement affects only the Pokémon spawned with that dungeon profile. It
does not globally replace the species' Cobblemon drops.
