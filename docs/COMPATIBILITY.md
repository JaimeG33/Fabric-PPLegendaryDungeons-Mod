# Compatibility

## Current declared baseline

| Component | Supported baseline |
|---|---|
| Mod version | `1.1.0` |
| Minecraft | `1.21.1` |
| Java | `21` |
| Cobblemon | `1.7.3+1.21.1` |
| Architectury API | `13.0.8` |
| Fabric Loader | `0.17.2` |
| Fabric API | `0.116.6+1.21.1` |
| Fabric Language Kotlin | `1.13.6+kotlin.2.2.20` |
| NeoForge | `21.1.214` |
| Kotlin for Forge | `5.10.0` |
| Accessories | `1.1.0-beta.52+1.21.1` |
| Mega Showdown | `1.6.7` or newer compatible 1.21.1 build |

The project compiles against Cobblemon `1.7.3+1.21.1`. The older statement that
the project compiles against Cobblemon 1.6.1 is no longer valid for the current
Architectury build.

## Loader support

The project publishes separate artifacts:

```text
Fabric:
cobblemon-eld-1.1.0-fabric-mc1.21.1-cob1.7.3.jar

NeoForge:
cobblemon-eld-1.1.0-neoforge-mc1.21.1-cob1.7.3.jar
```

Use the JAR that matches the installed loader. Do not place both project JARs in
the same instance.

Shared gameplay and resources are built from the common module. Loader-specific
modules provide metadata, dependencies, entrypoints, and minimal adapters.

## Cobblemon APIs used directly

The shared code uses Cobblemon APIs including Pokémon properties and entities,
battle-faint and loot-drop events, ownership/completion state, and shared
registries.

Any future Cobblemon update should be tested for:

- Changed event payloads or timing.
- Renamed or removed entity/property APIs.
- Ownership accessor changes.
- Loot event cancellation or ordering.
- Battle participant and killer attribution.
- Changes to published Fabric and NeoForge artifacts.

Do not broaden the declared Cobblemon range merely because the game starts.
Compile and smoke-test the exact target before changing metadata.

## Known caveats

### Multiplayer battle attribution

Some cooperative or multi-participant battle paths may require more precise
responsible-player attribution than selecting the first battle participant.
This remains a known hardening area.

### Modded drop cancellation

Another addon can cancel Cobblemon’s loot event. Depending on event ordering,
the additional Minecraft loot table may also be skipped. This project should
not remove or replace another addon’s drops.

### GUI scale

The zone editor may overflow at GUI scale 4. This is a known non-blocking UI
issue unless one loader behaves differently.

### Crystal Caves

Crystal Caves remains intentionally dormant. Source assets may exist, but the
dungeon should not be actively registered, mapped, or advertised until its
release work resumes.

## Compatibility confidence

A release is considered smoke-tested only after:

- The remapped Fabric JAR launches in a clean Fabric instance.
- The remapped NeoForge JAR launches in a clean NeoForge instance.
- Fresh worlds open on both.
- Dedicated servers start on both.
- Representative controllers and Rayquaza flows work.
- Metadata displays the intended version and dependencies.
- No blocking resource, registry, mixin, or datapack error appears.

Extended multiplayer, existing-world migration, dependency-floor, and
performance testing may remain documented as deferred rather than being
represented as passed.
