# Multi-loader Migration Phase Index

## Purpose

This directory stores focused records for each stage of the Architectury Fabric + NeoForge migration.

The high-level source of truth is:

- `../MULTILOADER_MIGRATION_MASTER_PLAN.md`

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
| 5 | `PHASE_05_SHARED_NETWORKING_AND_CLIENT_BOUNDARY.md` | Ready to apply and verify |
| 6 | Future: Fabric regression checkpoint | Planned |
| 7 | Future: module split | Planned |
| 8 | Future: NeoForge bootstrap | Planned |
| 9 | Future: parity and hardening | Planned |
| 10 | Future: release workflow | Planned |

Update a phase from “ready” to “complete” only after its clean build, client startup, dedicated-server startup, and focused gameplay tests have passed.
