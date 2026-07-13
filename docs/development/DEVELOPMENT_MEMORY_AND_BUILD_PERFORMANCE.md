# Development Memory and Build Performance

## Current machine and project context

The development computer has approximately 16 GB of installed RAM.

The current project uses three separate Java processes during normal development:

1. IntelliJ IDEA itself.
2. The Gradle daemon that imports and builds the project.
3. The Minecraft development client or server.

These heaps are independent. Increasing one does not increase either of the others.

## Current project allocations

### Minecraft development client

Configured in:

```text
build.gradle.kts
```

Current client run arguments:

```text
-Xms2G
-Xmx6G
```

### Gradle build process

Configured in:

```text
gradle.properties
```

Current value:

```text
org.gradle.jvmargs=-Xmx4G
```

Four gigabytes is already generous for the current single-module project. Raising it does not automatically make Java compilation faster; it mainly prevents memory pressure when Gradle, Loom, remapping, or dependency processing genuinely need more heap.

### IntelliJ IDEA

The IDE heap is configured through:

```text
Help > Change Memory Settings
```

A reasonable starting point on this 16 GB machine is:

```text
2048 MB to 3072 MB
```

Use IntelliJ's memory indicator to decide whether more is needed:

```text
Right-click status bar > Memory Indicator
```

Avoid assigning 4 GB or more to IntelliJ while also running a 6 GB Minecraft client and a Gradle daemon with up to 4 GB. Windows, Chrome, file explorers, launchers, and background services also need memory.

## What each memory setting affects

### IntelliJ heap

Helps with:

- Indexing.
- Code completion.
- Inspections.
- Large dependency models.
- Editor responsiveness.
- Plugin activity.

It usually does not make a Gradle command such as `gradlew build` compile faster.

### Gradle heap

Helps with:

- Gradle project configuration.
- Loom processing.
- Dependency resolution.
- Compilation orchestration.
- Remapping and packaging.
- Build caches and models.

This is the setting most directly related to command-line build memory.

### Minecraft heap

Helps only the launched game/server runtime. It does not speed Gradle or IntelliJ indexing.

## Recommended current settings

### IntelliJ

Set the IDE maximum heap to:

```text
2048 MB
```

Use the project for a while with the memory indicator enabled. Increase to:

```text
3072 MB
```

only when IntelliJ repeatedly approaches its maximum and becomes sluggish.

### Gradle

Keep:

```properties
org.gradle.jvmargs=-Xmx4G
```

An optional development configuration is:

```properties
org.gradle.jvmargs=-Xmx4G -Dfile.encoding=UTF-8
org.gradle.daemon=true
org.gradle.caching=true
```

Notes:

- The Gradle daemon is enabled by default, but making it explicit is harmless.
- The local build cache can speed repeated cacheable tasks.
- Do not enable the Gradle configuration cache during migration without testing Loom and all plugins.
- `org.gradle.parallel=true` has little value while the project is one module. Reconsider it after the `common`, `fabric`, and `neoforge` split.

### Minecraft

Keep the current maximum at 6 GB while IntelliJ and Gradle are open.

The codebase becoming larger does not necessarily require more Minecraft heap. Runtime memory depends more on loaded mods, worlds, textures, entities, structures, and data than on the number of Java source files.

## Build-command habits

For routine checks, prefer:

```powershell
.\gradlew.bat build
```

Use:

```powershell
.\gradlew.bat clean build
```

for migration checkpoints, release verification, or when stale outputs are suspected.

`clean build` deliberately deletes previous outputs, so it will usually take longer and prevents normal incremental reuse.

Check Gradle daemon status:

```powershell
.\gradlew.bat --status
```

Stop old daemons after changing JVM settings:

```powershell
.\gradlew.bat --stop
```

The next build starts a daemon using the new settings.

## IntelliJ compiler heap

IntelliJ also has:

```text
Settings > Build, Execution, Deployment > Compiler
> Shared build process heap size
```

That setting applies to IntelliJ's own compiler process. This project is normally built through Gradle, so changing the shared IntelliJ compiler heap is unlikely to improve `gradlew build`.

Keep IntelliJ configured to use the Gradle wrapper and Java 21:

```text
Settings > Build, Execution, Deployment > Build Tools > Gradle
```

Recommended:

- Distribution: Gradle wrapper.
- Gradle JVM: Java 21 / the project JDK.

## Will memory changes affect the released mod?

No. IDE, Gradle daemon, and development-client heap settings are development-machine settings.

They do not alter gameplay, mod logic, save data, or the memory allocated by end users unless JVM flags are deliberately packaged into a launcher or server configuration.
