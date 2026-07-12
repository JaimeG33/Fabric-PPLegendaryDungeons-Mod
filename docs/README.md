# Cobblemon: Explore Legendary Dungeons — Documentation

Release documentation for the Fabric 1.0.0 build targeting:

- Minecraft 1.21.1
- Cobblemon 1.6.1 through the 1.7.x line
- Fabric Loader 0.17.2 or newer
- Fabric API 0.116.6+1.21.1 or newer
- Java 21

## Start here

1. Read `RELEASE_CHECKLIST.md`.
2. Complete the two release-blocking Crystal Caves cleanup items.
3. Apply the metadata decisions in `development/PROJECT_IDENTIFIERS_AND_BUILD.md`.
4. Test the final JAR once with Cobblemon 1.6.1 and once with Cobblemon 1.7.3.
5. Keep the release artifact and its source/snapshot commit together.

## Documentation map

### Player-facing and gameplay systems

- `gameplay/MAPS.md`
- `gameplay/LOOT_INJECTIONS.md`
- `gameplay/TRADERS.md`
- `gameplay/LEGENDARY_SUMMONS_AND_DUNGEON_RULES.md`

### Development and extension

- `development/PROJECT_IDENTIFIERS_AND_BUILD.md`
- `development/WTRADER_JSON_SCHEMA.md`
- `development/ADDING_CONTENT.md`
- `COMPATIBILITY.md`

### Migration and cleanup

- `archive/README.md`

## Stable project identifiers

The public name and internal identifiers intentionally differ:

| Purpose | Value |
|---|---|
| Public display name | `Cobblemon: Explore Legendary Dungeons` |
| Fabric mod ID | `pp_legendarydungeons` |
| Java package | `porker.pp_legendarydungeons` |
| Main entrypoint | `porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons` |
| Client entrypoint | `porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeonsClient` |

The internal names are legacy identifiers. They should remain stable unless a deliberate migration is planned.
