# Phase 7 — Split into Common, Fabric, and NeoForge Modules

## Status

**In progress.**

## Starting checkpoint

- Branch: `architectury-multiloader-migration`
- Required tag: `architectury-fabric-checkpoint-phase6`
- Stable mod ID and namespace: `pp_legendarydungeons`
- Stable Java package root: `porker.pp_legendarydungeons`
- Minecraft: `1.21.1`
- Cobblemon: `1.7.3+1.21.1`
- Architectury API: `13.0.8`

## Goal

Physically separate the project into `common`, `fabric`, and `neoforge` modules without duplicating gameplay implementations or changing persistent identifiers.

## Step status

| Step | Purpose | Status |
|---|---|---|
| 7A | Complete and tag the Phase 6 checkpoint | Complete before Step 7B |
| 7B | Stage module build files and directories | Applied; awaiting commit verification |
| 7C | Copy and verify shared/platform source layout | Not applied |
| 7D | Activate the multi-project build and remove root `src` | Not applied |
| 7E | Run static, artifact, Fabric, and NeoForge build checks | Not applied |

## Ownership rules

### Common

`common` owns all gameplay logic and shared resources, including:

- Shared initializers.
- Dungeon rules, saved data, blocks, block entities, screens, and networking payloads.
- Architectury events and deferred registries.
- Cobblemon shared-event integrations.
- Tick systems, maps, summons, secrets, trades, loot, and WTrader logic.
- Assets, data, structures, functions, loot tables, tags, and WTrader JSON.
- Shared mixins targeting vanilla or shared Cobblemon classes.

### Fabric

`fabric` owns only:

- `ProfessorPorkersLegendaryDungeons`.
- `ProfessorPorkersLegendaryDungeonsClient`.
- `fabric.mod.json`.
- Fabric platform dependencies and run/build configuration.

### NeoForge

`neoforge` owns only:

- NeoForge main and physical-client entrypoints.
- `META-INF/neoforge.mods.toml`.
- NeoForge platform dependencies and run/build configuration.
- Unavoidable NeoForge-only adapters, if later proven necessary.

## Stable identifiers

Do not rename the mod ID, namespace, Java package root, blocks, block entities, saved-data keys, packet IDs, scoreboards, command tags, structures, functions, loot tables, WTrader IDs, or map targets.

## Completion conditions

- Root `src` is removed only after verified copies exist.
- `common` contains no Fabric or NeoForge imports.
- Platform modules contain no duplicated gameplay logic or shared resources.
- Fabric builds, starts, and matches the Phase 6 checkpoint.
- NeoForge produces a build or reaches only clearly documented Phase 8 dependency/bootstrap issues.
- Both final JARs contain shared classes/resources exactly once and only their own loader metadata.
