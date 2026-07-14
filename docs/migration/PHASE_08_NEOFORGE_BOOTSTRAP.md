# Phase 8 - NeoForge Bootstrap and Runtime Validation

## Status

**In progress.**

## Protected starting checkpoint

- Branch: `architectury-multiloader-migration`
- Phase 7 completion commit: `01edde6c0c5f4dc0fda9c3fa96d8d3a859ee34cc`
- Required checkpoint tag: `architectury-multiloader-checkpoint-phase7`
- Minecraft: `1.21.1`
- Java: `21`
- NeoForge: `21.1.214`
- Cobblemon: `1.7.3+1.21.1`
- Architectury API: `13.0.8`
- Mega Showdown: `1.6.9+1.7.3+1.21.1`
- Accessories: `1.1.0-beta.52+1.21.1`
- Kotlin for Forge: `5.10.0`

## Goal

Prove that the already-built NeoForge artifact starts and behaves correctly in
the NeoForge runtime. Phase 8 does not duplicate shared gameplay logic and does
not change balance, registry IDs, packet IDs, saved-data IDs, or resources
unless a concrete NeoForge runtime failure requires a narrowly scoped fix.

## Existing NeoForge baseline

The NeoForge module already contains:

- A thin main `@Mod` entrypoint calling `LegendaryDungeons.init()`.
- A physical-client entrypoint calling `LegendaryDungeonsClient.init()`.
- `META-INF/neoforge.mods.toml`.
- Architectury NeoForge.
- Cobblemon NeoForge.
- Mega Showdown NeoForge.
- Accessories NeoForge.
- Kotlin for Forge.
- Explicit Endec runtime libraries required by Accessories in development.
- A transformed common artifact and platform-qualified NeoForge JAR.

Phase 7 already proved that `:neoforge:build` succeeds and that the final JAR
contains the correct shared content and NeoForge metadata.

## Step status

| Step | Purpose | Status |
|---|---|---|
| 8A | Protect Phase 7, install Phase 8 docs and validation tools | In progress |
| 8B | NeoForge preflight, client startup, fresh world, controller smoke test | Not started |
| 8C | NeoForge dedicated server, connected client, networking, persistence | Not started |
| 8D | Dependency-error check, validation record, final documentation | Not started |

## Step 8A - Start Phase 8

- Create and push `architectury-multiloader-checkpoint-phase7` at the Phase 7 completion commit.
- Add this document and the Phase 8 validation tools.
- Update the documentation index to show Phase 8 in progress.
- Do not change Java or Gradle files during this step.

## Step 8B - NeoForge client bootstrap

Run the installed preflight tool first. It verifies the module boundary,
metadata, dependency declarations, Java entrypoints, and performs a clean
common/NeoForge build.

Then run:

```powershell
.\gradlew.bat :neoforge:runClient --no-parallel --max-workers=2
```

Required client checks:

- NeoForge reaches the title screen.
- The mod appears as loaded.
- A fresh world opens.
- Parent and zone controller blocks are registered.
- Both controller screens open.
- A controller setting saves and survives leaving/reopening the world.
- No fatal mixin, registry, packet, or client-class-loading error occurs.

If startup fails, stop this phase and run the diagnostics collector. Do not
apply speculative fixes without reviewing the actual runtime log.

## Step 8C - NeoForge dedicated server

Prepare the ignored NeoForge runtime directory, start
`:neoforge:runServer`, and connect with `:neoforge:runClient` on
`localhost:25566`.

Required checks:

- Dedicated server reaches the ready state.
- Client connects without registry or packet mismatch.
- Parent and zone screens work while connected.
- A saved controller setting survives disconnect/reconnect.
- The setting survives a full server stop and restart.
- Dedicated server does not load physical-client-only classes.

## Step 8D - Dependency and finalization checks

Use a disposable NeoForge profile or test instance with the built NeoForge JAR
but intentionally omit at least one required dependency. Confirm that NeoForge
shows a clear missing-dependency message naming the missing mod rather than an
unexplained class-loading crash.

After every check passes:

- Record `PHASE_08_VALIDATION_RESULTS.md`.
- Commit and push the results.
- Finalize this document and the phase index.
- Leave cross-loader feature parity testing to Phase 9.

## Diagnostic workflow

If either NeoForge client or server fails:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
    ".\tools\migration\phase8\Collect-Phase8NeoForgeDiagnostics.ps1"
```

Upload the generated desktop ZIP before changing Java, Gradle, metadata, or
dependencies.

## Completion conditions

- NeoForge client reaches the title screen.
- A fresh world loads.
- Controller blocks and screens work in NeoForge singleplayer.
- NeoForge dedicated server starts.
- A NeoForge client connects.
- Controller networking and restart persistence pass.
- Required dependencies load in the development runtime.
- A missing required dependency produces a clear loader message.
- All concrete runtime fixes are documented and committed.
- Phase 8 validation results and final documentation are committed.
