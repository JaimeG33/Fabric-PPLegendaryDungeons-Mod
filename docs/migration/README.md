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
| 0 | `PHASE_00_BRANCH_AND_BASELINE.md` | Ready to perform |
| 1 | `PHASE_01_ARCHITECTURY_DIRECT_DEPENDENCY.md` | Ready to perform |
| 2 | Future: common event foundation | Planned |
| 3 | Future: interaction and protection events | Planned |
| 4 | Future: deferred registries | Planned |
| 5 | Future: networking and client boundary | Planned |
| 6 | Future: Fabric regression checkpoint | Planned |
| 7 | Future: module split | Planned |
| 8 | Future: NeoForge bootstrap | Planned |
| 9 | Future: parity and hardening | Planned |
| 10 | Future: release workflow | Planned |

Create the next focused document when that phase begins. Record actual changed paths and test outcomes rather than relying only on the original plan.
