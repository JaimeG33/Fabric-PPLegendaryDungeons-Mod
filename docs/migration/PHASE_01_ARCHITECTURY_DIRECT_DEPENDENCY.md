# Phase 1 — Make Architectury API a Direct Dependency

## Goal

Make Architectury API a direct compile and runtime dependency of Explore Legendary Dungeons while the project is still a single-module Fabric mod.

This phase intentionally does **not** migrate lifecycle events, ticks, trades, loot, networking, registries, or interactions yet. It establishes the dependency and metadata foundation needed for later phases.

## Why this change is required

Before Phase 1, Architectury Fabric was declared as:

```kotlin
modRuntimeOnly("dev.architectury:architectury-fabric:13.0.8")
```

That made Architectury available when the development game launched because Mega Showdown requires it, but it did not place Architectury API on this project's compile classpath.

Later phases will import classes such as:

```text
dev.architectury.event.events.common.LifecycleEvent
dev.architectury.event.events.common.TickEvent
dev.architectury.event.events.common.InteractionEvent
dev.architectury.event.events.common.BlockEvent
dev.architectury.event.events.common.LootEvent
dev.architectury.registry.ReloadListenerRegistry
dev.architectury.registry.level.entity.trade.TradeRegistry
dev.architectury.registry.registries.DeferredRegister
dev.architectury.networking.NetworkManager
```

Therefore Architectury must be declared with `modImplementation`.

## Files changed in Phase 1

### 1. Gradle dependency configuration

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\build.gradle.kts
```

Changes:

- Keeps the existing Architectury Loom and Gradle plugins.
- Keeps the Architectury Maven repository.
- Changes Architectury Fabric from `modRuntimeOnly` to `modImplementation`.
- Separates the Architectury dependency from the Mega Showdown runtime-only comment.
- Adds comments explaining the temporary continued use of direct Fabric APIs.

### 2. Fabric dependency metadata

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\resources\fabric.mod.json
```

Change:

```json
"architectury": ">=13.0.8"
```

This declaration is direct and intentional even though Mega Showdown also requires Architectury. The mod itself will use Architectury APIs, so its dependency must not be hidden behind another mod.

### 3. Migration documentation

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\README.md
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\MULTILOADER_MIGRATION_MASTER_PLAN.md
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\README.md
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\PHASE_00_BRANCH_AND_BASELINE.md
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\PHASE_01_ARCHITECTURY_DIRECT_DEPENDENCY.md
```

## Copy procedure

The downloadable package contains:

```text
COPY_INTO_PROJECT/
```

Copy the **contents** of that directory into:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
```

Allow Windows to merge directories and replace the listed existing files.

Do not copy the outer package directory into `src` or `docs`. The file `COPY_INTO_PROJECT\build.gradle.kts` must end up directly in the repository root.

## IntelliJ steps after copying

### 1. Confirm the correct branch

```powershell
git branch --show-current
```

Expected:

```text
architectury-multiloader-migration
```

### 2. Inspect the changed files

```powershell
git status --short
```

Expected paths include:

```text
M build.gradle.kts
M docs/README.md
M src/main/resources/fabric.mod.json
M src/main/resources/data/pp_legendarydungeons/tags/function/tick.json
?? docs/MULTILOADER_MIGRATION_MASTER_PLAN.md
?? docs/migration/
```

### 3. Reload Gradle

In IntelliJ, use one of these:

- Click the Gradle refresh/reload icon.
- Open the Gradle tool window and choose **Reload All Gradle Projects**.
- Accept IntelliJ's prompt to load changed Gradle files.

Do not manually add an Architectury JAR to IntelliJ libraries. Gradle controls the dependency.

### 4. Verify the dependency resolves

Run:

```powershell
.\gradlew.bat dependencies --configuration modImplementation
```

Depending on Loom's configuration output, Architectury may appear directly or through a derived configuration. The more important tests are compilation and runtime startup.

### 5. Clean build

```powershell
.\gradlew.bat clean build
```

Expected result:

```text
BUILD SUCCESSFUL
```

### 6. Launch the development client

```powershell
.\gradlew.bat runClient
```

At the title screen, verify the Mods list includes:

- Cobblemon
- Mega Showdown
- Architectury API
- Accessories
- Explore Legendary Dungeons

Load a test world and verify there is no immediate startup error from duplicate Architectury loading. Gradle and the loader resolve one compatible Architectury mod; declaring it directly does not create a second physical copy in the final mod JAR.

### 7. Dedicated server smoke test

Use the existing IntelliJ server run configuration or the available Gradle server run task.

Confirm:

- Server starts.
- Explore Legendary Dungeons initializes.
- Architectury dependency is satisfied.
- No client-only class-loading error occurs.
- No missing voucher function error occurs.

## What should not change in this phase

The following remain Fabric-specific until later phases:

- `ProfessorPorkersLegendaryDungeons implements ModInitializer`
- `ProfessorPorkersLegendaryDungeonsClient implements ClientModInitializer`
- Fabric lifecycle events
- Fabric tick events
- Fabric interaction callbacks
- Fabric payload networking
- Fabric resource reload registration
- Fabric trade injection
- Fabric loot-table injection
- Current registry implementation

No Architectury API imports need to be added to Java source during Phase 1.

## Suggested commit

After successful testing:

```powershell
git add build.gradle.kts
git add src/main/resources/fabric.mod.json
git add src/main/resources/data/pp_legendarydungeons/tags/function/tick.json
git add docs
git commit -m "Add Architectury API as direct dependency"
git push
```

## Rollback

Because the work is on a separate branch, the protected Fabric baseline remains on `master`.

To discard all uncommitted Phase 1 edits:

```powershell
git restore .
git clean -fd
```

Use `git clean -fd` carefully because it deletes all untracked files in the repository, including the newly added migration documents.

To return temporarily to the protected branch:

```powershell
git switch master
```

To resume migration work:

```powershell
git switch architectury-multiloader-migration
```

## Completion criteria

- Architectury Fabric is `modImplementation`.
- `fabric.mod.json` directly requires Architectury 13.0.8 or newer.
- Gradle sync succeeds.
- Clean build succeeds.
- Fabric client and dedicated server start.
- No gameplay registration has been changed yet.
- Phase documentation exists in the repository.
- Changes are committed and pushed only to the migration branch.
