# Cobblemon: Explore Legendary Dungeons — Documentation

This is the main documentation index for the Architectury-based Fabric and
NeoForge project.

## Current release baseline

| Component | Current target |
|---|---|
| Mod version | `1.1.0` |
| Minecraft | `1.21.1` |
| Java | `21` |
| Cobblemon | `1.7.3+1.21.1` |
| Development Mega Showdown | `1.6.9+1.7.3+1.21.1` |
| Loader-accepted Mega Showdown | `1.6.7` or newer |
| Architectury API | `13.0.8` |
| Fabric Loader | `0.17.2` |
| Fabric API | `0.116.6+1.21.1` |
| Fabric Language Kotlin | `1.13.6+kotlin.2.2.20` |
| Accessories | `1.1.0-beta.52+1.21.1` |
| NeoForge | `21.1.214` |
| Kotlin for Forge | `5.10.0` |

The authoritative version value is `mod_version` in the root
`gradle.properties`. Fabric and NeoForge metadata expand that value during
resource processing.

## Architecture at a glance

The repository has three Gradle modules:

```text
common/
fabric/
neoforge/
```

### `common`

This is the default home for project code and resources.

Place the following in `common` whenever the behavior can be shared:

- Gameplay systems and rules.
- Blocks, block entities, items, registries, saved data, and services.
- Server-authoritative networking handlers and validation.
- Cobblemon integration that uses shared Cobblemon APIs.
- Architectury event registration and lifecycle hooks.
- Mixins that target shared vanilla or Cobblemon classes.
- Datapack functions, tags, loot tables, structures, template pools, recipes,
  language files, and other loader-neutral resources.
- Tests for shared behavior.

Primary paths:

```text
common/src/main/java/
common/src/main/resources/
common/src/test/
```

### `fabric`

Keep this module thin. It should contain only Fabric-specific entrypoints,
metadata, development dependencies, run configuration, and an adapter or hook
that cannot be implemented reliably in shared code.

Primary paths:

```text
fabric/src/main/java/
fabric/src/main/resources/fabric.mod.json
fabric/src/main/resources/icon.png
```

### `neoforge`

Keep this module thin for the same reason. It should contain NeoForge-specific
entrypoints, metadata, development dependencies, client distribution setup,
and unavoidable NeoForge adapters.

Primary paths:

```text
neoforge/src/main/java/
neoforge/src/main/resources/META-INF/neoforge.mods.toml
neoforge/src/main/resources/icon.png
```

Read [`development/ARCHITECTURE_GUIDE.md`](development/ARCHITECTURE_GUIDE.md)
before adding a new system or moving code between modules.

## Design priorities

All new work should favor:

1. **Shared/common implementation.**
2. **Server-authoritative behavior.**
3. **Per-world or per-instance state instead of global mutable state.**
4. **Bounded and data-driven processing that scales to multiple players.**
5. **Idempotent load, reload, registration, and scheduled behavior.**
6. **Persistent identifiers that remain compatible with existing worlds.**
7. **Thin loader modules with the same visible behavior on both loaders.**

Preferred implementation order:

1. Vanilla Minecraft API or standard data/resource files.
2. Cobblemon shared/common API.
3. Architectury common API.
4. Shared mixin against a stable vanilla or Cobblemon target.
5. A small platform abstraction or `@ExpectPlatform` boundary.
6. Separate Fabric and NeoForge implementations only as a last resort.

Do not duplicate an entire gameplay system in both loader modules merely
because the project has two loaders.

## Multiplayer and stability rules

- Make the server the source of truth for gameplay changes.
- Validate packet sender, permission, dimension, distance, block/entity type,
  and current state before applying a request.
- Store dungeon state by world, block entity, saved-data record, or persistent
  UUID rather than a global “current player” or “current dungeon.”
- Avoid one full entity scan per player per tick.
- Clamp user-controlled and persisted numeric values before using them in loops
  or spatial indexes.
- Make cleanup and completion operations safe to run more than once.
- Keep client-only classes out of common server initialization paths.
- Preserve registry IDs, packet IDs, NBT keys, saved-data IDs, function IDs,
  tags, and structure IDs unless a migration plan exists.
- Test both singleplayer and a dedicated server whenever networking,
  persistence, permissions, or multiple players are involved.

## Build and output

Run a full release build from the repository root:

```powershell
.\gradlew.bat `
    clean `
    :common:build `
    :fabric:build `
    :neoforge:build `
    --no-daemon `
    --no-parallel `
    --max-workers=2 `
    --console=plain
```

The uploadable remapped JARs are expected at:

```text
fabric/build/libs/cobblemon-eld-1.1.0-fabric-mc1.21.1-cob1.7.3.jar
neoforge/build/libs/cobblemon-eld-1.1.0-neoforge-mc1.21.1-cob1.7.3.jar
```

Do not upload:

```text
*-sources.jar
*-dev-slim.jar
*-dev-shadow.jar
common/build/libs/*
```

The Fabric and NeoForge release JARs are uploaded separately to Modrinth and
CurseForge. A GitHub Release is not part of the project’s required publication
workflow.

## Stable project identifiers

| Purpose | Stable value |
|---|---|
| Public display name | `Cobblemon: Explore Legendary Dungeons` |
| Mod ID / data namespace | `pp_legendarydungeons` |
| Java package | `porker.pp_legendarydungeons` |
| Shared main initializer | `porker.pp_legendarydungeons.LegendaryDungeons` |
| Shared client initializer | `porker.pp_legendarydungeons.LegendaryDungeonsClient` |
| Fabric main entrypoint | `porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons` |
| Fabric client entrypoint | `porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeonsClient` |
| NeoForge main entrypoint | `porker.pp_legendarydungeons.neoforge.ProfessorPorkersLegendaryDungeonsNeoForge` |
| NeoForge client entrypoint | `porker.pp_legendarydungeons.neoforge.ProfessorPorkersLegendaryDungeonsNeoForgeClient` |

Do not rename public or persistent identifiers as routine cleanup. Existing
worlds, datapacks, structure templates, loot tables, saved data, packets, and
other addons may depend on them.

## Development guides

Start with:

1. [`development/ARCHITECTURE_GUIDE.md`](development/ARCHITECTURE_GUIDE.md)
2. [`development/PROJECT_IDENTIFIERS_AND_BUILD.md`](development/PROJECT_IDENTIFIERS_AND_BUILD.md)
3. [`development/ADDING_CONTENT.md`](development/ADDING_CONTENT.md)
4. [`COMPATIBILITY.md`](COMPATIBILITY.md)
5. [`RELEASE_CHECKLIST.md`](RELEASE_CHECKLIST.md)

Additional guides:

- `development/WTRADER_JSON_SCHEMA.md`
- `development/DEVELOPMENT_MEMORY_AND_BUILD_PERFORMANCE.md`
- `gameplay/MAPS.md`
- `gameplay/LOOT_INJECTIONS.md`
- `gameplay/TRADERS.md`
- `gameplay/LEGENDARY_SUMMONS_AND_DUNGEON_RULES.md`
- [`development/RUNNING_DEVELOPMENT_CLIENTS.md`](development/RUNNING_DEVELOPMENT_CLIENTS.md)

## Migration history

The migration documents explain how the original Fabric project became the
current common/Fabric/NeoForge layout:

1. [`MULTILOADER_MIGRATION_MASTER_PLAN.md`](MULTILOADER_MIGRATION_MASTER_PLAN.md)
2. [`migration/README.md`](migration/README.md)
3. [`migration/PHASE_09_VALIDATION_RESULTS.md`](migration/PHASE_09_VALIDATION_RESULTS.md)
4. [`migration/PHASE_10_RELEASE_WORKFLOW.md`](migration/PHASE_10_RELEASE_WORKFLOW.md)

Migration documents are historical records. For current development decisions,
use this README and the architecture guide before older phase notes.
