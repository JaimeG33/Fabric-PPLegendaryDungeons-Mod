# Phase 7E Validation Checklist

## Status

**In progress.**

## Automated validation

Run from the project root after installing the Phase 7E tools:

```powershell
powershell -ExecutionPolicy Bypass -File `
    ".\tools\migration\phase7\Run-Phase7StaticAudits.ps1"

powershell -ExecutionPolicy Bypass -File `
    ".\tools\migration\phase7\Build-Phase7Artifacts.ps1"

powershell -ExecutionPolicy Bypass -File `
    ".\tools\migration\phase7\Inspect-Phase7Artifacts.ps1"
```

The build intentionally uses:

- No dependency refresh.
- No parallel project execution.
- Two Gradle workers.
- A fresh build of `common`, `fabric`, and `neoforge`.

NeoForge runtime launch is not part of Phase 7. Phase 7 requires the NeoForge
module to compile and its final JAR to pass metadata/content inspection.

## Fabric integrated-client check

Run:

```powershell
.\gradlew.bat :fabric:runClient --no-parallel --max-workers=2
```

Verify:

- The title screen loads.
- A world opens.
- Parent and zone controller blocks are registered.
- Both editor screens open.
- Editing and saving a controller still works.

## Fabric dedicated-server and connected-client check

Accept the EULA and prepare the ignored validation runtime directory:

```powershell
powershell -ExecutionPolicy Bypass -File `
    ".\tools\migration\phase7\Prepare-Phase7FabricServer.ps1" `
    -AcceptEula
```

In PowerShell window A:

```powershell
.\gradlew.bat :fabric:runServer --no-parallel --max-workers=2
```

After the server reports that startup is complete, launch the Fabric client
from IntelliJ or PowerShell in window B:

```powershell
.\gradlew.bat :fabric:runClient --no-parallel --max-workers=2
```

Connect to `localhost`.

Verify:

- The client joins without packet-registration or registry errors.
- Parent and zone editors open on the dedicated server.
- Changes save and are reflected server-side.
- Disconnect and reconnect.
- Stop the server with `stop`, restart it, reconnect, and confirm the saved
  controller state persists.

## Record passing results

After every automated and manual check passes:

```powershell
powershell -ExecutionPolicy Bypass -File `
    ".\tools\migration\phase7\Record-Phase7ValidationResults.ps1" `
    -IntegratedClientPass `
    -DedicatedServerPass `
    -ConnectedClientNetworkingPass `
    -RestartPersistencePass
```

This creates:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\PHASE_07_VALIDATION_RESULTS.md
```

Commit and push that result before applying the final Phase 7 documentation step.
