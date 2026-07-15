# Architectury Architecture Guide

## Purpose

This guide defines the expected architecture for all new development in
Cobblemon: Explore Legendary Dungeons.

The project supports Fabric and NeoForge through Architectury. The goal is not
to maintain two independent mods. The goal is to maintain one shared gameplay
codebase with two thin loader adapters.

## Module responsibility

### Common module

Use `common` by default.

```text
common/src/main/java/
common/src/main/resources/
common/src/test/
```

The common module should own:

- Gameplay logic.
- Registries and stable public IDs.
- Blocks, items, block entities, entities, services, and saved data.
- Dungeon controllers, protection rules, completion state, and persistence.
- Server-side packet handling and request validation.
- Cobblemon integrations that compile against the shared Cobblemon artifact.
- Architectury events, lifecycle callbacks, trade hooks, and networking.
- Shared mixins.
- Datapack and resource-pack content.
- Tests and reusable validation utilities.

A feature that can work through vanilla, Cobblemon common APIs, Architectury,
data files, or a shared mixin does not belong in both loader modules.

### Fabric module

The Fabric module should own only:

- Fabric main and client entrypoints.
- `fabric.mod.json`.
- Fabric development and runtime dependency declarations.
- Fabric-only initialization required by an API that Architectury cannot cover.
- Fabric-specific client wiring that cannot safely live in common.
- Fabric run configuration and release artifact assembly.

Loader entrypoints should call shared initialization and contain little or no
gameplay logic.

### NeoForge module

The NeoForge module should own only:

- NeoForge main and client entrypoints.
- `META-INF/neoforge.mods.toml`.
- NeoForge development and runtime dependency declarations.
- Distribution-safe client initialization.
- NeoForge-only events or adapters that Architectury cannot cover.
- NeoForge run configuration and release artifact assembly.

Do not copy Fabric gameplay code into NeoForge. Move the behavior into common
and keep only the unavoidable loader hook in each platform module.

## Decision process for a new feature

Before writing code, ask these questions in order:

1. Can this be implemented as a vanilla datapack/resource?
2. Can it use a vanilla shared Java API?
3. Can it use a Cobblemon shared/common API?
4. Does Architectury already expose the event, registry, networking, lifecycle,
   or trade hook?
5. Can one shared mixin target a stable vanilla or Cobblemon class?
6. Can the loader difference be represented by a tiny platform interface?
7. Only then: does each loader require its own implementation?

When a platform boundary is necessary:

- Keep the shared interface small.
- Pass plain shared-domain data across the boundary.
- Avoid leaking Fabric or NeoForge classes into common method signatures.
- Keep business rules in common.
- Test that both implementations produce the same visible result.

## Resource placement

Put loader-neutral resources in:

```text
common/src/main/resources/
```

Examples:

```text
assets/pp_legendarydungeons/
data/pp_legendarydungeons/function/
data/pp_legendarydungeons/tags/
data/pp_legendarydungeons/loot_table/
data/pp_legendarydungeons/structure/
data/pp_legendarydungeons/worldgen/
pp_legendarydungeons.mixins.json
```

Keep only loader metadata or deliberately loader-specific resources in
`fabric/src/main/resources` or `neoforge/src/main/resources`.

The platform build transforms and packages common resources into each release
JAR. Do not maintain duplicate copies of the same datapack tree under both
loaders.

## Server-authoritative design

The server must decide whether a gameplay action is valid and apply the final
state change.

For client-to-server requests, validate at least:

- The sender exists and is allowed to perform the action.
- The sender is in the expected dimension.
- The target still exists.
- The target is the expected block, entity, or block entity type.
- The sender is close enough to the target.
- The requested numbers are within safe bounds.
- The dungeon or controller is in a state where the action is legal.
- The action has not already completed or been consumed.

The client may preview or request a change, but should not be trusted to decide
persistent state.

## Multiplayer-friendly state

Prefer:

- Block entity data for state owned by one placed block.
- Level `SavedData` for persistent world-level collections.
- UUIDs for tracking a specific player, Pokémon, entity, or encounter.
- Per-dungeon or per-instance records for independent encounters.
- Explicit owner/actor parameters passed through a call chain.
- Server schedulers that store enough context to resume safely.

Avoid:

- Global mutable “current player,” “last player,” or “current dungeon” fields.
- Assuming the first player in a collection caused an event.
- One shared timer or lock for all dungeon instances.
- Client-side state as the only copy of a persistent rule.
- Static collections that retain world or entity objects after shutdown.
- Running the same registration or event subscription more than once.

## Scalability guidelines

- Avoid scanning every entity for every player every tick.
- Use bounded search radii, tags, indexes, block positions, or scheduled checks.
- Run slower systems at an appropriate interval rather than every tick.
- Keep one scheduler listener rather than registering a listener per task.
- Remove finished tasks and stale UUID records.
- Clamp loaded dimensions, radii, offsets, and priorities before using them.
- Treat malformed datapack entries independently so one bad file does not
  invalidate every valid entry.
- Replace reloadable registries atomically after successful parsing.
- Make reload and initialization logic idempotent.

## Persistence and compatibility

The following should be treated as public compatibility surfaces:

```text
pp_legendarydungeons
registry IDs
packet IDs
NBT keys
SavedData names
scoreboard objectives
entity tags
function IDs
loot-table IDs
structure IDs
map target IDs
```

Do not rename them during normal cleanup. A rename requires an explicit data
migration and compatibility plan.

When changing stored values:

- Preserve old keys when practical.
- Supply defaults for absent fields.
- Clamp unsafe values on both load and update.
- Test save, shutdown, restart, and reopen.
- Test copies of existing worlds rather than original worlds.

## Client/server separation

- Common initialization must be safe on a dedicated server.
- Client screens, renderers, keybindings, and client-only Minecraft classes
  must be initialized only from a client entrypoint or safe client boundary.
- Do not reference a client-only class from a common static initializer.
- Networking payload definitions may be shared, but screen opening and visual
  behavior must remain client-safe.
- A dedicated-server startup test is required after client-boundary changes.

## Testing expectations by change type

### Shared gameplay or data

Test:

- Fabric client.
- NeoForge client.
- Representative gameplay on both.
- `/reload` when data or registrations are affected.

### Networking, permissions, or persistence

Also test:

- Fabric dedicated server.
- NeoForge dedicated server.
- Save and restart.
- An ordinary player and an operator.
- Two players when simultaneous activity could matter.

### Loader-specific adapter

Test:

- The changed loader.
- The other loader as a regression check.
- Dedicated-server startup if client separation is involved.

### Worldgen or structures

Test:

- Fresh chunks.
- `/locate`.
- Internal loot and markers.
- Reload/restart behavior.
- A copied existing world when identifiers or saved data changed.

## Build outputs

From the repository root:

```powershell
.\gradlew.bat `
    clean `
    :common:build `
    :fabric:build `
    :neoforge:build `
    --no-daemon `
    --no-parallel `
    --max-workers=2 `
    --console=plain
```

Upload only the remapped platform JARs:

```text
fabric/build/libs/cobblemon-eld-1.1.0-fabric-mc1.21.1-cob1.7.3.jar
neoforge/build/libs/cobblemon-eld-1.1.0-neoforge-mc1.21.1-cob1.7.3.jar
```

Development slim, development shadow, sources, and common JARs are not player
release artifacts.

## Review checklist

Before accepting a new implementation:

- Is the gameplay logic in common?
- Are loader modules still thin?
- Is the server authoritative?
- Is state scoped to the correct world or instance?
- Are packet and persisted values validated and bounded?
- Can initialization or cleanup safely run twice?
- Does the implementation scale beyond one player?
- Are stable identifiers preserved?
- Does it start on a dedicated server?
- Does the same visible behavior occur on Fabric and NeoForge?
