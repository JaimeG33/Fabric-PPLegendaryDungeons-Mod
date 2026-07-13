# Phase 3 — Architectury Interaction and Block Protection Events

## Status

**Ready to apply and verify.**

Phase 2 was reported as fully tested in-game and pushed to:

```text
architectury-multiloader-migration
```

The branch is ahead of `master` and not behind it.

---

## 1. Goal

Move the remaining dungeon right-click restrictions and player block break/place restrictions from direct Fabric callbacks/custom mixins to Architectury common events.

This phase covers:

- Dungeon controller block right-click handling.
- Portable PC restrictions.
- Portable healer restrictions.
- Bed-use restrictions.
- Placed PC restrictions.
- Placed healer restrictions.
- Block-breaking restrictions.
- Block-placement restrictions.
- Allowed-break and allowed-place tags.
- Removal of the direct block break and placement mixins.

This phase does not migrate networking itself. The shared interaction event still calls the current `DungeonRuleNetworking.openEditor(...)` method. That call becomes fully loader-neutral in Phase 5.

---

## 2. API replacements

| Old boundary | Phase 3 boundary |
|---|---|
| Fabric `UseBlockCallback` | Architectury `InteractionEvent.RIGHT_CLICK_BLOCK` |
| Fabric `UseItemCallback` | Architectury `InteractionEvent.RIGHT_CLICK_ITEM` |
| Custom `ServerPlayerGameMode.destroyBlock` mixin | Architectury `BlockEvent.BREAK` |
| Custom `BlockItem.place` mixin | Architectury `BlockEvent.PLACE` |

Architectury event results map as follows:

| Desired behavior | Result |
|---|---|
| Allow vanilla and later listeners | `EventResult.pass()` |
| Consume a successful controller click | `EventResult.interruptTrue()` |
| Deny an interaction/break/place | `EventResult.interruptFalse()` |
| Deny item use and return the unchanged stack | `CompoundEventResult.interruptFalse(stack)` |

---

## 3. Files added

### Shared block events

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\events\DungeonRuleBlockEvents.java
```

This class registers:

```java
BlockEvent.BREAK
BlockEvent.PLACE
```

The class is idempotent and can only register once.

### Focused phase document

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\PHASE_03_INTERACTION_AND_PROTECTION_EVENTS.md
```

### Development memory guide

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\development\DEVELOPMENT_MEMORY_AND_BUILD_PERFORMANCE.md
```

This is documentation only and does not change runtime or build settings.

---

## 4. Files replaced

### Shared initializer

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\LegendaryDungeons.java
```

New shared registrations:

```java
DungeonRuleInteractionEvents.register();
DungeonRuleBlockEvents.register();
```

### Fabric entrypoint

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\ProfessorPorkersLegendaryDungeons.java
```

The entrypoint no longer registers interaction callbacks. Only Fabric networking remains.

### Interaction events

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\events\DungeonRuleInteractionEvents.java
```

Direct Fabric callback imports are removed.

### Mixin configuration

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\resources\pp_legendarydungeons.mixins.json
```

Removed entries:

```text
dungeon_rules.DungeonBlockBreakMixin
dungeon_rules.DungeonBlockPlacementMixin
```

Retained entries:

```text
dungeon_rules.DungeonExplosionMixin
trades.NaturalWanderingTraderMixin
```

### Documentation indexes

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\README.md
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\README.md
```

---

## 5. Files that must be deleted

Copying files cannot delete obsolete Java sources. Remove these two files after copying:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\mixin\dungeon_rules\DungeonBlockBreakMixin.java

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\mixin\dungeon_rules\DungeonBlockPlacementMixin.java
```

PowerShell:

```powershell
Remove-Item ".\src\main\java\porker\pp_legendarydungeons\mixin\dungeon_rules\DungeonBlockBreakMixin.java"
Remove-Item ".\src\main\java\porker\pp_legendarydungeons\mixin\dungeon_rules\DungeonBlockPlacementMixin.java"
```

The package also includes `APPLY_PHASE_3.ps1`, which performs the copy and deletion together.

---

## 6. Behavior preserved

### Builder bypass

A player bypasses restrictions only when both are true:

- Creative mode.
- Permission level 2 or higher.

### Block-breaking allowlist

The tag remains:

```text
data/pp_legendarydungeons/tags/block/dungeon_allowed_breaks.json
```

Allowed blocks bypass only this mod's dungeon restriction. Vanilla tool, adventure-mode, spawn-protection, and other mod restrictions still apply.

### Block-placement allowlist

The tag remains:

```text
data/pp_legendarydungeons/tags/block/dungeon_allowed_places.json
```

The existing tag already contains both standing and wall torch states:

- `minecraft:torch`
- `minecraft:wall_torch`
- `minecraft:soul_torch`
- `minecraft:soul_wall_torch`

### Controller blocks

Right-clicking a dungeon parent or zone block remains consumed.

- Creative operators open the editor.
- Other players do not open it.
- The server remains authoritative.

### Utility restrictions

The same rule locations and messages are retained:

- Portable PC/healer checks use the player's block position.
- Bed/PC/healer block checks use the clicked block position.
- Placement checks use the actual future placed block position.

---

## 7. Intentional precision improvement

The old placement mixin checked:

- The `BlockItem`'s primary/default block.
- The originally clicked position.

Architectury supplies:

- The actual future `BlockState`.
- The actual placement position.

This improves wall/floor variant handling and boundary accuracy. Because the existing allowed-place tag already includes both torch variants, the intended torch behavior should remain unchanged.

This precision improvement requires focused boundary tests.

---

## 8. Copy procedure

### Manual method

Copy the contents of:

```text
COPY_INTO_PROJECT
```

into:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
```

Allow Windows to merge folders and replace files.

Then delete the two obsolete mixin classes listed above.

### Script method

From the extracted package directory:

```powershell
powershell -ExecutionPolicy Bypass -File ".\APPLY_PHASE_3.ps1"
```

The script defaults to:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
```

To use another clone location:

```powershell
powershell -ExecutionPolicy Bypass -File ".\APPLY_PHASE_3.ps1" -ProjectRoot "D:\Your\Other\Path"
```

---

## 9. Pre-build inspection

Confirm the branch:

```powershell
git branch --show-current
```

Expected:

```text
architectury-multiloader-migration
```

Inspect changes:

```powershell
git status --short
```

Expected Java/config changes include:

```text
M src/main/java/porker/pp_legendarydungeons/LegendaryDungeons.java
M src/main/java/porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeons.java
M src/main/java/porker/pp_legendarydungeons/dungeon_rules/events/DungeonRuleInteractionEvents.java
A src/main/java/porker/pp_legendarydungeons/dungeon_rules/events/DungeonRuleBlockEvents.java
D src/main/java/porker/pp_legendarydungeons/mixin/dungeon_rules/DungeonBlockBreakMixin.java
D src/main/java/porker/pp_legendarydungeons/mixin/dungeon_rules/DungeonBlockPlacementMixin.java
M src/main/resources/pp_legendarydungeons.mixins.json
```

Search for removed direct boundaries:

```powershell
Get-ChildItem -Recurse ".\src\main\java" -Filter "*.java" |
    Select-String "UseBlockCallback|UseItemCallback|DungeonBlockBreakMixin|DungeonBlockPlacementMixin"
```

Expected: no active Java source matches.

---

## 10. Build verification

Run:

```powershell
.\gradlew.bat clean build
```

Expected:

```text
BUILD SUCCESSFUL
```

A compile failure in this phase is most likely to involve:

- An Architectury event signature.
- A missed obsolete mixin reference.
- The interaction result generic type.
- A file not copied to the correct path.

Do not commit until the clean build succeeds.

---

## 11. Client and server startup

Client:

```powershell
.\gradlew.bat runClient
```

Dedicated server:

```powershell
.\gradlew.bat runServer
```

Verify:

- No mixin error mentions either deleted mixin.
- The explosion mixin still loads.
- The natural wandering trader mixin still loads.
- Shared events register once.
- No client-only class loading occurs on the dedicated server.

---

## 12. Focused test matrix

### A. Controller editor

As a creative operator:

- Right-click a parent rule block.
- Right-click a zone rule block.
- Confirm the correct editor opens.

As a non-operator/non-creative player:

- Right-click both controller block types.
- Confirm no editor opens.
- Confirm no unrelated vanilla interaction occurs.

### B. Portable PC

With `PORTABLE_PC_USE` enabled in an active dungeon:

- Right-click air with a tagged portable PC.
- Right-click a normal block with the portable PC.
- Confirm both actions are denied.
- Confirm only one feedback message appears per attempt.

Outside the dungeon or with the rule disabled:

- Confirm the portable PC works normally.

Repeat as a creative operator and confirm bypass.

### C. Portable healer

Repeat the portable PC matrix with a tagged portable healer.

### D. Beds

Inside an active dungeon with `BED_USE` enabled:

- Use the head half.
- Use the foot half.
- Test day and night where practical.
- Confirm use is denied.

Outside or with the rule disabled:

- Confirm normal bed behavior.

### E. Placed PC and healer blocks

For each configured block tag:

- Test inside an active dungeon.
- Test outside.
- Test with the rule disabled.
- Test creative-operator bypass.

### F. Breaking

Inside an active dungeon with `BLOCK_BREAKING` enabled:

- Break a normal structure block: denied.
- Break a block in `dungeon_allowed_breaks`: allowed.
- Test creative non-operator: denied.
- Test creative operator: allowed.
- Test a boundary block exactly inside and exactly outside the zone.
- Confirm only one denial message appears.

### G. Placement

Inside an active dungeon with `BLOCK_PLACEMENT` enabled:

- Place a normal full block: denied.
- Place a floor torch: allowed.
- Place a wall torch: allowed.
- Place a soul floor torch: allowed.
- Place a soul wall torch: allowed.
- Place a door: denied without leaving half a door.
- Place a bed: denied without leaving half a bed.
- Place a waterlogged or directional block: denied cleanly.
- Test a boundary position where the clicked block is outside but the placed block is inside.
- Test a boundary position where the clicked block is inside but the placed block is outside.
- Test creative non-operator and creative-operator behavior.
- Confirm only one denial message appears.

### H. Non-player placement

Where practical, test a dispenser or another non-player mechanism that places a block. Phase 3 intentionally preserves the old behavior: only player placement is restricted by this rule.

### I. Explosion regression

Trigger an explosion in/near a protected dungeon and confirm the retained explosion mixin still behaves as before.

### J. Wandering trader regression

Trigger or locate a natural wandering trader and confirm the retained replacement mixin still behaves as before.

---

## 13. Commit instructions

After all tests pass:

```powershell
git add src/main/java/porker/pp_legendarydungeons/LegendaryDungeons.java
git add src/main/java/porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeons.java
git add src/main/java/porker/pp_legendarydungeons/dungeon_rules/events
git add src/main/java/porker/pp_legendarydungeons/mixin/dungeon_rules
git add src/main/resources/pp_legendarydungeons.mixins.json
git add docs

git status
git diff --cached
git commit -m "Migrate dungeon protection events to Architectury"
git push
```

---

## 14. IntelliJ local-state cleanup

The branch comparison still shows tracked changes in:

```text
.idea/workspace.xml
.idea/runConfigurations/Minecraft_Server.xml
```

These do not break Phase 3, but they should be cleaned in a separate commit.

```powershell
git rm --cached -- ".idea/workspace.xml"
git restore --source=master -- ".idea/runConfigurations/Minecraft_Server.xml"
git add ".idea/runConfigurations/Minecraft_Server.xml"
git status
git commit -m "Clean tracked IntelliJ local state"
git push
```

If the restored run configuration has no staged difference, that is acceptable.

---

## 15. Completion criteria

Phase 3 is complete only when:

- Interaction restrictions use Architectury events.
- Break/place protection uses Architectury events.
- Shared initialization registers both event classes.
- The Fabric entrypoint only retains networking.
- The break and placement mixin entries are removed.
- The break and placement mixin Java files are deleted.
- The explosion and natural trader mixins remain.
- Clean build succeeds.
- Client and dedicated server start.
- The focused test matrix passes.
- Changes are pushed only to `architectury-multiloader-migration`.
