# Cobblemon: Explore Legendary Dungeons — Documentation

This directory contains the project documentation for the Architectury-based
Fabric + NeoForge codebase.

## Authoritative current baseline

Unless a newer completed phase document explicitly replaces these values, use
this baseline:

| Component | Current target |
|---|---|
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

The repository is physically split into:

- `common` for shared gameplay, resources, Architectury APIs, and shared
  Cobblemon integration.
- `fabric` for thin Fabric entrypoints, metadata, dependencies, and build/run
  configuration.
- `neoforge` for thin NeoForge entrypoints, metadata, dependencies, and
  build/run configuration.

Phases 1 through 6 established and verified the shared boundaries. Phase 7
completed and validated the physical module split. Phase 8 completed and
user-tested the NeoForge client, fresh-world startup, dedicated server,
connected-client networking, persistence, release artifact, dependency errors,
mod-list icon, and Mega Showdown 1.6.7 compatibility.

Phase 9 is the next phase and owns complete cross-loader parity and hardening.

## Start here for the multi-loader migration

1. [`MULTILOADER_MIGRATION_MASTER_PLAN.md`](MULTILOADER_MIGRATION_MASTER_PLAN.md)
2. [`migration/README.md`](migration/README.md)
3. [`migration/PHASE_00_BRANCH_AND_BASELINE.md`](migration/PHASE_00_BRANCH_AND_BASELINE.md)
4. [`migration/PHASE_01_ARCHITECTURY_DIRECT_DEPENDENCY.md`](migration/PHASE_01_ARCHITECTURY_DIRECT_DEPENDENCY.md)
5. [`migration/PHASE_02_COMMON_EVENT_FOUNDATION.md`](migration/PHASE_02_COMMON_EVENT_FOUNDATION.md)
6. [`migration/PHASE_03_INTERACTION_AND_PROTECTION_EVENTS.md`](migration/PHASE_03_INTERACTION_AND_PROTECTION_EVENTS.md)
7. [`migration/PHASE_04_SHARED_DEFERRED_REGISTRIES.md`](migration/PHASE_04_SHARED_DEFERRED_REGISTRIES.md)
8. [`migration/PHASE_05_SHARED_NETWORKING_AND_CLIENT_BOUNDARY.md`](migration/PHASE_05_SHARED_NETWORKING_AND_CLIENT_BOUNDARY.md)
9. [`migration/PHASE_06_FABRIC_REGRESSION_CHECKPOINT.md`](migration/PHASE_06_FABRIC_REGRESSION_CHECKPOINT.md)
10. [`migration/PHASE_07_COMMON_FABRIC_NEOFORGE_SPLIT.md`](migration/PHASE_07_COMMON_FABRIC_NEOFORGE_SPLIT.md)
11. [`migration/PHASE_07_VALIDATION_CHECKLIST.md`](migration/PHASE_07_VALIDATION_CHECKLIST.md)
12. [`migration/PHASE_07_VALIDATION_RESULTS.md`](migration/PHASE_07_VALIDATION_RESULTS.md)
13. [`migration/PHASE_08_NEOFORGE_BOOTSTRAP.md`](migration/PHASE_08_NEOFORGE_BOOTSTRAP.md)
14. [`migration/PHASE_08_VALIDATION_RESULTS.md`](migration/PHASE_08_VALIDATION_RESULTS.md)

## Documentation precedence

When older documentation conflicts with newer migration work, use this order:

1. Newest completed migration phase document.
2. `MULTILOADER_MIGRATION_MASTER_PLAN.md`.
3. This documentation index.
4. Older gameplay and development pages.

## Existing gameplay documentation

- `gameplay/MAPS.md`
- `gameplay/LOOT_INJECTIONS.md`
- `gameplay/TRADERS.md`
- `gameplay/LEGENDARY_SUMMONS_AND_DUNGEON_RULES.md`

## Existing development documentation

- `development/ADDING_CONTENT.md`
- `development/PROJECT_IDENTIFIERS_AND_BUILD.md`
- `development/WTRADER_JSON_SCHEMA.md`
- `development/DEVELOPMENT_MEMORY_AND_BUILD_PERFORMANCE.md`
- `COMPATIBILITY.md`
- `RELEASE_CHECKLIST.md`

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

Do not rename persistent or public identifiers as part of the loader migration.

## Migration design rules

New and migrated gameplay should use shared/common code by default:

1. Vanilla Minecraft API or standard resources.
2. Cobblemon shared/common API.
3. Architectury common API.
4. Shared mixin against stable vanilla or Cobblemon classes.
5. A small platform interface or `@ExpectPlatform` boundary.
6. Separate Fabric and NeoForge implementations only when no reliable shared
   approach exists.

Platform modules should stay thin. Stability, scalability, multiplayer
correctness, server-authoritative validation, and dedicated-server safety take
priority over loader-specific shortcuts.
