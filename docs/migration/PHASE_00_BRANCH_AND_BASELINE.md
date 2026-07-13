# Phase 0 — Create the Migration Branch and Clean the Baseline

## Goal

Protect the existing `master` branch and establish a clean migration starting point before Architectury code conversions begin.

## Recommended branch name

```text
architectury-multiloader-migration
```

## Local project root

The project has previously been opened from:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
```

Use your actual clone location if it differs.

## Safe terminal procedure

Open the project in IntelliJ IDEA, then open the **Terminal** tab.

### 1. Confirm which branch and files are active

```powershell
git status
git branch --show-current
```

Do not proceed if `git status` reports changes you do not understand. Commit, stash, or deliberately discard them first.

### 2. Return to the protected baseline branch

```powershell
git switch master
```

### 3. Update the local baseline from GitHub

```powershell
git pull origin master
```

### 4. Create and switch to the migration branch

```powershell
git switch -c architectury-multiloader-migration
```

This creates a new local branch at the current `master` commit and immediately checks it out.

### 5. Publish the branch to GitHub

```powershell
git push -u origin architectury-multiloader-migration
```

The `-u` establishes the upstream branch, so later pushes can usually use only:

```powershell
git push
```

### 6. Verify protection

```powershell
git branch --show-current
git status
```

Expected current branch:

```text
architectury-multiloader-migration
```

On GitHub, open the repository branch selector and confirm both branches exist:

- `master`
- `architectury-multiloader-migration`

No migration edit should be committed directly to `master`.

## Alternative GitHub website method

1. Open the repository on GitHub.
2. Open the branch dropdown currently showing `master`.
3. Choose **View all branches**.
4. Choose **New branch**.
5. Enter `architectury-multiloader-migration`.
6. Select `master` as the source.
7. Create the branch.
8. In IntelliJ Terminal, run:

```powershell
git fetch origin
git switch architectury-multiloader-migration
```

The terminal-only method is usually simpler because it creates the local and remote branch in one continuous workflow.

## Baseline cleanup included with this package

### Changed file

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\resources\data\pp_legendarydungeons\tags\function\tick.json
```

### Reason

The active tick tag still referenced:

```text
pp_legendarydungeons:other/maps/voucher_check
```

but the corresponding function file no longer exists. The replacement file removes only that stale entry.

This cleanup is not an Architectury conversion. It prevents an unrelated missing-function reference from confusing migration testing.

## Phase 0 verification

Run:

```powershell
.\gradlew.bat clean build
```

Then start the client:

```powershell
.\gradlew.bat runClient
```

Check the log for missing-function errors mentioning:

```text
pp_legendarydungeons:other/maps/voucher_check
```

No such error should remain.

## Completion criteria

- Current branch is `architectury-multiloader-migration`.
- GitHub shows the new branch.
- `master` has not moved because of local migration edits.
- The stale voucher function reference is removed.
- The clean Fabric build still succeeds.
