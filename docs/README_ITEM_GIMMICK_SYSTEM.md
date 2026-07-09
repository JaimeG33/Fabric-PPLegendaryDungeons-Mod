# Item Gimmick System

This document explains the general item-gimmick system used by Professor Porker's Legendary Dungeons.

## Purpose

The item-gimmick system is a lightweight server-side inventory scanner. It looks for special item stacks that contain hidden custom data, then routes those items to the correct handler.

This allows the mod to add post-processing behavior to items after they enter a player's inventory.

Current supported gimmicks:

- `map_lore`
- `random_map_lore`

Examples of what this system is used for:

- Adding coordinates to generated explorer maps after the player owns them.
- Adding structure-specific lore to known maps.
- Adding broad and resolved lore to random structure maps.

## Main files

```text
src/main/java/porker/pp_legendarydungeons/items/ItemGimmickTicker.java
src/main/java/porker/pp_legendarydungeons/items/ItemGimmickService.java
src/main/java/porker/pp_legendarydungeons/items/ItemGimmickContext.java
```

## Startup registration

The ticker is registered from the main mod initializer:

```java
ItemGimmickTicker.register();
```

This should stay in:

```text
src/main/java/porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeons.java
```

## How the ticker works

`ItemGimmickTicker` runs once every 40 ticks, or about once every 2 seconds.

At each scan:

1. It loops through all online server players.
2. It scans the player's vanilla inventory slots.
3. It skips empty item stacks immediately.
4. It passes non-empty stacks to `ItemGimmickService`.
5. If a handler changes an item, the inventory is marked dirty and synced back to the player.

The ticker currently scans the normal vanilla inventory only. This includes the regular inventory slots exposed by `player.getInventory()`. It does not automatically scan modded inventory systems such as Accessories, Curios, Trinkets, or backpacks.

## Custom data router

All gimmick items should use the same top-level custom-data field:

```text
pp_gimmick
```

This field decides which handler processes the item.

Current values:

```text
pp_gimmick = "map_lore"
pp_gimmick = "random_map_lore"
```

The service logic is intentionally simple:

```text
if stack has no custom data:
    skip

if custom data has no pp_gimmick:
    skip

if pp_gimmick is empty:
    skip

if pp_gimmick == "map_lore":
    send to MapLoreGimmick

if pp_gimmick == "random_map_lore":
    send to RandomMapLoreGimmick
```

## Adding a new item gimmick type

To add a new gimmick later:

1. Create a new handler class.
2. Pick a new `pp_gimmick` value.
3. Add the custom data to the item stack or loot table.
4. Add a new case to `ItemGimmickService`.

Example future value:

```text
pp_gimmick = "relic_key"
```

Example future service branch:

```java
case "relic_key" -> RelicKeyGimmick.process(context, customData);
```

## Important custom data rule

The item-gimmick system does not use `custom_model_data` for routing.

It uses:

```json
"minecraft:custom_data": {
  "pp_gimmick": "..."
}
```

`custom_model_data` should only be used when a resource pack needs a custom visual model. It is not required for the gimmick system.
