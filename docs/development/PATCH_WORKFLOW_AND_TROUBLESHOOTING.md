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

#### Do not treat an excerpt as a complete file

A ranged fetch, search result, truncated tool response, or copied excerpt is not
a valid replacement for the complete source file when generating or validating
a patch. In particular, do not write a mid-file excerpt into a synthetic
checkout starting at line 1 and then use that same synthetic file to prove that
`git apply --check` succeeds. That only proves the patch matches the synthetic
excerpt, not the real repository file.

If the complete file cannot be retrieved in one response:

- fetch line-preserving ranges from the exact base commit;
- preserve their real source positions when generating localized hunks;
- use enough unchanged context around every edited region to identify it
  uniquely;
- for existing files, prefer unchanged context on both sides of a replacement
  hunk (normally at least three lines when practical) instead of ending the
  hunk immediately after the changed paragraph;
- confirm the file's actual beginning, blob SHA, or another independent marker
  so an excerpt is not accidentally mistaken for the whole file; and
- whenever possible, validate the final patch against a baseline reconstructed
  independently from the data used to generate the diff.

Documentation files need the same rigor as Java files. Large Markdown documents
are especially easy to mishandle because a section heading returned by a tool
can look like the beginning of a standalone document even when it is hundreds
of lines into the real file.

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
- a mid-file excerpt was treated as though it began at line 1;
- patch validation reused the same incomplete synthetic excerpt that generated
  the bad diff, creating a false-positive `git apply --check`;
- a hunk header's declared line counts do not match the actual added/removed
  lines, which can leave intended trailing patch text unapplied;
- a large first patch changed too many unrelated regions at once.

Reduction strategy: lock the base SHA, use complete current files or
line-preserving ranges, generate the diff mechanically, verify hunk counts, and
run `git apply --check` against a baseline reconstructed independently from the
patch-generation excerpts whenever possible.

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

Command-driven tests also need lifecycle scrutiny. For example, `/summon` can
read custom mob NBT before the command finishes applying the entity's final
coordinates. A fallback based on `mob.blockPosition()` during NBT load can
therefore capture `(0, 0, 0)` even though the mob later appears at `~ ~ ~`.

Reduction strategy: add a focused test matrix and test the smallest possible
scene before distributing the feature through production structures. When code
depends on entity position, UUID, owner, dimension, or other construction-time
state, verify **when** that value becomes final rather than assuming the NBT
read hook is late enough.

### Data/resource mismatch

JSON may be syntactically valid but use an invalid resource ID, unsupported
field, missing referenced profile, wrong datapack directory, or incorrect
reload assumption.

Reduction strategy: validate each resource independently, test `/reload`, and
ensure one malformed datapack file does not invalidate unrelated definitions.

## Verification labels for future patch handoffs

When practical, patch handoffs should state which gates were actually checked:

```text
[x] exact repository/branch/base SHA identified
[x] complete affected files or line-preserving ranges reviewed
[x] patch syntax/hunk counts parsed
[x] git apply --check against an independent exact touched baseline
[ ] Java compilation
[ ] Fabric build
[ ] NeoForge build
[ ] in-game/runtime test
```

Do not describe a syntax/stat check, or a check against the same incomplete
snippet used to generate the patch, as proof that a patch applies to the real
repository. If a tool/environment prevents one of the checks, state that
limitation explicitly so the next local test is clear.

## Scope strategy for large first patches

Large architectural changes are more reliable when internally staged even if
they are ultimately delivered as one patch. For example:

```text
data model / reload layer
profile/schema integration
manager behavior
built-in data
documentation and tests
```

Validate each layer before stacking the next one. Follow-up fixes are often
more reliable because a compiler/runtime error narrows the problem; the goal of
this workflow is to give first patches that same level of grounding before
delivery.
