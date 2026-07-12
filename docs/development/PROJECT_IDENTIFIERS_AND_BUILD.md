# Project Identifiers, Metadata, and Build

## Public name versus internal names

The following mismatch is valid and intentional:

```text
Public name:
Cobblemon: Explore Legendary Dungeons

Fabric mod ID:
pp_legendarydungeons

Java package:
porker.pp_legendarydungeons

Main class:
ProfessorPorkersLegendaryDungeons

Client class:
ProfessorPorkersLegendaryDungeonsClient
```

Fabric does not infer entrypoints from the public display name. It loads the exact fully qualified classes listed in `fabric.mod.json`. Renaming the public mod does not require renaming Java packages, classes, assets, data namespaces, scoreboards, or saved-data IDs.

Keeping the legacy identifiers avoids a large migration that could break:

- Existing worlds and saved data.
- Resource locations.
- Datapack functions and tags.
- Mixins and networking payloads.
- Other addons that reference the namespace.
- Existing structure templates and loot tables.

## Author pseudonym

This is valid:

```json
"authors": [
  "Professor Porker"
]
```

The authors field is display metadata. It does not need to match the GitHub username, Java package, or legal name.

## Version

The release version comes from:

```text
build.gradle.kts
```

Recommended:

```kotlin
version = "1.0.0"
```

`processResources` expands this value into:

```json
"version": "${version}"
```

inside `fabric.mod.json`.

## Recommended artifact name

Use:

```text
cobblemon-eld-1.0.0-mc1.21.1-cob1.6.1-1.7.3.jar
```

Meaning:

```text
cobblemon-eld  = Cobblemon: Explore Legendary Dungeons
1.0.0          = mod release version
mc1.21.1       = Minecraft version
cob1.6.1-1.7.3 = tested Cobblemon endpoints
```

The safest release workflow is:

1. Keep the internal Gradle version as `1.0.0`.
2. Run the normal clean build.
3. Take the remapped non-sources JAR.
4. Rename that copy to the public artifact name.

The JAR filename is not the Fabric mod ID and is not used to locate entrypoint classes.

## Root project name

The current setting:

```kotlin
rootProject.name = "cobblemon-explore-legendary-dungeons"
```

is valid. It only contributes to Gradle/IDE naming and the default archive name.

Changing it to `cobblemon-eld` would be reasonable, but it also changes generated task/archive naming and Loom’s default refmap name. Because the current release already works, manually renaming the final artifact is the lower-risk 1.0.0 choice.

Remove the stale TODO comment in `settings.gradle.kts`, but the project name itself can stay.

## Stable namespace rule

Do not rename for 1.0.0:

```text
pp_legendarydungeons
porker.pp_legendarydungeons
ProfessorPorkersLegendaryDungeons
ProfessorPorkersLegendaryDungeonsClient
```

A future internal rename should be treated as a separate migration release with aliases or data-fixer planning.
