# Phase 6 — Complete Fabric Regression Checkpoint

## Status

**In progress.**

Do not mark this phase complete until the full checklist has passed and the result is committed and pushed.

## Starting point

```text
Repository: JaimeG33/Fabric-PPLegendaryDungeons-Mod
Project root: D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
Branch: architectury-multiloader-migration
Starting commit: 407787814c56007849305b9ba89fe6ab55f5e175
Compared with master: 10 commits ahead, 0 behind
Minecraft: 1.21.1
Java: 21
Cobblemon: 1.7.3+1.21.1
Mega Showdown: 1.6.9+1.7.3+1.21.1
Architectury API: 13.0.8
Fabric Loader: 0.17.2
Fabric API: 0.116.6+1.21.1
Fabric Language Kotlin: 1.13.6+kotlin.2.2.20
Accessories Fabric: 1.1.0-beta.52+1.21.1
```

## Goal

Prove that the current single-module Fabric project remains a known-good implementation after Phases 1–5 moved its internal architecture toward shared/common APIs.

Phase 6 is a regression checkpoint, not a new architecture phase.

### Allowed changes

- Documentation.
- Test scripts.
- Narrow fixes for regressions discovered during testing.
- Small warning corrections only when they affect reliability or future parity.

### Changes to avoid

- Physical `common`, `fabric`, and `neoforge` module creation.
- Gameplay balancing.
- New features.
- Identifier renames.
- Broad refactors.
- Activating dormant Crystal Caves content.
- Making optional integrations required solely to remove warnings.

## Stable identifiers

Do not change:

```text
Public name: Cobblemon: Explore Legendary Dungeons
Mod ID / namespace: pp_legendarydungeons
Java package: porker.pp_legendarydungeons
Packet: pp_legendarydungeons:open_dungeon_rule_editor
Packet: pp_legendarydungeons:update_dungeon_rule_block
Block/entity: pp_legendarydungeons:dungeon_rule_parent
Block/entity: pp_legendarydungeons:dungeon_rule_zone
```

Also preserve saved-data IDs, scoreboards, command tags, structure IDs, functions, loot tables, WTrader IDs, map targets, block-entity NBT fields, and existing dungeon instance records.

## Working rules

- Work only on `architectury-multiloader-migration`.
- Keep `master` unchanged.
- Start with a clean working tree.
- Use a copied backup for existing-world testing.
- Keep server validation authoritative.
- Treat a clean build as necessary but not sufficient.
- Connect a matching client to the dedicated server; server startup alone does not validate networking.
- Record results while testing rather than reconstructing them afterward.

## Result summary

Fill this table during testing.

| Area | Test | Result | Notes |
|---|---|---|---|
| Source control | Correct branch and clean start | Not run | |
| Static audit | Obsolete Fabric networking absent | Not run | |
| Static audit | Old migrated Fabric hooks absent | Not run | |
| Static audit | Removed break/place mixins absent | Not run | |
| Static audit | Fabric imports limited to entrypoints | Not run | |
| Static audit | Client imports isolated | Not run | |
| Build | `clean build --warning-mode all` | Not run | |
| Artifact | Fabric JAR and sources JAR exist | Not run | |
| Artifact | Metadata/classes/resources present | Not run | |
| Client | Title screen | Not run | |
| Fresh world | World creation and scoreboard setup | Not run | |
| Existing world | Registry and controller compatibility | Not run | |
| Persistence | Controller NBT survives reload | Not run | |
| Networking | Parent editor matrix | Not run | |
| Networking | Zone editor matrix | Not run | |
| Networking | Invalid actions rejected | Not run | |
| Restrictions | Beds, PCs, healers, portable items | Not run | |
| Protection | Break rules | Not run | |
| Protection | Placement rules | Not run | |
| Protection | Explosion behavior | Not run | |
| Summons | Rayquaza and completion tracking | Not run | |
| Tick systems | Four shared ticker systems | Not run | |
| Maps | Map conversion and saved targets | Not run | |
| WTrader | Reload and natural replacement | Not run | |
| Villagers | Cartographer injections | Not run | |
| Loot | Minecraft loot injections | Not run | |
| Loot | Pokémon loot bridge | Not run | |
| Dependency | Mega Showdown integration | Not run | |
| Server | Dedicated startup | Not run | |
| Multiplayer | Connected client, both packet directions | Not run | |
| Restart | Save, stop, restart, reconnect | Not run | |
| Logs | Exceptions and warnings classified | Not run | |

## 1. Prepare the branch

Open PowerShell in:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
```

Run:

```powershell
git switch architectury-multiloader-migration
git pull --ff-only origin architectury-multiloader-migration
git branch --show-current
git status --short
git rev-parse HEAD
git rev-list --left-right --count master...architectury-multiloader-migration
```

Expected initial head for this document:

```text
407787814c56007849305b9ba89fe6ab55f5e175
```

If the branch has advanced, inspect the newer commits and update the starting-point record before continuing.

## 2. Run the static audit script

Full path:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\tools\migration\phase6\Run-Phase6StaticAudits.ps1
```

Run from the project root:

```powershell
powershell -ExecutionPolicy Bypass -File `
    ".\tools\migration\phase6\Run-Phase6StaticAudits.ps1"
```

Expected log:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\phase6-static-audit.log
```

### Expected static results

No results should appear for:

```text
PayloadTypeRegistry
ServerPlayNetworking
ClientPlayNetworking
Unpooled
ServerLifecycleEvents
ServerTickEvents
UseBlockCallback
UseItemCallback
TradeOfferHelper
LootTableEvents
DungeonBlockBreakMixin
DungeonBlockPlacementMixin
```

No direct `Registry.register` or `BuiltInRegistries` call should remain in:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\setup\ModBlocks.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\setup\ModBlockEntities.java
```

Known intentional `net.fabricmc` Java imports should be limited to:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\ProfessorPorkersLegendaryDungeons.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\ProfessorPorkersLegendaryDungeonsClient.java
```

Client imports must not appear in:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\LegendaryDungeons.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\network\DungeonRuleNetworking.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\server\ServerTickScheduler.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\setup\ModBlocks.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\setup\ModBlockEntities.java
```

## 3. Clean build and artifact inspection

Full path:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\tools\migration\phase6\Run-Phase6BuildAndArtifactCheck.ps1
```

Run:

```powershell
powershell -ExecutionPolicy Bypass -File `
    ".\tools\migration\phase6\Run-Phase6BuildAndArtifactCheck.ps1"
```

Expected logs:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\phase6-clean-build.log
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\phase6-artifact-audit.log
```

Required result:

```text
BUILD SUCCESSFUL
```

Record:

```text
Build result:
Build duration:
Warnings owned by this project:
Third-party warnings:
Artifact filename:
Artifact inspection result:
```

The main Fabric JAR should contain at least:

```text
fabric.mod.json
pp_legendarydungeons.mixins.json
porker/pp_legendarydungeons/LegendaryDungeons.class
porker/pp_legendarydungeons/LegendaryDungeonsClient.class
assets/pp_legendarydungeons/
data/pp_legendarydungeons/
```

It should not contain removed break/place mixin classes.

## 4. Integrated client startup

Run:

```powershell
.\gradlew.bat runClient 2>&1 | Tee-Object -FilePath ".\phase6-run-client.log"
```

At the title screen verify:

- Required mods are recognized.
- The mod name and icon are correct.
- No duplicate packet type registration occurs.
- No payload encoding/decoding exception occurs.
- No client entrypoint exception occurs.

Must not appear:

```text
Packet type ... already registered
BufCustomPacketPayload ClassCastException
Failed to encode update_dungeon_rule_block
NoClassDefFoundError
Could not execute entrypoint
```

Result:

```text
Integrated client startup:
Relevant log notes:
```

## 5. Fresh-world test

Create a temporary creative world.

Verify:

- World creation completes.
- Expected scoreboards initialize.
- No registry or data-pack failure occurs.
- Both controller blocks can be obtained and placed:

```mcfunction
/give @s pp_legendarydungeons:dungeon_rule_parent
/give @s pp_legendarydungeons:dungeon_rule_zone
```

- Invisible/selectable behavior remains intentional.
- Creative operator can open both editors.
- Non-operator cannot open either editor.
- Parent and zone values save.
- Preview toggles.
- Complete and reactivate work.
- Closing and reopening shows server values.
- Saving, quitting, and reopening preserves values.

Result:

```text
Fresh-world test:
Scoreboard setup:
Controller placement:
Controller persistence:
```

## 6. Existing-world compatibility

Use a copied backup, never the only important world.

Verify:

- No missing registry entries.
- Existing parent and zone controller blocks remain.
- Block-entity NBT remains readable.
- Preset IDs and channels remain.
- Offsets, sizes, priority, and maximum parent distance remain.
- Rule overrides remain.
- Completion state remains.
- Saved dungeon instance records remain.
- Structures and controller links remain.
- Saving and reloading does not erase values.

Result:

```text
Existing-world copy used:
World loaded:
Controller NBT retained:
Saved data retained:
Problems found:
```

## 7. Controller and networking matrix

### Parent editor

Test:

- Save.
- Preview on/off.
- Complete.
- Reactivate.
- Enabled toggle.
- Preset.
- Link channel.
- Every rule override.
- Close and reopen to verify authoritative values.

### Zone editor

Test:

- Save.
- Preview on/off.
- Preset.
- Link channel.
- X/Y/Z offsets.
- X/Y/Z sizes.
- Priority.
- Maximum parent distance.
- Every rule override.
- Parent-link status.
- Close and reopen to verify authoritative values.

### Validation and stale-screen cases

Test:

- Non-operator attempts.
- Creative non-operator, where applicable.
- Move beyond 16 blocks while the screen remains open, then send an action.
- Remove the controller while its screen remains open, then send an action.
- Disconnect and reconnect, then save again.

Expected:

- Invalid actions are rejected.
- No crash or disconnect occurs.
- Client values do not override rejected server state.
- Feedback is clear and not duplicated.

Result:

```text
Parent editor:
Zone editor:
Permission rejection:
Distance rejection:
Removed-controller rejection:
Reconnect test:
```

## 8. Interaction restrictions

Inside an active rule zone, test each related rule enabled and disabled:

- Bed.
- Placed PC block.
- Placed healer block.
- Portable PC targeting air.
- Portable PC targeting a block.
- Portable healer targeting air.
- Portable healer targeting a block.
- Survival player.
- Creative non-operator.
- Creative operator bypass.

Confirm denial feedback appears once per denied action.

Result:

```text
Interaction restrictions:
Bypass behavior:
Feedback duplication:
```

## 9. Break protection

With breaking disabled, test:

- Normal block denied.
- Allowed break-tag blocks allowed.
- Fire can be extinguished.
- Soul fire can be extinguished.
- Decorated pot.
- Floor torch.
- Wall torch.
- Soul torch.
- Soul wall torch.
- Relic coin pouch, where available.
- Position just inside the boundary.
- Position just outside the boundary.
- Creative operator bypass.

Result:

```text
Break protection:
Allowed blocks:
Fire behavior:
Boundary behavior:
```

## 10. Placement protection

With placement disabled, test:

- Full cube denied.
- Floor and wall torch variants follow their intended allowances.
- Door denied without leaving one half behind.
- Bed denied without leaving one half behind.
- Waterlogged block denied cleanly.
- Directional block denied cleanly.
- Boundary cases use the actual placed position.
- Creative operator bypass.
- Non-player placement where practical.

Result:

```text
Placement protection:
Multi-block placement:
Boundary behavior:
Non-player behavior:
```

## 11. Explosion regression

Test:

1. Explosion origin inside a protected active zone.
2. Explosion origin outside while affected blocks include protected blocks.
3. Entity damage behavior.
4. Protected block behavior.
5. Outside block behavior.
6. Completed or inactive dungeon behavior.

Keep the specialized explosion mixin unless equivalent cross-loader behavior is proven later.

Result:

```text
Explosion inside:
Explosion crossing boundary:
Entity damage:
Inactive/completed behavior:
```

## 12. Summons and dungeon completion

Using the documented Sky Pillar development route, verify:

- Normal Rayquaza summon.
- Secret summon if a safe test route exists.
- One-time summon locks.
- UUID completion tracking.
- Defeat completion.
- Capture/ownership completion.
- Distance handling.
- Loaded-chunk missing-entity handling.
- Unloaded chunks do not falsely complete.
- Completion disables the intended rules.
- Reactivation restores the intended rules.
- Restart does not lose the summon/completion watch.

Do not activate or advertise dormant Crystal Caves content.

Result:

```text
Normal summon:
Secret summon:
Completion routes:
Chunk handling:
Restart persistence:
```

## 13. Tick-driven systems

Verify representative behavior from:

```text
EntityLegendarySummonTicker
FeatureTicker
ItemGimmickTicker
SecretTicker
```

Watch for:

- Duplicate execution.
- Wrong interval.
- No execution.
- State leaking between server sessions.
- Shared tick counter failing to reset after shutdown.

Result:

```text
Summon ticker:
Feature ticker:
Item gimmick ticker:
Secret ticker:
Shutdown/reset behavior:
```

## 14. Maps and item gimmicks

Test available routes for:

- Dedicated Sky Pillar map.
- Random legendary dungeon map.
- Miscellaneous or research-outpost random maps where available.

Verify:

- Item conversion.
- Correct map target.
- Correct lore.
- Approximate coordinate behavior.
- One-time `pp_coords_revealed` behavior.
- Saved random target resolution.
- Sky Pillar remains the active release dungeon.
- Crystal Caves remains disabled.

Result:

```text
Sky Pillar map:
Random map:
Lore/coordinates:
Persistence:
```

## 15. WTrader

Run:

```mcfunction
/reload
```

Verify:

- Pools parse.
- Profiles parse.
- Selection tables parse.
- Offers generate.
- Smoke tests behave as documented.
- Reload listeners do not duplicate.

Natural trader tests:

- Natural replacement occurs.
- Only the vanilla-tracked natural trader is replaced.
- Manually summoned trader remains unaffected unless intended.
- Vanilla offers are preserved or replaced according to the selected profile.
- Map offers work.
- Multiplayer behavior is stable.

Result:

```text
Reload:
Profile parsing:
Natural replacement:
Manual trader behavior:
Multiplayer trader behavior:
```

## 16. Cartographer trades

Test novice and master levels.

Verify:

- Injections are additive.
- Vanilla trades remain.
- Other mod trades remain.
- Prices, uses, XP, and map targets remain correct.
- Reload does not duplicate trades.

Result:

```text
Novice trades:
Master trades:
Additive behavior:
Reload duplication:
```

## 17. Minecraft loot injection

Verify configured target tables:

- Retain original loot.
- Receive new independent pools.
- Continue respecting data-pack authority.
- Can be exercised through `/loot` where applicable.
- Resolve required Mega Showdown item IDs.

Record missing optional-addon IDs separately from migration regressions.

Result:

```text
Chest/entity target tables:
Original loot retained:
Injected loot:
Mega Showdown IDs:
Optional-addon warnings:
```

## 18. Pokémon loot bridge

Test:

- Direct player kill.
- Standard wild battle.
- Multiplayer battle where practical.
- Attribution fallback.
- Native Cobblemon drops retained.
- Additional configured loot runs.
- Cancelled drops remain respected.
- Temporary battle/faint context is cleaned up.

Result:

```text
Direct kill:
Wild battle:
Multiplayer battle:
Native drops:
Injected drops:
Context cleanup:
```

## 19. Mega Showdown compatibility

Verify:

- Required dependency is recognized at startup.
- Accessories dependency is present.
- Referenced items resolve.
- Referenced structures resolve.
- Trade references resolve.
- Loot references resolve.
- No undocumented Mega Showdown Java implementation class is imported.

Keep the metadata range:

```text
mega_showdown >=1.6.9 <=1.7.4
```

unless a wider range is deliberately tested.

Result:

```text
Startup dependency recognition:
Accessories:
Items/structures:
Trades/loot:
```

## 20. Dedicated Fabric server

Run:

```powershell
.\gradlew.bat runServer 2>&1 | Tee-Object -FilePath ".\phase6-run-server.log"
```

Verify before connecting:

- Dedicated server starts without client-class loading errors.
- Registries initialize.
- Lifecycle and scoreboards initialize.
- Reload listeners register once.
- Networking registers without duplication.
- Server reaches ready state.

Then connect a matching client and test:

- Parent editor opens from S2C snapshot.
- Zone editor opens from S2C snapshot.
- Parent updates reach the server through C2S.
- Zone updates reach the server through C2S.
- Preview works.
- Save works.
- Complete/reactivate work.
- Disconnect/reconnect works.
- Server save/stop/restart preserves state.

A server reaching ready state without a connected client is not sufficient.

Result:

```text
Dedicated startup:
Client connection:
S2C:
C2S:
Reconnect:
Restart persistence:
```

## 21. Log review

Full path:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\tools\migration\phase6\Review-Phase6Logs.ps1
```

Run:

```powershell
powershell -ExecutionPolicy Bypass -File `
    ".\tools\migration\phase6\Review-Phase6Logs.ps1"
```

Expected review file:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\phase6-log-review.log
```

Classify every match as:

- Migration regression.
- Existing project warning.
- Missing optional development dependency.
- Third-party warning.
- Harmless shutdown message.
- Release blocker.

Do not ignore an exception solely because the game remained open.

## 22. Known issues requiring classification

Previously observed items include:

- Missing SimpleTMs IDs when SimpleTMs is absent.
- Dormant Crystal Caves loot references.
- Cobblemon data-fixer warnings.
- Optional Cobblemon integration mixins for absent mods.
- Cobblemon resource `LICENSE` path warnings.
- Missing third-party sounds or textures.
- WTrader smoke-test invalid `null` item warnings.
- Deprecated Gradle features.
- Missing Kotlin output-directory warning.
- Deprecated API use in Rayquaza completion code.

These are not automatically Architectury regressions. Anything that breaks an advertised required feature remains a release issue.

## 23. Dependency profiles

### Required profile

- Fabric Loader.
- Fabric API.
- Architectury API.
- Cobblemon.
- Mega Showdown.
- Accessories.
- Fabric Language Kotlin.

### Optional compatibility profile

Install addons such as SimpleTMs only while testing the resources that intentionally reference them.

Do not silently make an optional addon required solely to eliminate a warning.

## 24. Targeted regression fixes

Record every code or resource fix made during Phase 6.

| Commit/file | Regression | Fix | Retest result |
|---|---|---|---|
| None yet | | | |

Any fix should remain shared/common-friendly unless a loader difference is proven unavoidable.

## 25. Final test decision

Fill after all testing:

```text
Phase 6 decision: IN PROGRESS / PASS / FAIL
Ending commit:
Build result:
Integrated client result:
Fresh-world result:
Existing-world result:
Dedicated-server result:
Connected multiplayer networking result:
Persistence result:
Known blockers:
Deferred non-blockers:
```

## 26. Completion criteria

Phase 6 is complete only when:

- The migration branch is clean and pushed.
- `master` remains unchanged.
- Static audits pass or every remaining match is intentional and documented.
- Clean build succeeds.
- Main and sources JARs are inspected.
- Integrated client starts.
- Fresh world passes.
- Existing-world copy passes.
- Controller NBT and saved data persist.
- Parent and zone editor matrices pass.
- Unauthorized, distant, and stale-controller actions are rejected safely.
- Interaction restrictions match the baseline.
- Break, placement, fire, and explosion behavior match the baseline.
- Summons and completion tracking work.
- All four tick-driven systems execute correctly.
- Maps and item gimmicks work.
- WTrader reload and natural replacement work.
- Cartographer trades remain additive.
- Minecraft loot injection remains additive.
- Pokémon loot bridge works and cleans temporary context.
- Mega Showdown integration works with required dependencies.
- Dedicated Fabric server starts without client-class errors.
- A matching client connects and both packet directions work.
- Server save, stop, restart, reconnect, and persistence pass.
- Exceptions and warnings are classified.
- This document contains the final results.
- A known-good commit exists.
- The pre-split tag exists, or a deliberate no-tag decision is documented.

Do not start Phase 7 before all completion criteria pass.

## 27. Commit and tag after completion

Stage only intended changes:

```powershell
git add docs
git add tools/migration/phase6
git add src/main/java
git add src/main/resources
git status
git diff --cached
```

If no code fixes were needed, do not stage unrelated Java or resource files.

Recommended commit:

```powershell
git commit -m "Complete Fabric Architectury regression checkpoint"
git push origin architectury-multiloader-migration
```

After the pushed commit, clean working tree, and connected dedicated-server test are verified:

```powershell
git tag -a architectury-fabric-checkpoint-phase6 `
    -m "Known-good Fabric checkpoint before common/fabric/neoforge split"

git push origin architectury-fabric-checkpoint-phase6
```

Do not create the tag before the dedicated-server networking and restart-persistence tests pass.
