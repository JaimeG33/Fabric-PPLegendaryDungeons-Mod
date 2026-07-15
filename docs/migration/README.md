# Multi-loader Migration Phase Index

## Purpose

This directory records the migration from the original Fabric-only layout to
the current Architectury common/Fabric/NeoForge project.

Current development guidance lives in:

```text
docs/README.md
docs/development/ARCHITECTURE_GUIDE.md
```

Older phase documents are historical records and may contain obsolete paths,
versions, or one-loader assumptions.

## Phase status

| Phase | Document | Status |
|---|---|---|
| 0 | `PHASE_00_BRANCH_AND_BASELINE.md` | Complete |
| 1 | `PHASE_01_ARCHITECTURY_DIRECT_DEPENDENCY.md` | Complete and locally verified |
| 2 | `PHASE_02_COMMON_EVENT_FOUNDATION.md` | Complete and locally verified |
| 3 | `PHASE_03_INTERACTION_AND_PROTECTION_EVENTS.md` | Complete and locally verified |
| 4 | `PHASE_04_SHARED_DEFERRED_REGISTRIES.md` | Complete and locally verified |
| 5 | `PHASE_05_SHARED_NETWORKING_AND_CLIENT_BOUNDARY.md` | Complete and user-tested |
| 6 | `PHASE_06_FABRIC_REGRESSION_CHECKPOINT.md` | Complete and locally verified |
| 7 | `PHASE_07_COMMON_FABRIC_NEOFORGE_SPLIT.md` | Complete and user-tested |
| 8 | `PHASE_08_NEOFORGE_BOOTSTRAP.md` | Complete and user-tested |
| 9 | `PHASE_09_CROSS_LOADER_PARITY.md` | Release-candidate smoke validation complete; extended hardening deferred |
| 10 | `PHASE_10_RELEASE_WORKFLOW.md` | In progress — 1.1.0 documentation, artifact, and platform-publication preparation |

## Current architecture result

The migration produced three modules:

```text
common
fabric
neoforge
```

`common` owns shared gameplay and resources. Fabric and NeoForge own thin
entrypoints, metadata, dependencies, run configuration, and only the platform
adapters that cannot be shared reliably.

All new development should follow the current architecture guide rather than
copying implementation patterns from an intermediate migration phase.

## Phase 9 closure

Phase 9 closed for the initial release candidate after representative Fabric
and NeoForge client, fresh-world, dedicated-server, controller, and Rayquaza
testing.

The following remain explicitly deferred:

- Exhaustive two-player race testing.
- Adversarial packet and invalid-input testing.
- Full dependency-floor combinations.
- Complete loot, trader, map, and structure matrices.
- Cross-loader existing-world certification.
- Extended performance and leak sessions.

Deferred means not completed, not passed.

## Phase 10 scope

Phase 10 prepares version `1.1.0` for distribution through Modrinth and
CurseForge.

It owns:

- Version and documentation consistency.
- Common-first architecture guidance.
- Clean Fabric and NeoForge artifact generation.
- Inspection and clean-instance testing of the exact release JARs.
- Platform metadata and dependency declarations.
- Recording publication results.

A GitHub Release is not required.
