# Cobblemon: Explore Legendary Dungeons — Documentation

This directory contains the project documentation for the current Fabric build and the Architectury Fabric + NeoForge migration.

## Authoritative current baseline

Unless a newer completed phase document explicitly replaces these values, use this baseline:

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
| NeoForge | Planned; initial alignment target `21.1.214` |
| Kotlin for Forge | Planned target `5.10.0` |

The repository is still physically a single-module Fabric project. Phases 1 through 5 moved its important internal boundaries to shared or loader-neutral APIs:

- Shared common and client initializers.
- Architectury lifecycle, tick, interaction, break, placement, reload, trade, loot, registry, and networking boundaries.
- Shared Cobblemon event integration where Cobblemon already exposes common APIs.
- Thin Fabric main and client entrypoints.

Phase 6 passed and is protected by the pre-split checkpoint tag. Phase 7 is now in progress; module build files are staged while the original root Fabric build remains active.

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

The master plan is the durable high-level source of truth. Each phase document records the exact scope, changed paths, tests, risks, corrections, and completion criteria for that stage.

## Documentation precedence

When older documentation conflicts with newer migration work, use this order:

1. Newest completed migration phase document.
2. `MULTILOADER_MIGRATION_MASTER_PLAN.md`.
3. This documentation index.
4. Older gameplay and development pages.

Some older pages were written before Mega Showdown became required and before the project moved to a Cobblemon 1.7.3-only baseline.

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

These identifiers must remain stable throughout the migration:

| Purpose | Stable value |
|---|---|
| Public display name | `Cobblemon: Explore Legendary Dungeons` |
| Mod ID / data namespace | `pp_legendarydungeons` |
| Java package | `porker.pp_legendarydungeons` |
| Shared main initializer | `porker.pp_legendarydungeons.LegendaryDungeons` |
| Shared client initializer | `porker.pp_legendarydungeons.LegendaryDungeonsClient` |
| Current Fabric main entrypoint | `porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons` |
| Current Fabric client entrypoint | `porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeonsClient` |

Do not rename the mod ID, Java package, resource namespace, registry IDs, block-entity IDs, saved-data IDs, scoreboard names, command tags, payload IDs, structure IDs, function IDs, loot-table IDs, WTrader IDs, or map target IDs as part of the loader migration.

## Migration design rules

New and migrated gameplay should use shared/common code by default. Use this priority:

1. Vanilla Minecraft API or standard resources.
2. Cobblemon shared/common API.
3. Architectury common API.
4. Shared mixin against stable vanilla or Cobblemon classes.
5. A small platform interface or `@ExpectPlatform` boundary.
6. Separate Fabric and NeoForge implementations only when no reliable shared approach exists.

Platform modules should stay thin. Stability, scalability, multiplayer correctness, server-authoritative validation, and dedicated-server safety take priority over loader-specific shortcuts.
