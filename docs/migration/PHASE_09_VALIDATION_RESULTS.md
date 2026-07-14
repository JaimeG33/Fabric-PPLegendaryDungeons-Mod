# Phase 9 Validation Results

## Result

**Release-candidate smoke validation passed with extended parity and hardening
tests deferred.**

## Validation baseline

| Item | Value |
|---|---|
| Branch | `architectury-multiloader-migration` |
| Validated commit | `39724ef2fbba6e556454c80e2f31ccbc6b8923f4` |
| Validation date | `2026-07-14` |
| Minecraft | `1.21.1` |
| Java | `21` |
| Cobblemon | `1.7.3+1.21.1` |
| Fabric Loader | `0.17.2` |
| NeoForge | `21.1.214` |
| Architectury API | `13.0.8` |
| Development Mega Showdown | `1.6.9+1.7.3+1.21.1` |

## Completed validation

The following behavior was manually tested and reported as working:

- Fabric development client startup.
- NeoForge development client startup.
- Fabric Mod Menu visibility and Fabric metadata icon.
- NeoForge mod-list metadata and icon.
- Fresh singleplayer world startup on both loaders.
- Local Fabric dedicated-server startup and clean shutdown.
- Local NeoForge dedicated-server startup and clean shutdown.
- Representative parent and zone controller behavior.
- Representative linking, persistence, priority, protection, editor, and
  restart behavior.
- Representative Cobblemon utility and protected interaction behavior.
- Normal Rayquaza summon flow on Fabric and NeoForge.
- Secret/alternate Rayquaza summon flow on Fabric and NeoForge.
- Representative repeat-prevention behavior during Rayquaza testing.

No blocking cross-loader failure was reported during these smoke tests.

## Explicitly deferred validation

The following were not completed exhaustively and must not be represented as
passed:

- Every controller editor field, invalid value, and numeric boundary.
- Adversarial packet permission, distance, stale-controller, and block-type tests.
- Two-player simultaneous controller edits or summon race conditions.
- Generic static-feature lifecycle coverage outside the tested Rayquaza path.
- Mega Showdown `1.6.7` compatibility-floor feature validation.
- Complete Mega Showdown `1.6.9` item, loot, trade, tag, and secret checks.
- Exhaustive vanilla and Cobblemon loot injection testing.
- Multiplayer Pokémon-drop attribution and duplicate prevention.
- Natural and static wandering-trader lifecycle coverage.
- Cartographer trade and structure-map coverage.
- Every enabled structure, jigsaw pool, marker, and internal loot path.
- Repeated `/reload` idempotency testing.
- Old-world, same-loader, and Fabric-to-NeoForge copied-world migration.
- Corrupt or hand-edited controller NBT bounds testing.
- Full target-modpack combinations.
- Extended dedicated-server performance, leak, reconnect, and traversal sessions.
- GUI scale 4 zone-editor overflow.
- Crystal Caves activation or balancing, which remains not applicable.

## Evidence limitations

Testing was performed manually during development. Complete logs, screenshots,
world copies, and exact per-row reproduction records were not retained for every
successful smoke test. The result therefore records a practical initial-release
validation, not exhaustive certification.

## Release decision

The Architectury migration is suitable to proceed as an initial release
candidate based on the completed Fabric and NeoForge smoke testing.

Deferred items should be revisited when:

- A player reports a reproducible issue.
- A target modpack changes dependency versions.
- Existing worlds are prepared for loader migration.
- A later release focuses on multiplayer, persistence hardening, or performance.

All deferred scenarios remain listed in
`PHASE_09_CROSS_LOADER_PARITY.md`.

## Recommended checkpoint

After reviewing and committing these documentation updates:

```text
architectury-multiloader-checkpoint-phase9
```
