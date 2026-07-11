# Controller UI and Structure-Building Workflow

## Permissions

Only a player who is both:

- in Creative mode, and
- permission level 2 or higher

can edit or bypass dungeon rules.

Permission helper:

```text
src/main/java/porker/pp_legendarydungeons/dungeon_rules/DungeonRulePermissions.java
```

## Networking

Server/client payload files:

```text
src/main/java/porker/pp_legendarydungeons/dungeon_rules/network/OpenDungeonRuleEditorPayload.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/network/UpdateDungeonRuleBlockPayload.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/network/DungeonRuleNetworking.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/client/DungeonRuleClientNetworking.java
```

The client sends desired values, but the server validates:

- permissions,
- distance to the controller,
- block type,
- numeric clamping,
- preset parsing.

## Screens

```text
src/main/java/porker/pp_legendarydungeons/dungeon_rules/client/DungeonRuleParentScreen.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/client/DungeonRuleZoneScreen.java
```

The parent screen edits default settings and offers manual Complete/Reactivate controls.

The zone screen edits offsets, sizes, priority, parent range, and local tri-state overrides.

## Boundary preview

```text
src/main/java/porker/pp_legendarydungeons/dungeon_rules/DungeonRulePreviewManager.java
```

Preview particles:

- are sent only to the requesting builder,
- last 20 seconds,
- are updated on the existing low-frequency player pulse,
- do not create block display entities or persistent particles.

## Invisible blocks and textures

Placed controllers use `RenderShape.INVISIBLE`, have no collision, and retain a full outline for selection.

Inventory/debug textures are included at:

```text
src/main/resources/assets/pp_legendarydungeons/textures/block/dungeon_rule_parent.png
src/main/resources/assets/pp_legendarydungeons/textures/block/dungeon_rule_zone.png
src/main/resources/assets/pp_legendarydungeons/textures/item/dungeon_rule_parent.png
src/main/resources/assets/pp_legendarydungeons/textures/item/dungeon_rule_zone.png
```

The blocks are unbreakable by normal survival mining and have no loot table.

## Recommended authoring pattern

- One parent per generated dungeon copy.
- Several child zones, each centered reasonably near the area it describes.
- Use multiple boxes for towers, floors, detached rooms, and irregular footprints.
- Slightly overlap adjacent boxes to avoid seams.
- Use a higher-priority local zone for exceptions.
- Use different channels when nearby same-preset structures could otherwise compete for the same child.

Save the configured custom blocks directly into the structure NBT. The building instance must contain the mod.
