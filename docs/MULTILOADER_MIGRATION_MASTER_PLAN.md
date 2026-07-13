# Architectury Fabric + NeoForge Migration Master Plan

## 1. Purpose of this document

This is the authoritative high-level migration plan for **Cobblemon: Explore Legendary Dungeons**.

It is written to provide enough durable context that a future developer, assistant, or separate development chat can understand:

- What the project currently is.
- Why the migration is being performed.
- Which versions and dependencies define the baseline.
- Which systems exist in the current Fabric mod.
- Which systems should become shared Architectury code.
- Which systems should remain mixins or loader-specific.
- The intended final directory and build structure.
- The order of work and the tests required after every phase.
- The major risks that must not be lost between development sessions.

Each active phase also receives a focused document under `docs/migration/`.

---

## 2. Current project baseline

### Repository and branch baseline

- Repository: `JaimeG33/Fabric-PPLegendaryDungeons-Mod`
- Historical/default branch: `master`
- Migration work should occur on a separate branch.
- Baseline reviewed commit: `6d731a1c43cbd36b71f8f90a9989c68c40514fb2`
- Baseline commit description: `Tweaks to mega trade_pools`

The default branch must remain available as the last known single-module Fabric implementation until the multi-loader branch has passed parity testing.

### Version matrix

| Component | Baseline |
|---|---|
| Minecraft | `1.21.1` |
| Java | `21` |
| Cobblemon | `1.7.3+1.21.1` |
| Mega Showdown | `1.6.9+1.7.3+1.21.1` |
| Architectury API | `13.0.8` |
| Fabric Loader | `0.17.2` |
| Fabric API | `0.116.6+1.21.1` |
| Fabric Language Kotlin | `1.13.6+kotlin.2.2.20` |
| Accessories Fabric | `1.1.0-beta.52+1.21.1` |
| Initial NeoForge alignment target | `21.1.214` |
| Kotlin for Forge target | `5.10.0` |

Mega Showdown is a required runtime dependency. The project currently refers to Mega Showdown primarily through resource identifiers and data files rather than by importing its internal Java classes.

### Stable identifiers that must not change

- Public name: `Cobblemon: Explore Legendary Dungeons`
- Mod ID: `pp_legendarydungeons`
- Data/resource namespace: `pp_legendarydungeons`
- Java package: `porker.pp_legendarydungeons`
- Existing saved-data identifiers
- Existing scoreboards and command tags
- Existing payload/resource IDs
- Existing worldgen IDs and structure IDs
- Existing function and loot-table IDs

Changing these during the loader migration could break existing worlds, datapacks, structures, saved data, or third-party references.

---

## 3. Migration goals

### Primary goals

1. Produce separate, public Fabric and NeoForge JARs for Minecraft 1.21.1.
2. Keep one shared implementation of gameplay behavior wherever practical.
3. Preserve feature parity with the current Fabric version.
4. Preserve existing-world compatibility and all stable resource identifiers.
5. Use Architectury common APIs instead of maintaining duplicate Fabric and NeoForge implementations.
6. Keep loader modules thin: entrypoints, metadata, platform dependencies, and rare platform-only hooks.
7. Establish a repeatable build and test process for both loaders.
8. Make future features start in common code by default.

### Maintenance goals

- Avoid two copies of the same event, networking, registry, trade, or loot system.
- Make platform-specific code obvious by its directory.
- Keep each migration step independently testable.
- Commit each phase separately when practical.
- Document exact changed paths and parity checks for every phase.
- Use vanilla or Cobblemon shared APIs before Architectury, and Architectury before custom loader adapters.

### Non-goals

- Renaming the project namespace or Java package.
- Rewriting working data-driven content solely for stylistic reasons.
- Importing undocumented Mega Showdown internals.
- Changing gameplay balance during the loader migration.
- Expanding to additional Minecraft or Cobblemon versions during initial parity work.
- Replacing all mixins merely because Architectury is being adopted.
- Combining the two platform artifacts into one universal JAR.

---

## 4. Target architecture

The intended final project structure is:

```text
cobblemon-explore-legendary-dungeons/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│
├── common/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── java/
│       │   └── porker/pp_legendarydungeons/
│       └── resources/
│           ├── assets/pp_legendarydungeons/
│           ├── data/pp_legendarydungeons/
│           └── pp_legendarydungeons.mixins.json
│
├── fabric/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── java/
│       │   └── .../fabric/
│       └── resources/
│           └── fabric.mod.json
│
└── neoforge/
    ├── build.gradle.kts
    └── src/main/
        ├── java/
        │   └── .../neoforge/
        └── resources/
            └── META-INF/neoforge.mods.toml
```

### Common module responsibilities

The common module should eventually contain almost all project logic and resources:

- Dungeon rule models, persistence, managers, presets, and completion rules.
- Dungeon controller blocks and block entities.
- Server lifecycle behavior.
- Tick-driven feature, summon, secret, and item systems.
- WTrader data models, parsing, registries, profile selection, and offer generation.
- Cartographer trade injection logic.
- Additive Minecraft loot-table injection logic.
- Cobblemon Pokémon loot bridge.
- Map generation, target resolution, lore, and coordinate systems.
- Network payload records, codecs, validation, and handlers.
- Shared client screens and screen logic.
- Standard Minecraft datapack resources.
- Shared mixins that use vanilla/Cobblemon classes and are valid on both loaders.

### Fabric module responsibilities

- Fabric main entrypoint.
- Fabric client entrypoint.
- `fabric.mod.json`.
- Fabric Loader, Fabric API, Fabric Language Kotlin, Fabric Cobblemon, Architectury Fabric, Mega Showdown Fabric, and Accessories Fabric dependencies.
- Any genuinely Fabric-only compatibility hook that cannot be expressed through common APIs.

### NeoForge module responsibilities

- NeoForge `@Mod` entrypoint.
- NeoForge client bootstrap where necessary.
- `META-INF/neoforge.mods.toml`.
- NeoForge, Architectury NeoForge, NeoForge Cobblemon, Mega Showdown NeoForge, Accessories NeoForge, Kotlin for Forge, and required runtime libraries.
- Any genuinely NeoForge-only compatibility hook that cannot be expressed through common APIs.

---

## 5. API selection rule

For each system, use this priority:

1. Vanilla Minecraft API or standard datapack resources.
2. Cobblemon shared/common API.
3. Architectury common API.
4. A shared mixin against stable vanilla/Cobblemon classes.
5. `@ExpectPlatform` or a small custom platform interface.
6. Separate Fabric and NeoForge implementations only as a last resort.

The presence of Architectury does not mean every system must use Architectury. Standard resources and common Cobblemon events are already multi-loader-friendly.

---

## 6. Current major systems and intended treatment

### 6.1 Main and client entrypoints

Current state:

- Main class implements Fabric `ModInitializer`.
- Client class implements Fabric `ClientModInitializer`.

Target:

- Shared `LegendaryDungeons.init()` method in common code.
- Shared `LegendaryDungeonsClient.init()` method for client-safe common registration.
- Tiny Fabric entrypoints call those methods.
- Tiny NeoForge entrypoint calls the same common methods at the correct lifecycle points.

### 6.2 Lifecycle events

Current state:

- Fabric `ServerLifecycleEvents.SERVER_STARTED`
- Fabric `ServerLifecycleEvents.SERVER_STOPPED`

Target:

- Architectury `LifecycleEvent.SERVER_STARTED`
- Architectury `LifecycleEvent.SERVER_STOPPED`

Shared behavior includes scoreboard setup, dungeon manager bootstrap, preview cleanup, and temporary Pokémon battle context cleanup.

### 6.3 Server tick systems

Current systems:

- `EntityLegendarySummonTicker`
- `FeatureTicker`
- `ItemGimmickTicker`
- `SecretTicker`

Target:

- Architectury `TickEvent.SERVER_POST`
- Prefer one shared scheduler that invokes each subsystem at its existing interval.
- Preserve behavior and interval timing before considering performance redesigns.

### 6.4 WTrader JSON reload

Current state:

- Fabric resource-manager reload registration.
- Loader-neutral JSON parsing and registry replacement.

Target:

- Architectury `ReloadListenerRegistry`.
- Keep all JSON schemas, parsers, validators, and registries in common code.

### 6.5 Cartographer trade injection

Current state:

- Fabric `TradeOfferHelper`.
- Loader-neutral trade construction and data-driven pools.

Target:

- Architectury `TradeRegistry`.
- Preserve novice/master levels, candidate behavior, pricing, uses, XP, and map generation.

### 6.6 Minecraft loot-table injection

Current state:

- Fabric loot-table modification event.
- Adds nested pools to selected built-in tables.

Target:

- Architectury `LootEvent.MODIFY_LOOT_TABLE`.
- Preserve additive behavior.
- Do not replace vanilla or modded tables.
- Preserve built-in-only handling so user datapack overrides remain authoritative.
- Do not create separate NeoForge Global Loot Modifiers unless the Architectury event fails a required parity test.

### 6.7 Cobblemon Pokémon loot bridge

Current state:

- Uses Cobblemon common events including battle-fainted and loot-dropped events.
- Executes additional Minecraft loot tables without replacing native drops.

Target:

- Keep in common code.
- No loader-specific rewrite expected.
- Retain battle attribution fallback and temporary context cleanup.
- Test direct player kills, standard wild battles, multiplayer battles, and mod-cancelled drops.

### 6.8 Interaction restrictions

Current state:

- Fabric block/item interaction callbacks prevent forbidden portable and placed healing/PC/bed use in protected dungeons.
- Controller editor opening also uses interaction callbacks.

Target:

- Architectury `InteractionEvent.RIGHT_CLICK_BLOCK` and `RIGHT_CLICK_ITEM` for general restrictions.
- Consider moving controller block editor opening into the custom block class itself through vanilla block interaction overrides.
- Preserve bypass permissions and feedback messages.

### 6.9 Block breaking and placement

Current state:

- Mixins intercept block breaking and block-item placement.

Target:

- Replace with Architectury `BlockEvent.BREAK` and `BlockEvent.PLACE` if parity tests pass.
- Verify doors, beds, torches, wall-attached blocks, waterlogged blocks, multi-block placement, boundary positions, allowlist tags, creative bypass, and non-player placement.
- Remove the old mixins only after parity is confirmed.

### 6.10 Explosion protection

Current state:

- Shared mixin can cancel explosions originating in protected areas.
- It can also selectively remove protected block positions from explosions originating outside the area.

Target:

- Keep the shared mixin unless a common API exposes equivalent mutable affected-block behavior.
- Do not simplify this into cancellation of all nearby explosions if that changes entity damage or outside-dungeon behavior.

### 6.11 Natural wandering trader replacement

Current state:

- Shared mixin runs after vanilla wandering trader offers are generated.
- Replacement is limited to the naturally spawned trader tracked by vanilla.
- The service may preserve vanilla offers or replace the entire set with a data-driven profile.

Target:

- Keep the shared mixin and service.
- Generic trade registration does not reproduce the exact natural-spawn and replacement semantics.

### 6.12 Blocks, block items, and block entities

Current state:

- Direct vanilla registry calls initialized from the Fabric entrypoint.

Target:

- Architectury `DeferredRegister`.
- Architectury `RegistrySupplier`.
- One shared registry implementation.
- Expect a compile-error cascade because references become supplier `.get()` calls.
- Preserve all registered resource IDs exactly.

### 6.13 Networking

Current state:

- Fabric payload registration, receivers, and sending.
- Payload records already use Minecraft `CustomPacketPayload` and `StreamCodec`.

Target:

- Architectury `NetworkManager`.
- Keep payload IDs, payload records, codecs, permission checks, distance checks, block-entity synchronization, screen data, and handlers common.
- Keep client bootstrap isolated so dedicated servers never load client-only classes.

### 6.14 Standard resources and worldgen

The following should remain shared with little or no modification:

- Structures and structure sets.
- Template pools and NBT structures.
- Loot tables.
- Functions and function tags.
- Item/block/entity tags.
- Models, textures, language files, and icon.
- WTrader JSON resources.
- Mega Showdown item IDs, tags, structure IDs, and trade resources.
- Map target data.

### 6.15 Mega Showdown dependency

Current policy:

- Required in loader metadata.
- Runtime-only in Gradle because the project does not import Mega Showdown Java classes.
- Architectury and Accessories are also required by Mega Showdown.

Target policy:

- Continue using resource IDs and data-driven integration.
- Avoid importing undocumented Mega Showdown implementation classes.
- Declare Architectury directly because this mod itself will use it.
- Keep platform-specific Mega Showdown artifacts in the corresponding platform module.
- Use a deliberately tested version range rather than assuming future Mega Showdown versions are compatible.

---

## 7. Migration phases

### Phase 0 — Branch and clean baseline

Goals:

- Protect `master`.
- Create and push a dedicated migration branch.
- Confirm the baseline builds.
- Remove stale references that no longer resolve.
- Record versions and known behavior.
- Avoid committing local IntelliJ workspace state.

Known cleanup:

- Remove the nonexistent `pp_legendarydungeons:other/maps/voucher_check` entry from `src/main/resources/data/pp_legendarydungeons/tags/function/tick.json`.

Completion criteria:

- Migration branch exists locally and on GitHub.
- `master` remains unchanged.
- `git status` is clean after the baseline commit.
- Fabric client and build still work.

### Phase 1 — Architectury as a direct dependency

Goals:

- Change Architectury Fabric from runtime-only to compile/runtime implementation.
- Declare Architectury in `fabric.mod.json`.
- Do not change gameplay registration yet.
- Establish the documentation framework.

Completion criteria:

- Gradle sync succeeds.
- Java compilation succeeds.
- Fabric client starts.
- Existing behavior remains unchanged.
- Architectury appears as a direct dependency of this mod.

### Phase 2 — Low-risk common event foundation

Scope:

- Shared common initializer.
- Lifecycle events.
- Consolidated server tick scheduler.
- WTrader reload listener.
- Cartographer trade injection.
- Minecraft loot-table injection.

Completion criteria:

- All direct Fabric imports for these systems are gone.
- Fabric behavior matches the baseline.
- Reload, trade, loot, startup, shutdown, and ticker tests pass.

### Phase 3 — Interaction and protection events

Scope:

- Portable PC/healer restrictions.
- Bed, PC block, and healer block restrictions.
- Controller block interaction cleanup.
- Block break and block placement Architectury events.
- Removal of break/place mixins after parity verification.

Completion criteria:

- All restriction and bypass cases match baseline behavior.
- No client/server desynchronization.
- Multi-block and boundary placement tests pass.

### Phase 4 — Shared deferred registries

Scope:

- Blocks.
- Block items.
- Block entities.
- All call sites updated from direct instances to registry suppliers.

Completion criteria:

- Registry IDs are unchanged.
- Existing worlds load.
- Controller blocks and block entities serialize and deserialize.
- Client rendering and interaction still work.

### Phase 5 — Shared networking and client boundary

Scope:

- Common Architectury payload registration.
- Common C2S/S2C sending and receiving.
- Shared payload records and codecs.
- Thin platform client bootstraps.
- Dedicated-server safety.

Completion criteria:

- Both editor screens open and save.
- Preview and block updates synchronize.
- Invalid or unauthorized packets are rejected.
- Dedicated Fabric server starts without client-class loading errors.

### Phase 6 — Complete Fabric regression checkpoint

Scope:

- Test the entire mod while it is still structurally Fabric-only but internally using common Architectury APIs.

Required areas:

- Startup/shutdown.
- Existing-world loading.
- Dungeon rules and persistence.
- Editor screens and networking.
- Block protection.
- Summons and secrets.
- Static features.
- Item gimmicks and maps.
- WTrader reloads and natural replacement.
- Cartographer trades.
- Minecraft loot injection.
- Pokémon loot bridge.
- Mega Showdown items and structures.
- Dedicated server.

Completion criteria:

- A known-good Fabric migration checkpoint is tagged or committed before module splitting.

### Phase 7 — Split into `common`, `fabric`, and `neoforge`

Scope:

- Root multi-project Gradle setup.
- Move shared Java/resources to `common`.
- Add Fabric transformed-common build.
- Add NeoForge transformed-common build.
- Create minimal platform entrypoints and metadata.

Completion criteria:

- `:fabric:build` succeeds.
- `:neoforge:build` succeeds or reaches only documented platform integration issues.
- No duplicate shared resources.
- Final JARs have distinct platform names.

### Phase 8 — NeoForge bootstrap and dependency integration

Scope:

- NeoForge entrypoint lifecycle.
- NeoForge metadata.
- Architectury NeoForge.
- Cobblemon NeoForge.
- Mega Showdown NeoForge.
- Accessories NeoForge.
- Kotlin for Forge and required Mega Showdown runtime libraries.
- Any unavoidable NeoForge-specific hooks.

Completion criteria:

- NeoForge client reaches title screen.
- Fresh world loads.
- Dedicated NeoForge server starts.
- Mod dependencies and version errors are clear and correct.

### Phase 9 — Cross-loader parity and hardening

Test matrix:

- Fabric client.
- Fabric dedicated server.
- NeoForge client.
- NeoForge dedicated server.
- Fresh world.
- Existing-world copy.
- Singleplayer.
- Multiplayer.
- Correct dependencies.
- Missing/wrong dependency versions.
- Resource reload.
- Mixed addon environment where practical.

Completion criteria:

- Same gameplay outcomes on both loaders.
- Known differences are documented and justified.
- No loader-specific data loss.
- No major warnings or startup errors.

### Phase 10 — Release and maintenance workflow

Scope:

- Platform-qualified artifact names.
- Release metadata.
- CurseForge/Modrinth dependency declarations.
- Changelog.
- Source and license checks.
- CI or scripted build verification.
- Long-term phase documentation.

Completion criteria:

- Fabric and NeoForge release JARs are reproducible.
- Both are tested against the declared dependency matrix.
- Upload pages clearly list required dependencies.
- The master plan and phase docs reflect the released architecture.

---

## 8. Testing philosophy

Every phase must pass three levels:

### Compile/build

- Gradle sync.
- Java compilation.
- Unit tests where available.
- Clean remapped JAR build.

### Startup

- Fabric client.
- Fabric dedicated server.
- Later: NeoForge client and dedicated server.

### Feature parity

Test the systems changed by the phase plus a small smoke test of unrelated critical features. A successful build alone does not prove parity.

Never remove the old implementation and the fallback in the same untested edit. Replace one boundary, test it, then delete the obsolete hook.

---

## 9. Source-control strategy

Recommended branch:

```text
architectury-multiloader-migration
```

Recommended commit style:

```text
Clean migration baseline
Add Architectury API as direct dependency
Migrate lifecycle and server tick events to Architectury
Migrate reload, trade, and loot hooks to Architectury
Migrate dungeon interaction and block protection events
Convert dungeon registries to Architectury DeferredRegister
Migrate dungeon networking to Architectury
Create common, fabric, and neoforge modules
Bootstrap NeoForge runtime dependencies
Complete cross-loader parity fixes
```

Do not merge into `master` until the user deliberately decides the multi-loader branch is ready.

---

## 10. Important risks

1. **Registry timing and supplier conversion**
   - Most invasive compile-time change.
   - Existing resource IDs must remain identical.

2. **Dedicated-server client isolation**
   - Screens and Minecraft client classes must never load from common server initialization.

3. **Networking parity**
   - Registration phase and thread context differ by loader.
   - Validation must remain server-authoritative.

4. **Mixin compatibility**
   - Shared mixins must be tested on both transformed platform outputs.
   - Keep the explosion and natural WTrader mixins unless equivalent shared behavior is proven.

5. **Mega Showdown dependency chain**
   - NeoForge requires its platform-specific artifact and runtime libraries.
   - Do not assume Fabric dependency declarations translate directly.

6. **Existing-world compatibility**
   - Block and block-entity IDs, saved-data IDs, and serialization must not change.

7. **Documentation drift**
   - Older 1.6.1-era pages may conflict with the current 1.7.3 baseline.
   - Newest migration documents are authoritative until older pages are updated.

8. **Over-broad refactoring**
   - Do not mix gameplay balancing, naming migrations, or unrelated cleanups into loader-port commits.

---

## 11. Definition of project success

The migration is complete when:

- The repository builds separate Fabric and NeoForge JARs.
- Both JARs require and run with the appropriate Cobblemon, Mega Showdown, Architectury, Accessories, and Kotlin dependencies.
- The current Fabric feature set works on NeoForge with equivalent gameplay behavior.
- Shared gameplay logic is maintained once in `common`.
- Platform modules remain small and understandable.
- Existing Fabric worlds continue to load without identifier migration.
- The project documentation contains an accurate master plan and completed phase records.
