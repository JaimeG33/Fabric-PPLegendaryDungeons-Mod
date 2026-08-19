# Patch Workflow and Troubleshooting

## Purpose

This guide defines the preferred workflow when a programming assistant or
maintainer prepares a `.patch` file for Cobblemon: Explore Legendary Dungeons.
Its purpose is to reduce avoidable first-patch failures caused by working from
an incorrect repository baseline, manually reconstructed diff context, version-
sensitive Minecraft/Cobblemon APIs, or insufficient validation.

The patch itself and the implementation it contains are separate things. A
correct Java design can still be delivered in a patch that Git cannot apply,
and a patch that applies perfectly can still fail to compile or behave
incorrectly in game. Each level therefore needs its own validation gate.

## Context to establish before creating a patch

For a substantial patch, establish the repository baseline before editing.
The most useful PowerShell commands are:

```powershell
git fetch origin
git status
git branch --show-current
git rev-parse HEAD
git rev-parse origin/$(git branch --show-current)
```

For this project the local repository is normally:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
```

The important information is not the path itself, but:

1. repository;
2. target branch;
3. exact base commit SHA;
4. whether the working tree is clean;
5. whether local HEAD matches the intended remote branch.

If the final two commit hashes differ, determine whether the patch should be
built against the local commit or the remote commit before generating it.

If `git status` reports local changes, do not silently assume they are absent.
Either:

- commit/stash those changes first; or
- provide the relevant `git diff` and deliberately generate the patch against
  the modified local state.

For small follow-up patches where the exact base is already known and has not
changed, this context does not need to be repeatedly collected. It should be
rechecked whenever new commits have been made, another branch has been checked
out, or there is uncertainty about the repository state.

## Preferred patch-generation workflow

### 1. Lock the baseline

Record the exact repository, branch, and commit SHA the patch targets.

Example:

```text
Repository: JaimeG33/PPLegendaryDungeons-Mod-multiplatform
Branch: architectury-multiloader-migration
Base SHA: <exact commit hash>
```

### 2. Read the complete affected files

Do not reconstruct a large existing file from isolated snippets when avoidable.
Read the complete current versions of files that will be modified, especially
central managers, registrars, schemas, and documentation indexes.

### 3. Make changes in a real/synthetic checkout

Prefer editing files in a checkout of the exact baseline and generating the
patch mechanically with Git:

```powershell
git diff --binary > change.patch
```

Avoid manually writing unified-diff hunk headers or surrounding context when a
mechanical diff is possible. Incorrect whitespace in unchanged context can make
a logically correct patch fail before any code is changed.

### 4. Check patch applicability

Against an untouched copy of the same baseline, run:

```powershell
git apply --check change.patch
```

This is stronger than:

```powershell
git apply --stat change.patch
git apply --summary change.patch
```

`--stat` and `--summary` prove that Git can parse the patch format; they do not
prove that the hunks match the target source files.

### 5. Check whitespace and compilation

After application:

```powershell
git diff --check
```

For shared Java changes, prefer at least:

```powershell
.\gradlew.bat `
    :common:compileJava `
    :fabric:compileJava `
    :neoforge:compileJava `
    --no-daemon `
    --no-parallel `
    --max-workers=2 `
    --console=plain
```

When practical, follow with the full platform builds.

### 6. Separate build verification from runtime verification

Successful compilation does not prove Minecraft AI, Cobblemon lifecycle hooks,
structure markers, persistence, or combat behavior work as intended. Document
what still requires an in-game test.

## Common error categories

### Patch/context mismatch

Typical message:

```text
patch failed: <file>:<line>
patch does not apply
```

Common causes:

- patch generated for another commit or branch;
- local uncommitted edits;
- manually reconstructed hunk context does not exactly match whitespace;
- stale snippets were used instead of the full current file;
- a large first patch changed too many unrelated regions at once.

Reduction strategy: lock the base SHA, use complete current files, generate the
diff mechanically, and run a real `git apply --check` against that baseline.

### Compile/API mismatch

Typical messages include `cannot find symbol`, incompatible types, missing
methods, or wrong constructor signatures.

Minecraft, Mojang mappings, Cobblemon, Architectury, Fabric, and NeoForge are
version-sensitive. Code that is valid for a nearby version may not compile for
this project's exact dependency set.

Reduction strategy: inspect the project's actual versions and existing usage
patterns, then run the relevant Gradle compile tasks.

### Loader/build mismatch

Shared code may compile while one platform module still fails because of
loader-specific metadata, mixins, dependency boundaries, or client/server
separation.

Reduction strategy: compile/build common, Fabric, and NeoForge rather than
checking only one module when the change is shared.

### Runtime behavior mismatch

The patch applies and builds, but Minecraft or Cobblemon behaves differently
than expected. AI can overwrite targets, entities can unload, battle state can
pause behavior, and events may fire at a different stage than assumed.

Reduction strategy: add a focused test matrix and test the smallest possible
scene before distributing the feature through production structures.
