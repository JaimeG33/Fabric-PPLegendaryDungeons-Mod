# Phase 5 — Shared Architectury Networking and Client Boundary

## Status

**Complete and user-tested.**

Final correction commit:

```text
407787814c56007849305b9ba89fe6ab55f5e175
```

Verified branch relationship after completion:

```text
Branch: architectury-multiloader-migration
Ahead of master: 10 commits
Behind master: 0 commits
```

## Goal

Replace direct Fabric networking APIs with Architectury typed payload transport and establish a shared client initialization boundary without changing packet IDs, field order, controller behavior, or server-authoritative validation.

## Stable packet IDs

These IDs remain unchanged:

```text
pp_legendarydungeons:open_dungeon_rule_editor
pp_legendarydungeons:update_dungeon_rule_block
```

The payload records continue implementing `CustomPacketPayload` and retain vanilla `Type` and `StreamCodec` definitions.

## Final implementation

### Shared server-safe registration

Full path:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\network\DungeonRuleNetworking.java
```

The class now:

- Registers the C2S payload and receiver through Architectury's typed payload API.
- Registers the S2C payload type without a receiver only on a physical dedicated server.
- Queues all game-state work through the Architectury packet context.
- Sends editor snapshots with `NetworkManager.sendToPlayer(player, payload)`.
- Preserves permission, distance, block-entity, preset, persistence, completion, and preview validation on the server.

### Shared client-only registration

Full paths:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\LegendaryDungeonsClient.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\client\DungeonRuleClientNetworking.java
```

The client boundary:

- Is called only from the loader client entrypoint.
- Registers the S2C payload and receiver together.
- Queues screen creation to the client thread.
- Sends controller updates with `NetworkManager.sendToServer(payload)`.

### Thin Fabric entrypoints

Full paths:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\ProfessorPorkersLegendaryDungeons.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\ProfessorPorkersLegendaryDungeonsClient.java
```

The Fabric main entrypoint delegates to:

```java
LegendaryDungeons.init();
```

The Fabric client entrypoint delegates to:

```java
LegendaryDungeonsClient.init();
```

## Final API model

| Previous Fabric API | Final shared replacement |
|---|---|
| Fabric payload type registration | Architectury typed receiver/type registration |
| `ServerPlayNetworking.registerGlobalReceiver` | `NetworkManager.registerReceiver(Side.C2S, TYPE, CODEC, receiver)` |
| `ClientPlayNetworking.registerGlobalReceiver` | `NetworkManager.registerReceiver(Side.S2C, TYPE, CODEC, receiver)` |
| `ServerPlayNetworking.send` | `NetworkManager.sendToPlayer` |
| `ClientPlayNetworking.send` | `NetworkManager.sendToServer` |
| Fabric receiver execution context | `NetworkManager.PacketContext.queue(...)` |
| Manually allocated packet buffers | Typed payload objects and payload-owned codecs |

The final payload codecs use `RegistryFriendlyByteBuf`. Architectury handles cross-loader transport around the typed payload object; no raw `Unpooled` buffer or manual Fabric send remains.

## Corrections made during Phase 5

### Typed payload overload

An initial attempt used the wrong raw-buffer overload. The final implementation registers the payload `TYPE`, `CODEC`, and receiver together through Architectury's typed API.

### Duplicate S2C registration

Integrated singleplayer runs the physical client and integrated server in the same JVM. Registering the S2C type in both common initialization and client initialization caused duplicate registration.

The final rule is:

- Physical client: `DungeonRuleClientNetworking.register()` registers the S2C type and receiver.
- Physical dedicated server: `DungeonRuleNetworking.register()` registers only the S2C type required for sending.

Both classes retain one-time guards.

### Remaining direct Fabric sends

The parent and zone screens initially still called `ClientPlayNetworking.send(...)` after the server networking boundary had migrated.

Final correction commit `4077878...` changed both screens to call:

```java
DungeonRuleClientNetworking.sendUpdate(payload);
```

No direct Fabric networking import remains in either screen.

## Threading and authority

Server receiver work is queued before accessing players, levels, block entities, saved data, completion state, or previews.

Client receiver work is queued before calling `Minecraft.setScreen(...)`.

The client only requests actions. The server remains authoritative and checks:

- Operator/bypass permission.
- Maximum editing distance.
- Whether the controller still exists.
- Whether the payload's expected controller type matches the actual block entity.
- Preset parsing and fallback.
- Completion and reactivation state.
- Parent/zone persistence and linking.

## Client isolation

The shared server initializer does not reference:

```text
LegendaryDungeonsClient
DungeonRuleClientNetworking
DungeonRuleParentScreen
DungeonRuleZoneScreen
net.minecraft.client.Minecraft
```

Client classes are reached only from the Fabric client entrypoint and, later, the NeoForge client bootstrap.

## Reported verification

The final Phase 5 build was reported to have passed these focused tests:

- Fabric client startup.
- Existing and manually placed parent/zone controllers open.
- Parent and zone save actions work.
- Preview actions work.
- Dungeon rules continue functioning.
- Final direct-Fabric-send corrections were pushed.

Phase 6 performs the broader build, dedicated-server, existing-world, persistence, multiplayer, and full-feature regression checkpoint.

## Files changed during Phase 5

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\LegendaryDungeons.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\LegendaryDungeonsClient.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\ProfessorPorkersLegendaryDungeons.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\ProfessorPorkersLegendaryDungeonsClient.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\network\DungeonRuleNetworking.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\network\OpenDungeonRuleEditorPayload.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\network\UpdateDungeonRuleBlockPayload.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\client\DungeonRuleClientNetworking.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\client\DungeonRuleParentScreen.java
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\client\DungeonRuleZoneScreen.java
```

## Completion criteria

Phase 5 is complete because:

- Direct Fabric networking imports and sends were removed from the migrated networking path.
- Shared initialization registers C2S networking.
- Shared client initialization registers S2C networking.
- Both Fabric entrypoints are thin.
- Packet IDs and payload field order remain unchanged.
- Server-authoritative validation remains intact.
- Client screen creation remains isolated from dedicated-server initialization.
- Focused client/controller tests passed.
- The final corrections were pushed only to the migration branch.

## Next phase

Phase 6 is the full Fabric regression checkpoint. Do not begin the physical `common`, `fabric`, and `neoforge` module split until Phase 6 is fully tested, documented, committed, and preferably tagged.
