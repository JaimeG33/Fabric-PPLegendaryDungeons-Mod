# Phase 4 — Shared Architectury Deferred Registries

## Status

**Complete and locally verified.**

Verified remote starting point:

```text
Branch: architectury-multiloader-migration
Commit: 4159a71c66b0faa4b91350e7d390def72ff6dc2a
```

Phase 3 and the fire/soul-fire allowlist patch are present on GitHub.

---


## Completion verification

The Phase 4 deferred-registry changes were reported as successfully built and
tested in-game, then pushed to the remote migration branch. Remote inspection
confirmed the deferred block, item, and block-entity registrations and their
supplier-based call sites.

---

## 1. Goal

Replace direct vanilla registry writes with one shared Architectury registry implementation for:

- Dungeon controller blocks.
- Their block items.
- Parent and zone block-entity types.
- Every Java call site that currently expects an eagerly created registry value.

No registered resource ID changes.

---

## 2. API replacement

Old registration:

```java
Registry.register(BuiltInRegistries.BLOCK, id, block);
Registry.register(BuiltInRegistries.ITEM, id, item);
Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
```

New registration:

```java
DeferredRegister<T>
RegistrySupplier<T>
```

The registry suppliers are declared during class initialization and resolved with:

```java
supplier.get()
```

only after registration is available.

---

## 3. Stable IDs

These IDs must remain exactly unchanged:

```text
pp_legendarydungeons:dungeon_rule_parent
pp_legendarydungeons:dungeon_rule_zone
```

They remain the IDs for:

- Both blocks.
- Both block items.
- Both block-entity types.

Because the IDs do not change, existing world palettes and block-entity NBT should continue resolving to the same entries.

---

## 4. Files replaced

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\setup\ModBlocks.java

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\setup\ModBlockEntities.java

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\blocks\entity\DungeonRuleParentBlockEntity.java

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\blocks\entity\DungeonRuleZoneBlockEntity.java

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\src\main\java\porker\pp_legendarydungeons\dungeon_rules\events\DungeonRuleInteractionEvents.java

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\README.md

D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\README.md
```

New documentation:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons\docs\migration\PHASE_04_SHARED_DEFERRED_REGISTRIES.md
```

No files are deleted in this phase.

---

## 5. Registry structure

### Blocks

`ModBlocks` owns:

```java
DeferredRegister<Block>
```

with typed suppliers:

```java
RegistrySupplier<DungeonRuleParentBlock>
RegistrySupplier<DungeonRuleZoneBlock>
```

### Block items

The same class owns a separate:

```java
DeferredRegister<Item>
```

with:

```java
RegistrySupplier<BlockItem>
```

for each controller block.

### Block-entity types

`ModBlockEntities` owns:

```java
DeferredRegister<BlockEntityType<?>>
```

with typed suppliers for the parent and zone block entities.

### Initialization order

The existing common initializer already calls:

```java
ModBlocks.register();
ModBlockEntities.register();
```

That order is retained.

Inside `ModBlocks.register()`:

1. Blocks register.
2. Block items register.

Then block-entity types register from `ModBlockEntities.register()`.

---

## 6. Supplier call-site changes

### Block interactions

Old:

```java
state.is(ModBlocks.DUNGEON_RULE_PARENT)
```

New:

```java
state.is(ModBlocks.DUNGEON_RULE_PARENT.get())
```

The same applies to the zone block.

### Block-entity constructors

Old:

```java
super(ModBlockEntities.DUNGEON_RULE_PARENT, pos, state);
```

New:

```java
super(ModBlockEntities.DUNGEON_RULE_PARENT.get(), pos, state);
```

The same applies to the zone block entity.

### Block-entity builders

The block-entity registrations resolve:

```java
ModBlocks.DUNGEON_RULE_PARENT.get()
ModBlocks.DUNGEON_RULE_ZONE.get()
```

inside the deferred type suppliers.

---

## 7. Behavior deliberately unchanged

- Controller block hardness and explosion resistance.
- No collision.
- No occlusion.
- No loot table.
- Invisible rendering.
- Full selection outline.
- Parent and zone block item IDs.
- Block-entity NBT field names.
- Parent/zone manager registration.
- Controller editor behavior.
- Saved-data identifiers.
- Structure NBT block IDs.
- Datapack resources.
- Blockstate/model/item-model paths.

---

## 8. Applying the patch

Copy the contents of:

```text
COPY_INTO_PROJECT
```

into:

```text
D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons
```

Allow Windows to merge folders and replace the listed files.

Do not put the `Architectury_Phase_4_Package` folder itself inside the repository.

---

## 9. Pre-build inspection

Confirm branch:

```powershell
git branch --show-current
```

Expected:

```text
architectury-multiloader-migration
```

Inspect:

```powershell
git status --short
```

Expected changed files should match the list in this document.

Search for old direct registry calls:

```powershell
Get-ChildItem -Recurse ".\src\main\java" -Filter "*.java" |
    Select-String "BuiltInRegistries|Registry\.register"
```

There should be no results in `ModBlocks.java` or `ModBlockEntities.java`.

Search supplier call sites:

```powershell
Get-ChildItem -Recurse ".\src\main\java" -Filter "*.java" |
    Select-String "ModBlocks\.DUNGEON_RULE_|ModBlockEntities\.DUNGEON_RULE_"
```

Inspect every result and confirm each runtime value access uses `.get()`.

---

## 10. Build tests

Run:

```powershell
.\gradlew.bat clean build
```

Expected:

```text
BUILD SUCCESSFUL
```

Likely failure categories:

- Missed `.get()` call.
- Incorrect generic inference on a block-entity supplier.
- Wrong registry key import.
- Registration attempted twice.
- A copied file landed in the wrong folder.

---

## 11. Fresh-world test

Run:

```powershell
.\gradlew.bat runClient
```

Create a new test world and verify:

- Both controller blocks can be obtained by command.
- Both can be placed.
- Both remain invisible but selectable.
- Creative operator interaction opens the correct editor.
- Parent and zone values can be edited.
- Leaving and re-entering the world retains their configuration.
- Breaking/removing controllers updates the manager as before.

Useful commands:

```mcfunction
/give @s pp_legendarydungeons:dungeon_rule_parent
/give @s pp_legendarydungeons:dungeon_rule_zone
```

---

## 12. Existing-world compatibility test

Use a copied backup of a world created before Phase 4.

Verify:

- The world opens without “missing registry entry” warnings.
- Existing parent blocks remain in place.
- Existing zone blocks remain in place.
- Existing block entities retain their saved configurations.
- Existing links, sizes, offsets, priorities, presets, and overrides remain.
- Dungeon protection still activates.
- Editor screens still open and show the correct values.
- Saving and reloading the world does not erase controller data.

Do not test migration compatibility on the only copy of an important world.

---

## 13. Dedicated-server test

Run:

```powershell
.\gradlew.bat runServer
```

Verify:

- Both deferred block registers initialize.
- The item register initializes.
- The block-entity type register initializes.
- No registry entry is requested before it is present.
- Server startup and shutdown succeed.
- Repeated development launches do not report duplicate registrations.

---

## 14. Resource verification

No resource file changes are required, but verify that these still resolve:

```text
assets/pp_legendarydungeons/blockstates/dungeon_rule_parent.json
assets/pp_legendarydungeons/blockstates/dungeon_rule_zone.json
assets/pp_legendarydungeons/models/block/dungeon_rule_parent.json
assets/pp_legendarydungeons/models/block/dungeon_rule_zone.json
assets/pp_legendarydungeons/models/item/dungeon_rule_parent.json
assets/pp_legendarydungeons/models/item/dungeon_rule_zone.json
```

The IDs are unchanged, so paths should remain valid.

---

## 15. Commit instructions

After all tests pass:

```powershell
git add src/main/java/porker/pp_legendarydungeons/setup/ModBlocks.java
git add src/main/java/porker/pp_legendarydungeons/setup/ModBlockEntities.java
git add src/main/java/porker/pp_legendarydungeons/blocks/entity/DungeonRuleParentBlockEntity.java
git add src/main/java/porker/pp_legendarydungeons/blocks/entity/DungeonRuleZoneBlockEntity.java
git add src/main/java/porker/pp_legendarydungeons/dungeon_rules/events/DungeonRuleInteractionEvents.java
git add docs

git status
git diff --cached
git commit -m "Move controller registries to Architectury"
git push origin architectury-multiloader-migration
```

---

## 16. Completion criteria

Phase 4 is complete only when:

- No direct registry writes remain for the controller blocks/items/types.
- All six entries use Architectury deferred registers.
- All affected call sites use registry suppliers.
- Resource IDs are unchanged.
- Clean build succeeds.
- Client and dedicated server start.
- Fresh-world controller tests pass.
- A backed-up pre-Phase-4 world loads correctly.
- Controller NBT survives save/reload.
- Changes are pushed only to `architectury-multiloader-migration`.

---

## 17. Deferred to Phase 5

Still Fabric-specific after this phase:

- Payload registration.
- Client and server packet receivers.
- Packet sending.
- Screen-opening payloads.
- Client screen bootstrap.
- The Fabric main and client entrypoint interfaces.

Phase 4 does not change networking.
