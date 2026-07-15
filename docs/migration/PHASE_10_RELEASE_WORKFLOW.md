# Phase 10 — 1.1.0 Documentation and Release Workflow

## Status

**In progress — architecture documentation updated and version 1.1.0 prepared
for release-artifact validation.**

## Goal

Phase 10 converts the completed Architectury migration into a maintainable
development baseline and a repeatable Modrinth/CurseForge publication process.

This phase does not require a GitHub Release.

## Version baseline

```text
Mod version: 1.1.0
Minecraft: 1.21.1
Cobblemon: 1.7.3+1.21.1
Architectury API: 13.0.8
Fabric Loader: 0.17.2
NeoForge: 21.1.214
Mega Showdown development version: 1.6.9+1.7.3+1.21.1
Mega Showdown accepted metadata floor: 1.6.7
```

## Architecture documentation completed

- `docs/README.md` is the current documentation entry point.
- `docs/development/ARCHITECTURE_GUIDE.md` defines common-first module
  responsibilities and design rules.
- `docs/development/PROJECT_IDENTIFIERS_AND_BUILD.md` documents the split build
  and artifact paths.
- `docs/development/ADDING_CONTENT.md` uses common resource and code paths.
- `docs/COMPATIBILITY.md` reflects the current Cobblemon and loader baseline.
- `docs/RELEASE_CHECKLIST.md` describes separate Modrinth and CurseForge uploads.

Older migration documents remain historical and should not override the current
architecture guide.

## Architecture acceptance rules

Phase 10 establishes these expectations for future work:

1. Shared gameplay and resources go in `common`.
2. Fabric and NeoForge modules remain thin.
3. The server is authoritative for gameplay and persistence.
4. State is scoped by world, block entity, dungeon instance, player UUID, or
   entity UUID rather than global mutable fields.
5. Registration, reload, scheduling, and cleanup are idempotent.
6. Search areas, loaded dimensions, and user-provided numbers are bounded.
7. Stable IDs are preserved unless a migration exists.
8. Networking and persistence changes receive dedicated-server testing.
9. Both loaders should expose the same gameplay behavior.

## Required remaining work

### Build

Run:

```powershell
.\gradlew.bat `
    clean `
    :common:build `
    :fabric:build `
    :neoforge:build `
    --no-daemon `
    --no-parallel `
    --max-workers=2 `
    --console=plain
```

Record:

```text
Build commit:
Build result:
Build date:
```

### Artifact inspection

Expected upload files:

```text
fabric/build/libs/cobblemon-eld-1.1.0-fabric-mc1.21.1-cob1.7.3.jar
neoforge/build/libs/cobblemon-eld-1.1.0-neoforge-mc1.21.1-cob1.7.3.jar
```

Record:

```text
Fabric filename:
Fabric size:
Fabric processed metadata version:
Fabric inspection result:

NeoForge filename:
NeoForge size:
NeoForge processed metadata version:
NeoForge inspection result:
```

### Clean-instance smoke testing

Record:

```text
Fabric clean-instance result:
Fabric latest.log notes:

NeoForge clean-instance result:
NeoForge latest.log notes:
```

Minimum checks are listed in `docs/RELEASE_CHECKLIST.md`.

### Modrinth publication

Record:

```text
Project version:
Fabric file status:
NeoForge file status:
Minecraft/loader metadata checked:
Dependency selectors checked:
Publication date:
Notes:
```

### CurseForge publication

Record:

```text
Project version:
Fabric file status:
NeoForge file status:
Minecraft/loader metadata checked:
Dependency selectors checked:
Publication date:
Notes:
```

## Known deferred work

The following are not blockers unless a reproduced failure is found:

- GUI scale 4 zone-editor layout.
- Exhaustive simultaneous-player controller and summon races.
- Full multiplayer Pokémon-drop attribution.
- Fabric-to-NeoForge existing-world certification.
- Corrupt or manually edited persistence data.
- Extended performance and leak testing.
- Crystal Caves, which remains dormant.

## Completion gate

Phase 10 is complete when:

- Version `1.1.0` is present in both processed platform metadata files.
- Both remapped platform JARs build successfully.
- Both exact JARs pass a clean-instance smoke test.
- The correct Fabric and NeoForge files are uploaded separately.
- Modrinth dependency and loader metadata is verified.
- CurseForge dependency and loader metadata is verified.
- Publication results and known limitations are recorded here.
- `docs/migration/README.md` marks Phase 10 complete.
