# Multi-loader Migration Phase Index

## Purpose

This directory stores the focused records for each stage of the Architectury
Fabric + NeoForge migration.

The high-level source of truth is:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\MULTILOADER_MIGRATION_MASTER_PLAN.md
```

Each phase document should contain:

- The phase goal.
- Exact files changed.
- APIs being replaced.
- Behavior that must remain unchanged.
- Step-by-step implementation notes.
- Build and gameplay tests.
- Known risks.
- Completion status.
- Follow-up items deliberately deferred to later phases.

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
| 9 | Future: cross-loader parity and hardening | Planned |
| 10 | Future: release workflow | Planned |

A phase may be changed to complete only after its required build, startup,
artifact, multiplayer, persistence, and documentation checks have passed.
