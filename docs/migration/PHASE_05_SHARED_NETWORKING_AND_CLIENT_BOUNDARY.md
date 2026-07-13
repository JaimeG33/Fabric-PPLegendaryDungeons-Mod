# Phase 5 — Shared Architectury Networking and Client Boundary

## Status

**Ready to apply and verify.**

Remote verification before this phase:

```text
Branch: architectury-multiloader-migration
Ahead of master: 6 commits
Behind master: 0 commits
```

Phase 4's deferred block, item, and block-entity registrations are present on GitHub.

---

## 1. Goal

Replace the remaining direct Fabric networking APIs with Architectury networking and establish a shared client initialization boundary.

This phase migrates:

- Client-to-server controller updates.
- Server-to-client editor snapshots.
- Global receiver registration.
- Packet sending.
- Main-thread scheduling.
- Shared client initialization.
- Thin Fabric main and client entrypoints.

The two existing payload IDs, payload records, field order, codecs, permission checks, distance checks, block-entity checks, and screen behavior remain.

---

## 2. API replacement

| Direct Fabric API | Phase 5 replacement |
|---|---|
| `PayloadTypeRegistry` | Payload-owned `StreamCodec<FriendlyByteBuf, ...>` |
| `ServerPlayNetworking.registerGlobalReceiver` | `NetworkManager.registerReceiver(Side.C2S, ...)` |
| `ClientPlayNetworking.registerGlobalReceiver` | `NetworkManager.registerReceiver(Side.S2C, ...)` |
| `ServerPlayNetworking.send` | `NetworkManager.sendToPlayer` |
| `ClientPlayNetworking.send` | `NetworkManager.sendToServer` |
| Fabric receiver context execution | `NetworkManager.PacketContext.queue(...)` |

Architectury's receiver accepts a `FriendlyByteBuf` and a cross-loader packet context. The context exposes the receiving player and a queue for the correct game thread.

---

## 3. Stable payload IDs

These IDs remain exactly unchanged:

```text
pp_legendarydungeons:open_dungeon_rule_editor
pp_legendarydungeons:update_dungeon_rule_block
```

Both records continue implementing `CustomPacketPayload` and retain a vanilla `Type` and `StreamCodec`.

The codec buffer type changes from `RegistryFriendlyByteBuf` to `FriendlyByteBuf` because none of the fields require registry-aware serialization and Architectury's transport uses `FriendlyByteBuf`.

---

## 4. New shared client initializer

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\LegendaryDungeonsClient.java
```

Fabric and the future NeoForge client bootstrap both call:

```java
LegendaryDungeonsClient.init();
```

This class must never be referenced from dedicated-server initialization.

---

## 5. Full replacement files

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\LegendaryDungeons.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\ProfessorPorkersLegendaryDungeons.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\ProfessorPorkersLegendaryDungeonsClient.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\client\DungeonRuleClientNetworking.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\network\OpenDungeonRuleEditorPayload.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\network\UpdateDungeonRuleBlockPayload.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\README.md
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\README.md
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\PHASE_04_SHARED_DEFERRED_REGISTRIES.md
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\PHASE_05_SHARED_NETWORKING_AND_CLIENT_BOUNDARY.md
```

---

## 6. Script-patched files

The application script makes small targeted changes to:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\network\DungeonRuleNetworking.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\client\DungeonRuleParentScreen.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\client\DungeonRuleZoneScreen.java
```

The server class registers the C2S receiver with Architectury, queues all world/player work to the server thread, and sends editor snapshots with `NetworkManager.sendToPlayer`.

Both screens route updates through:

```java
DungeonRuleClientNetworking.sendUpdate(payload)
```

---

## 7. Threading behavior

Packet decoding occurs when the Architectury receiver is called.

All game-state work is queued.

### Server

```java
context.queue(() -> handleUpdate(serverPlayer, payload));
```

This keeps permission checks, distance checks, block-entity access, saved data, preview actions, completion actions, and feedback on the server thread.

### Client

```java
context.queue(() -> openEditorScreen(payload));
```

Screen creation and `Minecraft.setScreen(...)` therefore occur on the client thread.

---

## 8. Applying the patch

Extract the package and open PowerShell in the extracted package directory.

Run:

```powershell
powershell -ExecutionPolicy Bypass -File ".\APPLY_PHASE_5.ps1"
```

Default project root:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
```

For another clone:

```powershell
powershell -ExecutionPolicy Bypass -File ".\APPLY_PHASE_5.ps1" -ProjectRoot "D:\Other\Path"
```

The script verifies the project root, copies full replacement files, applies three guarded source edits, and refuses to silently produce a partial migration when an expected old snippet is absent.

No files are deleted.

---

## 9. Pre-build checks

```powershell
git branch --show-current
git status --short

Get-ChildItem -Recurse ".\src\main\java" -Filter "*.java" |
    Select-String "PayloadTypeRegistry|ServerPlayNetworking|ClientPlayNetworking"
```

Expected branch:

```text
architectury-multiloader-migration
```

Expected search result: no matches.

The two Fabric entrypoint interfaces remain intentionally until the module split.

---

## 10. Build test

```powershell
.\gradlew.bat clean build
```

Expected:

```text
BUILD SUCCESSFUL
```

Likely compile failure categories:

- A screen was not patched.
- An expected codec generic did not update.
- A direct Fabric networking import remains.
- The script was run against the wrong branch/version.
- A copied file landed under the package directory instead of the project root.

---

## 11. Integrated-client tests

```powershell
.\gradlew.bat runClient
```

### Parent controller

- Open the editor.
- Save preset, channel, enabled state, and rule overrides.
- Toggle preview.
- Complete the dungeon.
- Reactivate it.
- Close and reopen to confirm current values.

### Zone controller

- Open the editor.
- Save offsets, sizes, priority, maximum parent distance, channel, preset, and rule overrides.
- Toggle preview.
- Confirm parent-link feedback.
- Close and reopen to confirm current values.

### Server validation

- Move more than 16 blocks away while a screen is open, then send an action.
- Remove the controller while its screen is open, then send.
- Test a non-operator.
- Confirm normal dungeon interaction and protection still works.

---

## 12. Dedicated-server two-way test

```powershell
.\gradlew.bat runServer
```

Connect using a matching development client and verify:

- The dedicated server starts without loading client screen classes.
- Both editors open.
- C2S updates reach the server.
- S2C snapshots reach the client.
- Save, preview, complete, and reactivate work.
- Disconnect and reconnect work.
- Restarting does not produce duplicate receiver errors.

A server startup without a connected client does not fully test Phase 5.

---

## 13. Compatibility rule

Use the same Phase 5 build on the client and server.

The IDs and field order remain unchanged, but mixed pre-Phase-5/Phase-5 compatibility should not be promised because the registration and transport boundary changed.

---

## 14. Dedicated-server class-loading check

The server log must not report loading:

```text
LegendaryDungeonsClient
DungeonRuleClientNetworking
DungeonRuleParentScreen
DungeonRuleZoneScreen
net.minecraft.client.Minecraft
```

The common server initializer references only the server-safe networking class.

---

## 15. Commit instructions

```powershell
git add src/main/java/porker/pp_legendarydungeons/LegendaryDungeons.java
git add src/main/java/porker/pp_legendarydungeons/LegendaryDungeonsClient.java
git add src/main/java/porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeons.java
git add src/main/java/porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeonsClient.java
git add src/main/java/porker/pp_legendarydungeons/dungeon_rules/network
git add src/main/java/porker/pp_legendarydungeons/dungeon_rules/client
git add docs

git status
git diff --cached
git commit -m "Migrate dungeon networking to Architectury"
git push origin architectury-multiloader-migration
```

---

## 16. Completion criteria

Phase 5 is complete only when:

- No direct Fabric networking imports remain.
- The shared initializer registers C2S networking.
- The shared client initializer registers S2C networking.
- Both Fabric entrypoints are thin.
- Both payload IDs and field orders are unchanged.
- Server validation remains intact.
- Both handlers queue game-state work correctly.
- The clean build succeeds.
- Integrated-client tests pass.
- Dedicated-server two-way networking tests pass.
- Changes are pushed only to the migration branch.

---

## 17. Next phase

Phase 6 is the full Fabric regression checkpoint before splitting the project into `common`, `fabric`, and `neoforge` modules.
