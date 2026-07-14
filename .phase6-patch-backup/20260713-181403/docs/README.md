# Cobblemon: Explore Legendary Dungeons — Documentation

This directory contains the project documentation for the current Fabric build and the planned Architectury Fabric + NeoForge migration.

## Authoritative current baseline

Unless a newer phase document explicitly replaces these values, use this baseline:

| Component | Current target |
|---|---|
| Minecraft | `1.21.1` |
| Java | `21` |
| Cobblemon | `1.7.3+1.21.1` |
| Mega Showdown | `1.6.9+1.7.3+1.21.1` |
| Architectury API | `13.0.8` |
| Fabric Loader | `0.17.2` |
| Fabric API | `0.116.6+1.21.1` |
| Fabric Language Kotlin | `1.13.6+kotlin.2.2.20` |
| Accessories | `1.1.0-beta.52+1.21.1` |
| NeoForge | planned; initial alignment target `21.1.214` |

The repository remains a single-module Fabric project. Phases 1 through 4 established shared Architectury events and deferred registries. Phase 5 moves dungeon-rule packet transport and client initialization behind Architectury while retaining thin Fabric entrypoints.

## Start here for the multi-loader migration

1. [`MULTILOADER_MIGRATION_MASTER_PLAN.md`](MULTILOADER_MIGRATION_MASTER_PLAN.md)
2. [`migration/README.md`](migration/README.md)
3. [`migration/PHASE_00_BRANCH_AND_BASELINE.md`](migration/PHASE_00_BRANCH_AND_BASELINE.md)
4. [`migration/PHASE_01_ARCHITECTURY_DIRECT_DEPENDENCY.md`](migration/PHASE_01_ARCHITECTURY_DIRECT_DEPENDENCY.md)
5. [`migration/PHASE_02_COMMON_EVENT_FOUNDATION.md`](migration/PHASE_02_COMMON_EVENT_FOUNDATION.md)
6. [`migration/PHASE_03_INTERACTION_AND_PROTECTION_EVENTS.md`](migration/PHASE_03_INTERACTION_AND_PROTECTION_EVENTS.md)
7. [`migration/PHASE_04_SHARED_DEFERRED_REGISTRIES.md`](migration/PHASE_04_SHARED_DEFERRED_REGISTRIES.md)
8. [`migration/PHASE_05_SHARED_NETWORKING_AND_CLIENT_BOUNDARY.md`](migration/PHASE_05_SHARED_NETWORKING_AND_CLIENT_BOUNDARY.md)

The master plan is intended to be the durable context document for future development sessions. Each completed phase receives its own focused document containing the exact scope, changed paths, tests, risks, and completion criteria.

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

Some older documents were written before Mega Showdown became required and before the project moved to a Cobblemon 1.7.3-only baseline. When version statements conflict, the migration master plan and the newest completed phase document take priority until the older page is revised.

## Stable project identifiers

These identifiers are deliberately retained throughout the migration:

| Purpose | Stable value |
|---|---|
| Public display name | `Cobblemon: Explore Legendary Dungeons` |
| Mod ID / data namespace | `pp_legendarydungeons` |
| Java package | `porker.pp_legendarydungeons` |
| Current Fabric main entrypoint | `porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons` |
| Future shared initializer | `porker.pp_legendarydungeons.LegendaryDungeons` |
| Current Fabric client entrypoint | `porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeonsClient` |

Do not rename the mod ID, Java package, resource namespace, saved-data IDs, scoreboard names, payload IDs, structure IDs, or other persistent identifiers as part of the loader migration.
