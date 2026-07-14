# Phase 8 Validation Results

## Result

**PASS**

- Validated branch: `architectury-multiloader-migration`
- Validated branch head: `d7038af0fff54831f10e83c277cc92bc0a45ac64`
- Administrative record prepared: `2026-07-14`
- Minecraft: `1.21.1`
- Java: `21`
- NeoForge: `21.1.214`
- Cobblemon: `1.7.3+1.21.1`
- Development Mega Showdown: `1.6.9+1.7.3+1.21.1`
- Additional compatibility tested: `1.6.7+1.7.3+1.21.1`

This file records the user-performed Phase 8 checks summarized in the migration
handoff. It does not claim that this documentation review reran the game.

## NeoForge client checks

| Check | Result |
|---|---|
| NeoForge client reaches title screen | PASS |
| Mod appears in the mod list | PASS |
| NeoForge mod-list icon appears | PASS |
| Fresh world loads | PASS |
| Controller blocks register | PASS |
| Parent controller screen opens and saves | PASS |
| Zone controller screen opens and saves | PASS |
| No fatal registry, mixin, packet, or wrong-environment class-loading failure | PASS |

## NeoForge dedicated-server and networking checks

| Check | Result |
|---|---|
| Dedicated server reaches ready state | PASS |
| NeoForge client connects | PASS |
| No registry or packet mismatch | PASS |
| Parent and zone editor networking works | PASS |
| Settings survive disconnect and reconnect | PASS |
| Settings survive full server stop and restart | PASS |
| Dedicated server avoids client-class loading | PASS |
| Server shuts down cleanly after saving | PASS |

## Artifact and dependency checks

| Check | Result |
|---|---|
| NeoForge release JAR builds | PASS |
| JAR contains `META-INF/neoforge.mods.toml` | PASS |
| JAR contains `icon.png` | PASS |
| JAR does not contain `fabric.mod.json` | PASS |
| Missing dependencies produce a clear loader message | PASS |
| Mega Showdown `1.6.7+1.7.3+1.21.1` compatibility | PASS |
| Mega Showdown `1.6.9+1.7.3+1.21.1` development baseline | PASS |

## Runtime corrections retained

- NeoForge feature metadata uses `[features.pp_legendarydungeons]`.
- NeoForge development runtime includes the required Endec adapters and Jankson.
- NeoForge metadata declares `logoFile = "icon.png"`.
- Fabric and NeoForge metadata accept Mega Showdown `1.6.7` or newer.
- `gradle.properties` continues to use Mega Showdown `1.6.9` for the normal
  development environment.

## Deferred non-blocking issue

At GUI scale 4, or some Auto scale and resolution combinations, the zone editor
can extend below the visible screen. This is deferred and does not block the
multi-loader migration unless Fabric and NeoForge behave differently.
