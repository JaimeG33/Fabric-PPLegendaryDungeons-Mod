# Phase 9 — Cross-loader Parity and Hardening

## Status

**Release-candidate smoke validation complete — extended parity and hardening
remain deferred to post-release testing.**

## Protected baseline

- Branch: `architectury-multiloader-migration`
- Validation commit:
  `39724ef2fbba6e556454c80e2f31ccbc6b8923f4`
- Phase 8 runtime head before documentation closure:
  `d7038af0fff54831f10e83c277cc92bc0a45ac64`
- Required Phase 8 tag: `architectury-multiloader-checkpoint-phase8`
- Minecraft: `1.21.1`
- Java: `21`
- Cobblemon: `1.7.3+1.21.1`
- Fabric Loader: `0.17.2`
- NeoForge: `21.1.214`
- Architectury API: `13.0.8`
- Development Mega Showdown: `1.6.9+1.7.3+1.21.1`
- Compatibility-floor target: `1.6.7+1.7.3+1.21.1`

Do not modify or merge `master` during this phase.

## Release-candidate scope decision

The initial release is closing Phase 9 at a practical smoke-validation scope.

Completed manual testing covered:

- Fabric and NeoForge client startup.
- Fabric Mod Menu metadata and icon visibility.
- Fresh singleplayer worlds on both loaders.
- Local Fabric and NeoForge dedicated-server startup and shutdown.
- Representative controller, zone, rule, protection, editor, persistence, and
  restart behavior in singleplayer and local server environments.
- Normal and secret/alternate Rayquaza summon behavior on both loaders.
- Repeat-prevention behavior observed during representative Rayquaza testing.

The following remain deferred rather than being reported as passed:

- Exhaustive editor boundary and malformed-input testing.
- Packet distance, stale-controller, and adversarial permission testing.
- Two-client simultaneous actions and multiplayer race-condition testing.
- Full Mega Showdown version-floor verification.
- Exhaustive loot, Pokémon-drop attribution, trader, map, structure, reload,
  existing-world, target-modpack, and performance/leak matrices.

Deferred items may be exercised through release-candidate testing and initial
player feedback. Any reproduced failure should be documented and fixed in a
small shared/common patch where possible.

## Result vocabulary

Use only:

- `PASS`
- `FAIL`
- `NOT TESTED`
- `NOT APPLICABLE`
- `DEFERRED`

A result is `PASS` only when the corresponding representative scenario was
performed. A `PASS` in this release-candidate matrix does not claim exhaustive
coverage beyond the notes for that row.

## Phase rules

1. Preserve exact logs, world names, pack versions, commands, and reproduction
   steps for every reported failure.
2. Prefer shared fixes in `common`.
3. Prefer vanilla, Cobblemon common APIs, and Architectury common APIs before
   loader-specific hooks.
4. Keep public registry IDs, packet IDs, saved-data names, function IDs, tags,
   and structure IDs unchanged.
5. Retest the failing loader, the other loader, and dedicated-server behavior
   after every code fix.
6. Do not enable or rebalance Crystal Caves.
7. Keep the GUI-scale-4 zone-editor overflow `DEFERRED` unless loaders differ.

## Parity matrix

| Group | Feature | Fabric SP | Fabric dedicated | NeoForge SP | NeoForge dedicated | Existing-world result | Notes / failure details |
|---|---|---|---|---|---|---|---|
| 9B | Clean common, Fabric, and NeoForge build | PASS | NOT APPLICABLE | PASS | NOT APPLICABLE | NOT APPLICABLE | Both loader development builds launched successfully during validation. |
| 9B | Client title screen, mod list, and icon | PASS | NOT APPLICABLE | PASS | NOT APPLICABLE | NOT APPLICABLE | Fabric Mod Menu was added as a development-only runtime dependency; Fabric and NeoForge icons were confirmed. |
| 9B | Fresh world startup and registry/resource baseline | PASS | PASS | PASS | PASS | NOT APPLICABLE | Fresh client worlds and local dedicated servers started without observed blocking errors. |
| 9C | Parent plus two linked zone controllers | PASS | PASS | PASS | PASS | DEFERRED | Representative controller and linked-zone behavior was manually tested. |
| 9C | Controller save, reopen, world reopen, and server restart persistence | PASS | PASS | PASS | PASS | DEFERRED | Representative persistence and restart behavior was manually tested; broader existing-world migration remains deferred. |
| 9C | Overlapping-zone priority and removal fallback | PASS | PASS | PASS | PASS | DEFERRED | Representative priority and fallback behavior was manually tested. |
| 9C | Parent inheritance, unlinked zones, and relinking | PASS | PASS | PASS | PASS | DEFERRED | Representative linking behavior was manually tested. |
| 9C | Block breaking and allowlisted breaks | PASS | PASS | PASS | PASS | DEFERRED | Representative protection behavior was manually tested. |
| 9C | Block placement and allowlisted placement | PASS | PASS | PASS | PASS | DEFERRED | Representative protection behavior was manually tested. |
| 9C | Doors, trapdoors, gates, buttons, levers, and containers | PASS | PASS | PASS | PASS | DEFERRED | Representative interactions were manually tested; exhaustive block coverage remains deferred. |
| 9C | Beds and respawn anchors where applicable | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred to release-candidate and player testing. |
| 9C | Cobblemon PCs, healers, portable utilities, and other utility blocks | PASS | PASS | PASS | PASS | DEFERRED | Representative Cobblemon utility interaction was manually tested. |
| 9C | Buckets, fluids, fire, entity interaction, item use, vehicles, teleport items | PASS | PASS | PASS | PASS | DEFERRED | Representative interactions were tested; exhaustive item/entity coverage remains deferred. |
| 9C | Operator, creative, survival, and ordinary-player authority | PASS | PASS | PASS | PASS | DEFERRED | Representative authority behavior was manually tested; adversarial permission testing remains deferred. |
| 9C | TNT, creeper, bed, and respawn-anchor explosions | PASS | PASS | PASS | PASS | DEFERRED | Representative explosion protection was tested; every explosion source was not exhaustively repeated. |
| 9C | Fire spread, mob griefing, pistons, and fluid spread where handled | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred to release-candidate and player testing. |
| 9C | Two-player simultaneous controller/rule activity | NOT APPLICABLE | DEFERRED | NOT APPLICABLE | DEFERRED | DEFERRED | Multi-client race and simultaneous edit testing was not completed. |
| 9D | Parent editor: representative fields, save, cancel, and preview | PASS | PASS | PASS | PASS | DEFERRED | Representative editor use was tested. Exhaustive invalid-value and boundary testing remains deferred. |
| 9D | Zone editor: representative fields, save, cancel, and preview | PASS | PASS | PASS | PASS | DEFERRED | Representative editor use was tested. Exhaustive numeric-boundary testing remains deferred. |
| 9D | Packet permission, distance, block-type, and stale-controller checks | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Normal server-authoritative use was observed; adversarial packet validation remains deferred. |
| 9D | GUI scale 4 overflow | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Known non-blocking issue unless loaders differ. |
| 9E | Active static feature trigger distance, cleanup, re-entry, and restart | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Rayquaza was tested, but the complete generic static-feature lifecycle matrix was not. |
| 9E | Two-player static-feature duplicate prevention | NOT APPLICABLE | DEFERRED | NOT APPLICABLE | DEFERRED | DEFERRED | Multi-client duplicate-race testing was not completed. |
| 9E | Rayquaza Tower normal summon flow | PASS | PASS | PASS | PASS | DEFERRED | Normal Rayquaza summoning was reported consistent across Fabric and NeoForge. |
| 9E | Rayquaza Tower secret/alternate summon flow | PASS | PASS | PASS | PASS | DEFERRED | Secret/alternate Rayquaza summoning was reported consistent across Fabric and NeoForge. |
| 9E | Crystal Caves activation or balancing | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | Intentionally dormant; do not enable. |
| 9F | Mega Showdown 1.6.7 item IDs, tags, loot, trades, and secret checks | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred to target-modpack and player testing. |
| 9F | Mega Showdown 1.6.9 item IDs, tags, loot, trades, and secret checks | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred beyond startup-level dependency validation. |
| 9F | Blue Orb, Red Orb, Deoxys Meteorite, blank stones/Z-Crystals | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9F | Mega trader/loot items, special pools, and Zygarde tower items | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9G | Vanilla chest, igloo, cartographer, and entity loot injection | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9G | Repeated `/reload` does not duplicate or invalidate loot injection | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9G | Cobblemon Pokémon loot: direct player defeat | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9G | Cobblemon Pokémon loot: battle and multiplayer attribution | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Verify the correct player, not merely the first battle player, when resumed. |
| 9G | Cobblemon Pokémon loot: non-player defeat and duplicate prevention | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9H | Natural wandering trader replacement and persistent one-roll tag | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9H | Natural trader llama behavior and no restart/replacement loop | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9H | Static/custom trader spawn, marker cleanup, profile, and persistence | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9H | Representative basic, Cobblemon, map, Mega, joke, weighted pools | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9H | Cartographer additions, vanilla retention, and no reload duplication | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9I | Active maps create and locate the correct structure target | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9I | Map survives restart and copied-world cross-loader opening | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9J | Every enabled structure generates with matching biome/height/terrain | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9J | Jigsaw pools, internal loot, markers, and new chunks after reload/restart | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9K | Objectives, teams, load/tick functions, and scheduled functions | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9K | Several `/reload` cycles without duplicate messages/entities/tasks | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9L | Old Fabric-only world copy | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | Deferred; always test a copy rather than the original world. |
| 9L | Current split Fabric world copy opened on Fabric | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | Deferred. |
| 9L | Current NeoForge world copy reopened on NeoForge | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | Deferred. |
| 9L | Current Fabric world copy opened on NeoForge | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | Deferred. |
| 9L | Controller NBT and saved-data bounds hardening | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | Review maximum clamps before opening hand-edited or corrupt copies. |
| 9M | Fabric development environment with Mega Showdown 1.6.9 | DEFERRED | DEFERRED | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | Startup-level validation occurred; full feature matrix deferred. |
| 9M | Fabric target modpack with Mega Showdown 1.6.7 | DEFERRED | DEFERRED | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | Deferred. |
| 9M | NeoForge development environment with Mega Showdown 1.6.9 | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | DEFERRED | DEFERRED | Startup-level validation occurred; full feature matrix deferred. |
| 9M | NeoForge target modpack with Mega Showdown 1.6.7 | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | DEFERRED | DEFERRED | Deferred. |
| 9N | 20–30 minute Fabric dedicated-server performance/leak run | NOT APPLICABLE | DEFERRED | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | Deferred. |
| 9N | 20–30 minute NeoForge dedicated-server performance/leak run | NOT APPLICABLE | NOT APPLICABLE | NOT APPLICABLE | DEFERRED | DEFERRED | Deferred. |
| 9N | Repeated connect/disconnect, reload, area traversal, and two-player cases | NOT APPLICABLE | DEFERRED | NOT APPLICABLE | DEFERRED | DEFERRED | Deferred. |

## Evidence summary

Validation was performed manually and reported during development. Exact per-session
logs and screenshots were not retained for every matrix row.

```text
Date: 2026-07-14
Branch commit: 39724ef2fbba6e556454c80e2f31ccbc6b8923f4
Loaders: Fabric and NeoForge
Environments: Development clients, fresh singleplayer worlds, local dedicated servers
Result: Release-candidate smoke validation passed with extended testing deferred
Known blocking failures: None reported
```

## Failure record

Use this template for any issue found during release-candidate or player testing:

```text
Failure ID:
Matrix row:
Classification:
- shared
- Fabric-only
- NeoForge-only
- dependency-specific
- data-specific
- multiplayer-only

Exact reproduction:
Expected:
Actual:
Relevant log excerpt:
Suspected owner:
Changed files:
Retest Fabric SP:
Retest Fabric dedicated:
Retest NeoForge SP:
Retest NeoForge dedicated:
Existing-world retest:
Final status:
```

## Closure decision

Phase 9 is closed for the initial release candidate with these conclusions:

- Shared, Fabric, and NeoForge development builds reached playable client states.
- Fresh worlds opened on Fabric and NeoForge.
- Local Fabric and NeoForge dedicated servers started and stopped successfully.
- Representative controller, protection, networking, editor, and persistence
  behavior worked in the tested environments.
- Normal and secret/alternate Rayquaza summoning worked across both loaders.
- No blocking cross-loader regression was reported during smoke testing.
- All unperformed extended tests are recorded as `DEFERRED`, not `PASS`.
- Crystal Caves remains intentionally dormant.
- `PHASE_09_VALIDATION_RESULTS.md` records the release-candidate conclusion.

The full exhaustive parity gate remains deferred. Deferred work may be resumed
after initial release feedback or before a later stability-focused release.

Recommended checkpoint tag:

```text
architectury-multiloader-checkpoint-phase9
```
