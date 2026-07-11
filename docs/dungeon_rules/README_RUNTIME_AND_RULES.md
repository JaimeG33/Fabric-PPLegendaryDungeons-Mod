# Runtime, Rule Enforcement, and Performance

## Runtime manager

Core files:

```text
src/main/java/porker/pp_legendarydungeons/dungeon_rules/DungeonRuleManager.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/DungeonRuleSpatialIndex.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/DungeonRuleZone.java
```

Every active child box is added to the bucket for each X/Z chunk it intersects. A rule query at a block position examines only that position's chunk bucket, then runs exact X/Y/Z box containment checks.

No controller block entity ticks.

## Rule hierarchy

```text
Preset defaults
  -> Parent tri-state overrides
     -> Child tri-state overrides
```

Tri-state values:

- `INHERIT`: keep the lower-level value.
- `ALLOW`: do not enforce the restriction.
- `DENY`: enforce the restriction.

Core files:

```text
src/main/java/porker/pp_legendarydungeons/dungeon_rules/DungeonRule.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/RuleDecision.java
src/main/java/porker/pp_legendarydungeons/dungeon_rules/DungeonRuleSet.java
```

## Event/action restrictions

Breaking:

```text
src/main/java/porker/pp_legendarydungeons/mixin/dungeon_rules/DungeonBlockBreakMixin.java
```

Placement:

```text
src/main/java/porker/pp_legendarydungeons/mixin/dungeon_rules/DungeonBlockPlacementMixin.java
```

Explosions:

```text
src/main/java/porker/pp_legendarydungeons/mixin/dungeon_rules/DungeonExplosionMixin.java
```

Block and portable-item interactions:

```text
src/main/java/porker/pp_legendarydungeons/dungeon_rules/events/DungeonRuleInteractionEvents.java
```

Action checks use the action/block position, not merely a player scoreboard or tag. This handles a player standing outside and reaching inward, or standing inside and acting outside.

## Beds, PCs, healers, and portable access

Physical block access is controlled through block tags. Portable item access is controlled through item tags.

The action is cancelled even when the relevant block was already present before dungeon activation.

This intentionally does not globally block every Cobblemon healing operation, because potions and consumable healing items are intended to remain usable.

## Player pulse

The existing file:

```text
src/main/java/porker/pp_legendarydungeons/features/FeatureTicker.java
```

now calls:

```text
src/main/java/porker/pp_legendarydungeons/dungeon_rules/DungeonRulePlayerTracker.java
```

on its existing 30-tick cadence.

That player pulse:

- applies/refreshes Mining Fatigue when needed,
- adds/removes `pp_in_dungeon_zone`,
- renders builder preview particles for players who explicitly requested them.

The tag is informational and can support future dungeon scripting. It is not the authority for placement, breaking, or interaction checks.

## Completion and inactive zones

A completed or disabled instance is not added to the active spatial index. The controller records remain in the world and SavedData, allowing later reactivation without resaving structures.
