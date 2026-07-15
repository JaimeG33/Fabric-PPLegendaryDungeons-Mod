# Project Identifiers, Metadata, and Build

## Stable public and internal identifiers

The public display name and internal identifiers intentionally differ:

```text
Public name:
Cobblemon: Explore Legendary Dungeons

Mod ID and data namespace:
pp_legendarydungeons

Java package:
porker.pp_legendarydungeons
```

Primary entrypoints:

```text
Shared main initializer:
porker.pp_legendarydungeons.LegendaryDungeons

Shared client initializer:
porker.pp_legendarydungeons.LegendaryDungeonsClient

Fabric main:
porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons

Fabric client:
porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeonsClient

NeoForge main:
porker.pp_legendarydungeons.neoforge.ProfessorPorkersLegendaryDungeonsNeoForge

NeoForge client:
porker.pp_legendarydungeons.neoforge.ProfessorPorkersLegendaryDungeonsNeoForgeClient
```

These names are compatibility surfaces. Renaming them can break existing
worlds, resource locations, functions, tags, mixins, packets, structures, loot
tables, or integrations.

## Author metadata

The release author is displayed as:

```text
Professor Porker
```

This does not need to match the repository username, Java package, or legal
name.

## Version source

The release version comes from the root:

```text
gradle.properties
```

Current value:

```properties
mod_version=1.1.0
```

The root Gradle build assigns this project version to every module. Each
platform’s `processResources` task expands it into:

```text
fabric/src/main/resources/fabric.mod.json
neoforge/src/main/resources/META-INF/neoforge.mods.toml
```

The source metadata correctly contains `${version}`. The completed release JAR
must contain `1.1.0` after resource processing.

## Module layout

```text
common/
fabric/
neoforge/
```

Use `common` for shared code and resources. Use the loader modules only for
entrypoints, metadata, dependencies, run configuration, and unavoidable
platform adapters.

See:

```text
docs/development/ARCHITECTURE_GUIDE.md
```

## Root project name

The Gradle root project name is:

```kotlin
rootProject.name = "cobblemon-explore-legendary-dungeons"
```

It is an IDE and Gradle project label. It is not the mod ID and does not need to
match the public artifact filename.

## Build command

From the repository root:

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

Require:

```text
BUILD SUCCESSFUL
```

## Release artifacts

Fabric:

```text
fabric/build/libs/cobblemon-eld-1.1.0-fabric-mc1.21.1-cob1.7.3.jar
```

NeoForge:

```text
neoforge/build/libs/cobblemon-eld-1.1.0-neoforge-mc1.21.1-cob1.7.3.jar
```

Do not upload:

```text
common/build/libs/*
*-sources.jar
*-dev-slim.jar
*-dev-shadow.jar
```

The two release JARs are uploaded separately to Modrinth and CurseForge. The
project does not require a GitHub Release.

## Metadata inspection

The Fabric JAR must include:

```text
fabric.mod.json
icon.png
pp_legendarydungeons.mixins.json
data/pp_legendarydungeons/
porker/pp_legendarydungeons/
```

The NeoForge JAR must include:

```text
META-INF/neoforge.mods.toml
icon.png
pp_legendarydungeons.mixins.json
data/pp_legendarydungeons/
porker/pp_legendarydungeons/
```

The NeoForge JAR should not include `fabric.mod.json`.

Verify the processed version in both metadata files before uploading.

## Versioning guidance

Use semantic versioning as a project convention:

- Patch, such as `1.1.1`: fixes without intended feature or compatibility
  changes.
- Minor, such as `1.2.0`: backward-compatible features or substantial content.
- Major, such as `2.0.0`: intentionally incompatible behavior or data changes.

A version change should be made before the final clean build so both loader
artifacts receive the same version.
