# Phase 8 - NeoForge Bootstrap and Runtime Validation

## Status

**In progress - Step 8D final validation and documentation.**

## Protected starting checkpoint

- Branch: `architectury-multiloader-migration`
- Phase 7 completion commit: `01edde6c0c5f4dc0fda9c3fa96d8d3a859ee34cc`
- Checkpoint tag: `architectury-multiloader-checkpoint-phase7`
- Minecraft: `1.21.1`
- Java: `21`
- NeoForge: `21.1.214`
- Cobblemon: `1.7.3+1.21.1`
- Architectury API: `13.0.8`
- Mega Showdown: `1.6.9+1.7.3+1.21.1`
- Accessories: `1.1.0-beta.52+1.21.1`
- Kotlin for Forge: `5.10.0`

## Goal

Prove that the NeoForge module starts and behaves correctly at runtime without
duplicating shared gameplay logic or changing gameplay balance, registry IDs,
packet IDs, saved-data IDs, or data/resource identifiers.

## Workflow policy

Phase 8 continues through manual file replacement and normal Gradle commands.
The PowerShell tools committed during Step 8A are optional historical tooling
and are not required by the remaining workflow.

## Current NeoForge baseline

The NeoForge module contains:

- A thin common `@Mod` entrypoint calling `LegendaryDungeons.init()`.
- A physical-client-only `@Mod` entrypoint calling
  `LegendaryDungeonsClient.init()`.
- `META-INF/neoforge.mods.toml`.
- Architectury NeoForge.
- Cobblemon NeoForge.
- Mega Showdown NeoForge.
- Accessories NeoForge.
- Kotlin for Forge.
- Explicit Endec, Endec format-adapter, and Jankson development-runtime
  libraries.
- A transformed common artifact and platform-qualified NeoForge JAR.
- A NeoForge mod-list icon declared through `logoFile = "icon.png"`.

## Step status

| Step | Purpose | Status |
|---|---|---|
| 8A | Protect Phase 7 and begin Phase 8 documentation | Complete |
| 8B | NeoForge client startup, fresh world, and controller smoke test | Complete |
| 8C | NeoForge dedicated server, connected client, networking, persistence | Complete |
| 8D | Missing-dependency check, validation record, final documentation | In progress |

## Step 8B - NeoForge client bootstrap

### Runtime findings and corrections

The NeoForge client bootstrap exposed three concrete development-runtime
problems:

1. NeoForge 21.1.214 rejected `[[features.pp_legendarydungeons]]` as a
   multi-object list. The header is now `[features.pp_legendarydungeons]`.
2. Accessories/owo-lib required the Jankson 1.x API at runtime.
   `blue.endless:jankson:1.2.3` is now an explicit
   `forgeRuntimeLibrary`.
3. Accessories also required Endec's separate Jankson format adapter.
   `io.wispforest.endec:jankson:0.1.5` is now an explicit
   `forgeRuntimeLibrary`.

The NeoForge client subsequently reached the title screen, loaded a fresh
world, registered the mod and controller blocks, and opened and saved both
controller editors without fatal mixin, registry, packet, or class-loading
errors.

A cosmetic metadata omission prevented the mod icon from appearing in the
NeoForge mod list. The NeoForge metadata now declares `logoFile = "icon.png"`,
and the existing shared icon is copied to the root of the NeoForge resources.

These corrections do not change shared gameplay code, public identifiers,
saved data, or the release artifact layout.

The previously documented GUI-scale-4 zone-screen overflow remains deferred
and does not block Phase 8.

## Step 8C - NeoForge dedicated-server result

The NeoForge dedicated server reached its ready state and accepted a connected
NeoForge development client on `localhost:25566`.

The connected-client test confirmed:

- No registry or packet mismatch.
- Parent and zone controller screens opened while connected.
- Controller settings saved through the server.
- Saved settings survived disconnect and reconnect.
- Saved settings survived a full server stop and restart.
- The server shut down cleanly after saving players, worlds, and dimensions.
- No physical-client-only class-loading failure occurred on the server.

## Step 8D - Missing dependency and finalization

Build the final NeoForge artifact:

```powershell
.\gradlew.bat :neoforge:clean :neoforge:build --no-daemon --no-parallel --max-workers=2 --console=plain
```

Inspect the final JAR and confirm it contains:

- `META-INF/neoforge.mods.toml`
- `icon.png`

It must not contain `fabric.mod.json`.

Use a disposable NeoForge 1.21.1 instance containing only the final
`cobblemon-eld-*-neoforge-mc1.21.1-cob1.7.3.jar`. Confirm NeoForge displays a
clear missing-dependency message naming required mods rather than an
unexplained class-loading crash.

After all checks pass:

1. Add and commit `PHASE_08_VALIDATION_RESULTS.md`.
2. Push and verify the validation-results commit.
3. Mark Phase 8 complete in this document.
4. Mark Phase 8 complete in `docs/migration/README.md`.
5. Update `docs/README.md` to hand off to Phase 9.
6. Commit and push the final Phase 8 documentation.

## Completion conditions

- NeoForge client reaches the title screen.
- A fresh world loads.
- Controller blocks and screens work in NeoForge singleplayer.
- NeoForge dedicated server starts.
- A NeoForge client connects.
- Controller networking and restart persistence pass.
- Required dependencies load in the development runtime.
- Missing required dependencies produce clear loader messages.
- The final NeoForge JAR contains NeoForge metadata and the mod icon.
- The final NeoForge JAR does not contain Fabric metadata.
- Concrete runtime fixes are documented and committed.
- Phase 8 validation results and final documentation are committed.
