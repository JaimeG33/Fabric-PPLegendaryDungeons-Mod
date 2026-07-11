# Dungeon Completion System

## Goal

A generated dungeon instance can move between:

```text
ACTIVE
COMPLETED
DISABLED
```

When an instance becomes completed, its rule boxes stop enforcing restrictions without deleting any controller blocks.

General state files:

```text
src/main/java/porker/pp_legendarydungeons/dungeon_rules/instance/DungeonInstanceState.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/instance/DungeonInstanceRecord.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/instance/DungeonCompletionService.java
```

## Completion watches

Legendary-encounter completion files are intentionally grouped under:

```text
src/main/java/porker/pp_legendarydungeons/summon/dungeon_completion/
```

Generic files:

```text
CompletionCheckResult.java
DungeonCompletionCondition.java
DungeonCompletionWatch.java
DungeonCompletionSavedData.java
DungeonCompletionRegistry.java
DungeonCompletionTracker.java
```

A watch stores:

- unique watch UUID,
- condition ID,
- dungeon instance ID,
- dimension,
- exact tracked entity UUID,
- encounter origin,
- maximum allowed distance,
- last-known position,
- grace/missing counters.

Watches persist across server restarts.

## Rayquaza / Sky Pillar

Dungeon-specific files:

```text
src/main/java/porker/pp_legendarydungeons/summon/dungeon_completion/rayquaza/RayquazaDungeonCompletion.java
src/main/java/porker/pp_legendarydungeons/summon/dungeon_completion/rayquaza/RayquazaCompletionCondition.java
```

The arming step runs only after a successful normal Rayquaza summon. It resolves the active dungeon instance from the spawn zone, with a same-preset/channel parent fallback.

The condition tracks the exact spawned entity UUID—not any nearby Rayquaza.

Completion occurs when the entity:

- is owned/captured,
- is dead,
- has been removed while the last-known chunk is confirmed loaded for repeated checks,
- or has moved more than 100 blocks from the origin.

If its last-known chunk is unloaded, the watch waits. This avoids treating chunk unloading as defeat.

## Adding another dungeon

For a future dungeon, create a dedicated subfolder, for example:

```text
src/main/java/porker/pp_legendarydungeons/summon/dungeon_completion/diancie/
```

Add:

1. An arming/coordinator file called only after that dungeon's final encounter starts.
2. A `DungeonCompletionCondition` implementation.
3. Register the condition in:

```text
src/main/java/porker/pp_legendarydungeons/summon/dungeon_completion/DungeonCompletionRegistry.java
```

The condition can use a legendary entity UUID, boss entity UUID, puzzle state, vault state, or another persistent objective.
