# Parent Blocks, Child Zones, Presets, and Instances

## Concept

A **preset** is reusable configuration, for example:

```text
pp_legendarydungeons:standard_dungeon
pp_legendarydungeons:sky_pillar
```

A preset is not one generated structure. Every generated structure receives its own **instance ID** from its parent controller's dimension and world position:

```text
minecraft:overworld@1200,90,-3400
```

This allows one Sky Pillar to be completed while another Sky Pillar remains active.

## Parent controller

Implementation:

```text
src/main/java/porker/pp_legendarydungeons/blocks/dungeon_rules/DungeonRuleParentBlock.java
src/main/java/porker/pp_legendarydungeons/blocks/entity/DungeonRuleParentBlockEntity.java
```

The parent stores:

- preset ID,
- link channel,
- enabled state,
- tri-state rule overrides.

The persistent instance record is stored by:

```text
src/main/java/porker/pp_legendarydungeons/dungeon_rules/instance/DungeonRuleSavedData.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/instance/DungeonInstanceRecord.java
```

The parent does not need to stay loaded after it has registered. Its persistent record remains available to child zones.

## Child zone controller

Implementation:

```text
src/main/java/porker/pp_legendarydungeons/blocks/dungeon_rules/DungeonRuleZoneBlock.java
src/main/java/porker/pp_legendarydungeons/blocks/entity/DungeonRuleZoneBlockEntity.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/instance/DungeonZoneRecord.java
```

Each child defines one axis-aligned box:

```text
minimum = controller position + offset
maximum = minimum + size - 1
```

It also stores:

- expected preset ID,
- link channel,
- maximum parent-link range,
- priority,
- local rule overrides.

## Linking

A child links to the nearest parent that matches:

- dimension,
- preset,
- channel,
- maximum range.

Physical touching or overlap is not required for linking. Separate secret rooms or floating platforms can belong to the same instance.

The resolved link is stored in server SavedData, not embedded into the structure-template block NBT. Therefore, copied/generated structures do not retain a stale link to the build-world parent.

## Persistence of large boxes

After a child controller loads once, its definition is written to SavedData. At server start, saved child definitions rebuild the runtime spatial index. This means rule enforcement does not depend on the child anchor chunk remaining loaded.

If an operator actually removes a controller block, its corresponding persistent record is removed through the block's `onRemove` path.

## Priorities and overlaps

The manager first selects the highest priority among all containing zones.

- A higher-priority smaller room overrides a lower-priority general floor box.
- If several zones at the same highest priority overlap, restrictions combine conservatively: if any equal-priority zone denies a rule, it is denied.

Use small priority differences such as `0`, `10`, and `20` rather than extreme values.
