# Phase 7 — Split into Common, Fabric, and NeoForge Modules

## Status

**In progress — Step 7E validation.**

## Starting checkpoint

- Branch: `architectury-multiloader-migration`
- Required tag: `architectury-fabric-checkpoint-phase6`
- Phase 7D structural commit: `6de88e45cc1eca2b702fb7d3f9971a5f7f7bb19b`
- Phase 7D common-boundary fix: `72766dd968f90ee21173afb2d44e4bdef773b0ff`
- Stable mod ID and namespace: `pp_legendarydungeons`
- Stable Java package root: `porker.pp_legendarydungeons`
- Minecraft: `1.21.1`
- Cobblemon: `1.7.3+1.21.1`
- Architectury API: `13.0.8`

## Goal

Physically separate the project into `common`, `fabric`, and `neoforge`
modules without duplicating gameplay implementations or changing persistent
identifiers.

## Step status

| Step | Purpose | Status |
|---|---|---|
| 7A | Complete and tag the Phase 6 checkpoint | Complete |
| 7B | Stage module build files and directories | Complete |
| 7C | Copy and verify shared/platform source layout | Complete |
| 7D | Activate the multi-project build and remove root `src` | Complete; common and Fabric build/client verified |
| 7E | Static audits, both platform builds, artifact checks, and Fabric multiplayer/persistence validation | In progress |

## Active module structure

```text
project-root/
|-- common/
|   `-- src/main/
|       |-- java/porker/pp_legendarydungeons/...
|       `-- resources/...
|-- fabric/
|   `-- src/main/
|       |-- java/porker/pp_legendarydungeons/
|       |   |-- ProfessorPorkersLegendaryDungeons.java
|       |   `-- ProfessorPorkersLegendaryDungeonsClient.java
|       `-- resources/fabric.mod.json
`-- neoforge/
    `-- src/main/
        |-- java/porker/pp_legendarydungeons/neoforge/...
        `-- resources/META-INF/neoforge.mods.toml
```

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
- Unavoidable NeoForge-only adapters only if Phase 8 proves they are required.

## Step 7D verification completed

The activated split has already passed:

- Common compilation.
- Fabric compilation.
- Split Fabric client startup.
- World loading.
- Parent controller registration and editor opening.
- Zone controller registration and editor opening.
- Shared controller networking in the integrated client.

Twenty-three stale shared references to the Fabric entrypoint were corrected to
use `LegendaryDungeons`, preserving the common-first ownership boundary.

## Deferred issue

`DungeonRuleZoneScreen` can exceed the visible height at GUI scale `4` or some
`Auto` scale/resolution combinations. Save and Preview remain functional and
the screen displays correctly at lower GUI scales. The UI layout fix is deferred
until after the loader migration.

## Stable identifiers

Do not rename the mod ID, namespace, Java package root, blocks, block entities,
saved-data keys, packet IDs, scoreboards, command tags, structures, functions,
loot tables, WTrader IDs, or map targets.

## Completion conditions

- Root `src` remains removed.
- `common` contains no direct Fabric or NeoForge Java imports.
- Platform modules contain no duplicated gameplay logic or shared resources.
- Fabric builds and its final JAR contains shared content exactly once.
- NeoForge builds and its final JAR contains shared content exactly once.
- Each final JAR contains only its own loader metadata and entrypoints.
- Fabric integrated-client behavior remains equivalent to Phase 6.
- Fabric dedicated-server connected-client networking passes.
- Controller state persists through disconnect and server restart.
- Validation results and documentation are committed.
