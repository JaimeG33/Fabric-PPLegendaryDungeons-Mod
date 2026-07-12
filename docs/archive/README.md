# Documentation Archive and Replacement Plan

The repository accumulated implementation handoffs, patch instructions, drafts, and pre-release design notes. They are useful history but should not be the primary 1.0.0 documentation.

## Replace with the new release docs

| Existing document | Problem | Replacement |
|---|---|---|
| `README_LOOT_TABLE_INJECTIONS.md` | Lists old target sets, fixed chances, only two groups, and says Pokémon injection is not implemented | `gameplay/LOOT_INJECTIONS.md` |
| `README_VILLAGER_AND_NATURAL_WTRADER_INJECTIONS.md` | Patch-oriented wording and stale original cartographer prices | `gameplay/TRADERS.md` |
| `README_MAP_GENERATION_AND_RANDOM_MAPS.md` | Still describes a 50/50 Sky Pillar/Crystal Caves random dungeon table | `gameplay/MAPS.md` |
| `README_MAP_LORE_AND_COORDINATES.md` | Still lists Crystal Caves as an active target | `gameplay/MAPS.md` |
| `wtrader_json_schema.md` | Calls the implemented system “planned” and contains an active-looking Crystal Caves profile example | `development/WTRADER_JSON_SCHEMA.md` |

## Move historical implementation folders under an archive

Known candidates:

```text
docs/draft_map_profiles/
docs/wtrader_map_porting/
docs/wtrader_random_profiles_include_any/
docs/wtrader_random_map_odds_fix/
docs/examples/wtraders/
```

These can remain in Git history or under:

```text
docs/archive/legacy_handoffs/
docs/archive/drafts/
```

Do not leave disabled Crystal Caves examples beside active runtime documentation without an explicit `DISABLED` notice.

## Dungeon-rule documents

The existing detailed dungeon-rule documents remain technically useful:

```text
docs/dungeon_rules/README_BLOCKS_AND_ZONES.md
docs/dungeon_rules/README_RUNTIME_AND_RULES.md
docs/dungeon_rules/README_UI_AND_BUILDING.md
docs/dungeon_rules/README_DUNGEON_COMPLETION.md
docs/dungeon_rules/README_LEGENDARY_SUMMON_INTEGRATION.md
```

For a cleaner repository, keep them under:

```text
docs/development/dungeon_rules/
```

and link them from the root documentation index. The consolidated gameplay overview is:

```text
gameplay/LEGENDARY_SUMMONS_AND_DUNGEON_RULES.md
```

## Source-tree README files

A README under a Java source package can be overlooked by users and may imply that implementation is still provisional. Move long-form user/developer documentation from:

```text
src/main/java/.../README.md
```

into the central `docs/development/` hierarchy, leaving only short package-level comments in source code.

## Archive rule

A document belongs in active docs only when it describes the current runtime behavior. Anything that says “planned,” “patch,” “first test,” “temporary diagnostic,” or references removed content should be updated or archived.
