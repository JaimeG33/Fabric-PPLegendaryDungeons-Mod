# Phase 2 — Common Event Foundation

## Status

**Ready to apply and verify.**

Phase 1 has already been pushed to:

```text
architectury-multiloader-migration
```

The Phase 1 Gradle build and development-client startup were reported successful. This phase should remain on the same branch.

---

## 1. Goal

Move the lowest-risk loader boundaries from direct Fabric APIs to Architectury while the project remains a single-module Fabric build.

Phase 2 establishes:

- A loader-neutral shared initializer.
- Architectury server lifecycle events.
- One Architectury post-server-tick listener.
- A central interval scheduler for all four existing ticker systems.
- Architectury server-data reload registration.
- Architectury cartographer trade registration.
- Architectury additive loot-table modification.

The gameplay implementations behind those boundaries remain intentionally unchanged.

---

## 2. API replacements

| Current Fabric API | Phase 2 replacement |
|---|---|
| `ServerLifecycleEvents.SERVER_STARTED` | `LifecycleEvent.SERVER_STARTED` |
| `ServerLifecycleEvents.SERVER_STOPPED` | `LifecycleEvent.SERVER_STOPPED` |
| Four `ServerTickEvents.END_SERVER_TICK` listeners | One `TickEvent.SERVER_POST` listener |
| `ResourceManagerHelper` reload registration | `ReloadListenerRegistry` |
| `SimpleSynchronousResourceReloadListener` | Vanilla `SimplePreparableReloadListener<Unit>` |
| `TradeOfferHelper.registerVillagerOffers` | `TradeRegistry.registerVillagerTrade` |
| `LootTableEvents.MODIFY` | `LootEvent.MODIFY_LOOT_TABLE` |

Architectury 13.0.8's event and registry APIs are direct compile dependencies because of Phase 1.

---

## 3. Files added

### Shared initializer

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\LegendaryDungeons.java
```

Responsibilities:

- Owns the canonical `MOD_ID` and logger.
- Bootstraps presets, completion conditions, blocks, and block entities.
- Registers shared lifecycle behavior.
- Registers WTrader reloads.
- Registers cartographer additions.
- Registers Minecraft loot injections.
- Registers the Cobblemon Pokémon loot bridge.
- Registers the shared server scheduler.
- Protects against accidental duplicate initialization.

### Shared server scheduler

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\server\ServerTickScheduler.java
```

Responsibilities:

- Registers one `TickEvent.SERVER_POST` callback.
- Maintains one server-tick counter.
- Calls the four existing subsystems in their previous registration order.
- Resets interval alignment when a server stops.

### Focused phase document

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\PHASE_02_COMMON_EVENT_FOUNDATION.md
```

---

## 4. Files replaced

### Fabric entrypoint

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\ProfessorPorkersLegendaryDungeons.java
```

The Fabric entrypoint becomes thin:

1. Calls `LegendaryDungeons.init()`.
2. Temporarily registers Fabric networking.
3. Temporarily registers Fabric dungeon interaction callbacks.
4. Logs Fabric initialization.

`MOD_ID` and `LOGGER` remain as aliases so unchanged classes continue compiling.

### Existing ticker systems

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\features\FeatureTicker.java

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\items\ItemGimmickTicker.java

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\secrets\SecretTicker.java

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\summon\trigger\EntityLegendarySummonTicker.java
```

Each class:

- No longer registers a Fabric event.
- No longer owns a separate static tick counter.
- Exposes `tick(MinecraftServer server, long tickCount)`.
- Retains its prior interval and inner gameplay logic.

Intervals remain:

| System | Interval |
|---|---:|
| Legendary summon/completion tracker | 20 ticks |
| Secrets | 20 ticks |
| Features/dungeon player tracker | 30 ticks |
| Item gimmicks | 40 ticks |

### WTrader reload registration and listener

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\features\wtraders\json\WTraderJsonReloadRegistrar.java

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\features\wtraders\json\WTraderJsonReloadListener.java
```

The registrar now uses `ReloadListenerRegistry`.

The listener now extends vanilla:

```java
SimplePreparableReloadListener<Unit>
```

Its existing static parser remains intact. The prepare phase returns `Unit.INSTANCE`; the apply phase calls the existing parser and atomic registry replacement.

### Cartographer trade injection

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\features\wtraders\villager\VillagerTradeInjectionRegistrar.java
```

The three existing `VillagerTrades.ItemListing` instances are registered through `TradeRegistry`:

- Cartographer level 1: misc map pool.
- Cartographer level 1: research-outpost map pool.
- Cartographer level 5: dungeon map pool.

### Minecraft loot-table injection

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\loot\LootTableInjectionRegistrar.java
```

The target sets, nested injection tables, roll count, and built-in-only rule remain unchanged.

The only API-level difference is:

```java
context.addPool(poolBuilder)
```

instead of modifying Fabric's supplied `LootTable.Builder`.

### Documentation indexes

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\README.md

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\README.md
```

---

## 5. Behavior that must remain unchanged

### Lifecycle

On server start:

- `ModScoreboards.setup(server)`
- `DungeonRuleManager.bootstrap(server)`

On server stop:

- Clear `DungeonRuleManager`.
- Clear `DungeonRulePreviewManager`.
- Clear temporary Pokémon battle-faint context.
- Reset only the scheduler's counter, not the registered event.

### Tick systems

The following behavior must remain identical:

- Dungeon-completion watches advance every second.
- Legendary summon armor stands are scanned every second.
- Secret starter armor stands are scanned every second.
- Static feature markers and dungeon-player state are checked every 30 ticks.
- Player inventories are checked for item gimmicks every 40 ticks.

### Reloading

`/reload` must still:

- Load all WTrader trade pools.
- Load all WTrader profiles.
- Load all selection tables.
- Load all map offers.
- Atomically replace the runtime registry.
- Print loaded counts.
- Run the WTrader JSON smoke-test logger.

### Trades

The three cartographer candidate listings must remain additive. Vanilla and other mods' trades must not be removed.

### Loot

Only built-in vanilla/mod-bundled target tables should receive the nested injection pools. User datapack replacements must remain authoritative.

---

## 6. Deliberately deferred systems

These remain Fabric-specific after Phase 2:

### Phase 3

```text
DungeonRuleInteractionEvents
```

Also deferred:

- Block break event migration.
- Block placement event migration.
- Portable healer/PC restrictions.
- Placed healer/PC/bed restrictions.
- Controller block interaction cleanup.

### Phase 4

```text
ModBlocks
ModBlockEntities
```

These still use the current registry implementation until conversion to `DeferredRegister` and `RegistrySupplier`.

### Phase 5

```text
DungeonRuleNetworking
ProfessorPorkersLegendaryDungeonsClient
```

Payload registration, sending, receiving, screens, and client boundaries remain Fabric-specific.

### Shared mixins retained

This phase does not change:

- Natural wandering trader replacement mixin.
- Explosion protection mixin.
- Block breaking/placement mixins.
- Any mixin configuration.

---

## 7. Pre-copy Git checks

Run from:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
```

Confirm the correct branch:

```powershell
git branch --show-current
```

Expected:

```text
architectury-multiloader-migration
```

Confirm the Phase 1 working tree is clean:

```powershell
git status
```

Do not copy Phase 2 over uncommitted work you do not recognize.

---

## 8. IntelliJ tracked-file cleanup

The Phase 1 branch currently contains changes to two generated IntelliJ files. They do not affect the mod build, but they should not remain mixed into migration history.

Run these before or in a separate cleanup commit:

```powershell
git rm --cached -- ".idea/workspace.xml"
git restore --source=master -- ".idea/runConfigurations/Minecraft_Server.xml"
git add ".idea/runConfigurations/Minecraft_Server.xml"
git status
git commit -m "Clean tracked IntelliJ local state"
git push
```

Important:

- `git rm --cached` removes `workspace.xml` from Git but leaves the local file on disk.
- `.gitignore` already ignores `.idea/workspace.xml`.
- Restoring the server run configuration removes the generated classpath-order-only change.
- If the second `git add` reports that the run configuration has no changes, that is acceptable.
- Keep this cleanup commit separate from Phase 2.

---

## 9. Copy procedure

The package contains:

```text
COPY_INTO_PROJECT
```

Copy the **contents** of that folder into:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
```

Allow Windows to merge folders and replace the listed files.

The package root itself must not become a subfolder of the repository.

Correct:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\...
```

Incorrect:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\COPY_INTO_PROJECT\src\main\java\...
```

---

## 10. Compile verification

Reload Gradle in IntelliJ, then run:

```powershell
.\gradlew.bat clean build
```

Expected:

```text
BUILD SUCCESSFUL
```

### What compile success proves

- Architectury lifecycle imports resolve.
- Architectury tick imports resolve.
- Architectury reload registration resolves.
- The vanilla reload-listener implementation matches Minecraft 1.21.1.
- Architectury trade registration resolves.
- Architectury loot modification context resolves.
- No old ticker `register()` call remains.

### Useful direct-Fabric import check

Run:

```powershell
Get-ChildItem -Recurse ".\src\main\java" -Filter "*.java" |
    Select-String "ServerLifecycleEvents|ServerTickEvents|ResourceManagerHelper|SimpleSynchronousResourceReloadListener|TradeOfferHelper|LootTableEvents"
```

There should be no results for the Phase 2 files. Results in unrelated, deliberately deferred files should be inspected rather than automatically removed.

---

## 11. Client startup verification

Run:

```powershell
.\gradlew.bat runClient
```

Verify the log contains messages similar to:

```text
[WTrader JSON] Registered Architectury server-data reload listener.
[Villager Trades] Registered 3 additive Architectury cartographer trade-pool listings.
[Loot Injection] Registered 3 additive Architectury loot-table injection groups.
Cobblemon: Explore Legendary Dungeons initialized on Fabric.
```

Load a test world and leave it running long enough for all intervals to execute.

---

## 12. Dedicated-server verification

Run the existing server configuration or:

```powershell
.\gradlew.bat runServer
```

Verify:

- The server starts successfully.
- No client-only class is loaded.
- The common initializer runs once.
- The Architectury scheduler registers once.
- Server shutdown completes without an exception.
- Restarting the development server does not duplicate callbacks.

---

## 13. Focused gameplay tests

### A. WTrader reload

Run:

```mcfunction
/reload
```

Check the log for the WTrader loaded-count message and smoke-test results.

Test at least one generated WTrader and confirm its profile/trades still populate.

### B. Cartographer trades

Use a fresh cartographer or a command-created test cartographer.

Verify:

- Level 1 can draw the misc/research map candidates.
- Level 5 can draw the dungeon map candidate.
- Vanilla cartographer trades still exist.
- Existing villagers are not expected to regenerate old offers automatically.

### C. Chest loot injection

Test or repeatedly generate/open:

- Village cartographer chests.
- Taiga village house chests.
- Igloo basement chests.

Confirm the custom nested tables can still contribute results and the original loot remains.

### D. Entity loot injection

Test player-attributed kills of:

- Warden.
- Elder guardian.
- Wither.

Confirm the base dungeon-key injection can still occur according to its JSON chance and conditions.

### E. Legendary summon/completion tick

Use a structure or test setup containing the relevant summon marker.

Verify:

- Summon checks still occur.
- Persistent dungeon-completion watches continue resolving.
- No repeated summon is caused by duplicate tick callbacks.

### F. Feature/dungeon player tracking

Enter and leave a protected dungeon region.

Verify:

- Dungeon-zone state updates.
- Mining fatigue or other configured player-state behavior remains correct.
- Static feature markers still trigger.

### G. Secret checks

Test one existing secret starter marker and confirm the secret service still evaluates it.

### H. Item gimmicks

Place a known gimmick item in a player inventory and wait at least 40 ticks.

Confirm its previous behavior and inventory synchronization remain correct.

---

## 14. Suggested commit

After all focused tests pass:

```powershell
git add src/main/java/porker/pp_legendarydungeons/LegendaryDungeons.java
git add src/main/java/porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeons.java
git add src/main/java/porker/pp_legendarydungeons/server/ServerTickScheduler.java
git add src/main/java/porker/pp_legendarydungeons/features/FeatureTicker.java
git add src/main/java/porker/pp_legendarydungeons/items/ItemGimmickTicker.java
git add src/main/java/porker/pp_legendarydungeons/secrets/SecretTicker.java
git add src/main/java/porker/pp_legendarydungeons/summon/trigger/EntityLegendarySummonTicker.java
git add src/main/java/porker/pp_legendarydungeons/features/wtraders/json/WTraderJsonReloadRegistrar.java
git add src/main/java/porker/pp_legendarydungeons/features/wtraders/json/WTraderJsonReloadListener.java
git add src/main/java/porker/pp_legendarydungeons/features/wtraders/villager/VillagerTradeInjectionRegistrar.java
git add src/main/java/porker/pp_legendarydungeons/loot/LootTableInjectionRegistrar.java
git add docs/README.md
git add docs/migration/README.md
git add docs/migration/PHASE_02_COMMON_EVENT_FOUNDATION.md

git status
git diff --cached
git commit -m "Migrate common events to Architectury"
git push
```

---

## 15. Rollback

Before committing, restore all Phase 2 tracked modifications and remove the new untracked files with:

```powershell
git restore .
git clean -fd
```

`git clean -fd` deletes every untracked file under the repository, so inspect:

```powershell
git status --short
```

before using it.

After committing, the cleanest rollback is:

```powershell
git revert <PHASE_2_COMMIT_SHA>
```

Do not reset or force-push unless you deliberately intend to rewrite branch history.

---

## 16. Completion criteria

Phase 2 is complete only when:

- `LegendaryDungeons.init()` is the shared initialization foundation.
- The Fabric entrypoint is thin.
- Lifecycle start/stop uses Architectury.
- Exactly one Architectury server post-tick listener owns all four intervals.
- WTrader server-data reload registration uses Architectury.
- Cartographer additions use Architectury.
- Minecraft loot-table additions use Architectury.
- The clean build succeeds.
- Fabric client startup succeeds.
- Fabric dedicated-server startup and shutdown succeed.
- `/reload` succeeds.
- Focused trade, loot, ticker, and lifecycle tests pass.
- The commit is pushed only to `architectury-multiloader-migration`.

---

## 17. Expected remaining direct Fabric boundaries

After this phase, direct Fabric APIs are still expected in:

- Fabric main/client entrypoint types.
- Dungeon networking.
- Dungeon interaction callbacks.
- Any current break/place mixins or callbacks.
- Fabric metadata.
- Fabric build dependencies.

Their continued presence is not a Phase 2 failure.
