# Phase 9 — Cross-loader Parity and Hardening

## Status

**In progress — Phase 9A matrix created.**

## Protected baseline

- Branch: `architectury-multiloader-migration`
- Phase 8 runtime head before documentation closure:
  `d7038af0fff54831f10e83c277cc92bc0a45ac64`
- Required Phase 8 tag: `architectury-multiloader-checkpoint-phase8`
- Minecraft: `1.21.1`
- Java: `21`
- Cobblemon: `1.7.3+1.21.1`
- Fabric Loader: `0.17.2`
- NeoForge: `21.1.214`
- Architectury API: `13.0.8`
- Development Mega Showdown: `1.6.9+1.7.3+1.21.1`
- Compatibility-floor test: `1.6.7+1.7.3+1.21.1`

Do not modify or merge `master` during this phase.

## Result vocabulary

Use only:

- `PASS`
- `FAIL`
- `NOT TESTED`
- `NOT APPLICABLE`
- `DEFERRED`

A result is `PASS` only after that exact scenario was performed. Phase 7 or
Phase 8 results may guide setup, but they do not automatically satisfy Phase 9.

## Phase rules

1. Test one feature group on both loaders before moving to the next group.
2. Preserve exact logs, world names, pack versions, commands, and reproduction
   steps for every failure.
3. Prefer shared fixes in `common`.
4. Prefer vanilla, Cobblemon common APIs, and Architectury common APIs before
   loader-specific hooks.
5. Keep public registry IDs, packet IDs, saved-data names, function IDs, tags,
   and structure IDs unchanged.
6. Retest the failing loader, the other loader, and dedicated-server behavior
   after every code fix.
7. Do not enable or rebalance Crystal Caves.
8. Treat the GUI-scale-4 zone-editor overflow as `DEFERRED` unless loaders differ.

## Parity matrix

| Group | Feature | Fabric SP | Fabric dedicated | NeoForge SP | NeoForge dedicated | Existing-world result | Notes / failure details |
|---|---|---|---|---|---|---|---|
| 9B | Clean common, Fabric, and NeoForge build | NOT TESTED | NOT APPLICABLE | NOT TESTED | NOT APPLICABLE | NOT APPLICABLE |  |
| 9B | Client title screen, mod list, and icon | NOT TESTED | NOT APPLICABLE | NOT TESTED | NOT APPLICABLE | NOT APPLICABLE |  |
| 9B | Fresh world startup and registry/resource baseline | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT APPLICABLE |  |
| 9C | Parent plus two linked zone controllers | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Controller save, reopen, world reopen, and server restart persistence | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Overlapping-zone priority and removal fallback | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Parent inheritance, unlinked zones, and relinking | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Block breaking and allowlisted breaks | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Block placement and allowlisted placement | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Doors, trapdoors, gates, buttons, levers, and containers | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Beds and respawn anchors where applicable | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Cobblemon PCs, healers, portable utilities, and other utility blocks | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Buckets, fluids, fire, entity interaction, item use, vehicles, teleport items | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Operator, creative, survival, and ordinary-player authority | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | TNT, creeper, bed, and respawn-anchor explosions | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Fire spread, mob griefing, pistons, and fluid spread where handled | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9C | Two-player simultaneous controller/rule activity | NOT APPLICABLE | NOT TESTED | NOT APPLICABLE | NOT TESTED | NOT TESTED |  |
| 9D | Parent editor: every field, save, cancel, preview, invalid values | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9D | Zone editor: every field, save, cancel, preview, numeric boundaries | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9D | Packet permission, distance, block-type, and stale-controller checks | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9D | GUI scale 4 overflow | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Known non-blocking issue unless loaders differ. |
| 9E | Active static feature trigger distance, cleanup, re-entry, and restart | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9E | Two-player static-feature duplicate prevention | NOT APPLICABLE | NOT TESTED | NOT APPLICABLE | NOT TESTED | NOT TESTED |  |
| 9E | Rayquaza Tower normal summon flow | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9E | Rayquaza Tower secret/alternate summon flow | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9E | Crystal Caves activation or balancing | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | Intentionally dormant; do not enable. |
| 9F | Mega Showdown 1.6.7 item IDs, tags, loot, trades, and secret checks | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9F | Mega Showdown 1.6.9 item IDs, tags, loot, trades, and secret checks | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9F | Blue Orb, Red Orb, Deoxys Meteorite, blank stones/Z-Crystals | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9F | Mega trader/loot items, special pools, and Zygarde tower items | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9G | Vanilla chest, igloo, cartographer, and entity loot injection | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9G | Repeated `/reload` does not duplicate or invalidate loot injection | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9G | Cobblemon Pokémon loot: direct player defeat | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9G | Cobblemon Pokémon loot: battle and multiplayer attribution | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | Verify the correct player, not merely the first battle player. |
| 9G | Cobblemon Pokémon loot: non-player defeat and duplicate prevention | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9H | Natural wandering trader replacement and persistent one-roll tag | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9H | Natural trader llama behavior and no restart/replacement loop | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9H | Static/custom trader spawn, marker cleanup, profile, and persistence | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9H | Representative basic, Cobblemon, map, Mega, joke, weighted pools | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9H | Cartographer additions, vanilla retention, and no reload duplication | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9I | Active maps create and locate the correct structure target | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9I | Map survives restart and copied-world cross-loader opening | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9J | Every enabled structure generates with matching biome/height/terrain | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9J | Jigsaw pools, internal loot, markers, and new chunks after reload/restart | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9K | Objectives, teams, load/tick functions, and scheduled functions | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9K | Several `/reload` cycles without duplicate messages/entities/tasks | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9L | Old Fabric-only world copy | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED |  |
| 9L | Current split Fabric world copy opened on Fabric | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED |  |
| 9L | Current NeoForge world copy reopened on NeoForge | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED |  |
| 9L | Current Fabric world copy opened on NeoForge | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED |  |
| 9L | Controller NBT and saved-data bounds hardening | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED | Review max clamps before opening hand-edited/corrupt copies. |
| 9M | Fabric development environment with Mega Showdown 1.6.9 | NOT TESTED | NOT TESTED | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED |  |
| 9M | Fabric target modpack with Mega Showdown 1.6.7 | NOT TESTED | NOT TESTED | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED |  |
| 9M | NeoForge development environment with Mega Showdown 1.6.9 | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9M | NeoForge target modpack with Mega Showdown 1.6.7 | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED | NOT TESTED | NOT TESTED |  |
| 9N | 20–30 minute Fabric dedicated-server performance/leak run | NOT APPLICABLE | NOT TESTED | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED |  |
| 9N | 20–30 minute NeoForge dedicated-server performance/leak run | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT TESTED | NOT TESTED |  |
| 9N | Repeated connect/disconnect, reload, area traversal, and two-player cases | NOT APPLICABLE | NOT TESTED | NOT APPLICABLE | NOT TESTED | NOT TESTED |  |

## Evidence log

Create one entry per test session.

```text
Date/time:
Branch commit:
Loader:
Environment:
Minecraft:
Cobblemon:
Mega Showdown:
Other relevant mods:
World copy / fresh world:
Commands or setup:
Rows tested:
Result:
Log path:
Screenshots or recordings:
Notes:
```

## Failure record

```text
Failure ID:
Matrix row:
Classification:
- shared
- Fabric-only
- NeoForge-only
- dependency-specific
- data-specific
- multiplayer-only

Exact reproduction:
Expected:
Actual:
Relevant log excerpt:
Suspected owner:
Changed files:
Retest Fabric SP:
Retest Fabric dedicated:
Retest NeoForge SP:
Retest NeoForge dedicated:
Existing-world retest:
Final status:
```

## Completion gate

Phase 9 is complete only when:

- Both loader JARs build cleanly.
- Fabric and NeoForge fresh worlds pass.
- Fabric and NeoForge dedicated servers pass.
- Controller, protection, networking, and persistence behavior match.
- Active static features and legendary summons match.
- Supported Mega Showdown versions and secret-item systems match.
- Loot, Pokémon drops, traders, cartographers, maps, and structures match.
- Scoreboards, functions, reloads, and scheduled systems remain idempotent.
- Existing-world copies retain blocks, NBT, links, saved data, maps, structures,
  traders, teams, and scoreboards.
- The four required development/target-modpack combinations pass.
- Performance/leak sessions do not show unexplained growth or tick degradation.
- Intentional differences and deferred issues are documented.
- `PHASE_09_VALIDATION_RESULTS.md` is committed.
- The migration index says `Complete and user-tested`.
- The checkpoint tag exists:

```text
architectury-multiloader-checkpoint-phase9
```
