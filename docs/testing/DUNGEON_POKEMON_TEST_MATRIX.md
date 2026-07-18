# Dungeon Pokémon Test Matrix

## Build and startup

- `:common:build`
- `:fabric:build`
- `:neoforge:build`
- Fabric client startup
- NeoForge client startup
- Fabric dedicated server startup
- NeoForge dedicated server startup

## Profile loading

- Built-in spawn and dungeon profiles load on startup.
- `/reload` replaces registries without duplicate listener registration.
- A malformed profile logs an error without blocking valid profiles.
- An outside datapack adds a new profile.
- An outside datapack overrides a built-in profile at the same resource ID.

## Existing legendary regression

- Normal Rayquaza condition still summons level 70 Rayquaza.
- Secret condition still produces shiny, perfect-IV Rayquaza.
- Aftermath tags, sounds, effects, one-time locks, and completion tracking work.
- Removing or corrupting a built-in profile uses the code fallback.

## Marker spawning

- One `pp_dungeon_pokemon` marker spawns exactly one Pokémon.
- The marker remains after a failed profile lookup.
- The marker is discarded only after success.
- Multiple markers near one `pp_feature` marker all resolve once.
- Two structures active for different players do not share state.

## Persistence

- Move more than the normal despawn distance away and return.
- Unload and reload the chunk.
- Save, stop, and restart the server.
- Send the entity through a portal if that is supported by the encounter.
- Confirm profile ID, home position, and instance link survive.
- Confirm the Pokémon does not consume Cobblemon's natural spawn cap.

## Aggression

- Survival player inside detection range becomes the target.
- Creative and spectator exclusions work.
- Target remains stable while valid.
- Target outside chase range is released.
- Pokémon returns toward home after disengaging.
- Battle or busy state pauses overworld targeting.
- Dungeon completion/inactive instance pauses profiles that require it.

## Effects and capture

- Combat effects appear only while a valid target exists.
- Effects expire after disengagement.
- Normal capture succeeds when allowed.
- Captured Pokémon does not retain dungeon entity effects or hostility state.
- `allowed: false` prevents capture.

## Optional Fight or Flight

- Not installed.
- Installed with global proactive aggression disabled.
- Installed with global proactive aggression enabled.
- Forced-battle-on-damage disabled.
- Forced-battle-on-damage enabled and documented behavior accepted.
- No mixin conflict during client or dedicated-server startup.

## Load testing

Test these counts in one loaded dungeon area:

```text
1
10
40
80
```

For each count, record:

- Dedicated-server MSPT/tick health.
- Client FPS.
- Memory use.
- Network behavior with two players.
- Performance with and without Fight or Flight.
- Simultaneous pathfinding after all Pokémon acquire targets.
