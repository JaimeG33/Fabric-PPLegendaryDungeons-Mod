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

## Factions and player relations — Step 1

- Built-in `kyogre_defenders` and `pirate_raiders` faction resources load.
- `/reload` replaces faction definitions without duplicate listener
  registration.
- An outside datapack can add a new `dungeon_factions` resource.
- An invalid self-hostile, duplicate, blank, or malformed faction definition is
  rejected without blocking valid factions.
- A dungeon Pokémon profile with no `faction` and no `player_relation` behaves
  exactly as it did before the faction patch.
- `player_relation: legacy` still respects `target_players` and explicit
  `player` target rules.
- `player_relation: hostile` can target an eligible Survival player even when
  the old `target_players` flag is false.
- `player_relation: neutral` does not proactively target an eligible Survival
  player even when an old player target entry remains in the profile.
- `player_relation: faction_retaliatory` behaves as neutral in Step 1.
- Two managed dungeon Pokémon in opposing built-in factions acquire each other
  when inside detection/chase limits.
- Two managed dungeon Pokémon in the same faction do not automatically target
  each other, including when a broad legacy non-player rule would match both.
- Legacy `entity_tag`, `scoreboard_team`, and `entity_type_tag` rules still
  target ordinary vanilla mobs.
- Ordinary pillagers, drowned, and guardians are not treated as faction members
  yet; reciprocal vanilla-mob faction targeting remains a Step 2 test.
- Same-faction damage cancellation and shared provocation remain Step 4 tests.

## Effects and capture

- Combat effects appear only while a valid target exists.
- Effects expire after disengagement.
- Normal capture succeeds when allowed.
- Captured Pokémon does not retain dungeon entity effects or hostility state.
- `allowed: false` prevents capture.
- Ceruledge with `wind_charged` produces one wind-charge death callback at the
  end of its death animation after a direct overworld kill.
- Ceruledge whose visible `wind_charged` duration expires during a long battle
  still produces one callback at the end of its death animation.
- Naganadel with `oozing` produces one delayed callback after direct and battle
  defeats.
- Debug logging reports callback arming, queueing, and triggering with deathTime
  near 59 rather than immediately when `LOOT_DROPPED` fires.
- `/kill`, player damage, projectile damage, and battle fainting never produce
  more than one callback for the same entity.
- Capturing, chunk unloading, or discarding a living dungeon Pokémon does not
  trigger a configured death callback.
- A profile can use a death callback without configuring supplemental loot.
- Additional and replacement loot each still execute once alongside the death
  callback.
- A custom registered effect with `death_callback: vanilla_killed` can fail
  without interrupting Cobblemon drops or dungeon loot.

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


## Team and spawn-status options

- `scoreboard_team: rayquaza_team` adds the Pokémon to that existing team.
- A missing configured team is created once with default team settings.
- Multiple Pokémon using the same configured team reuse it.
- Team membership survives chunk unload/reload and disappears normally on death.
- A one-entry Sleep pool always spawns the Pokémon asleep.
- Sleeping Pokémon use Cobblemon's sleep behavior/pose and enter battle asleep.
- Burn, Frozen, Paralysis, Poison, and Badly Poisoned enter battle with the
  configured status even when no special overworld idle pose is visible.
- A 6/2/2 Sleep/Paralysis/none pool statistically selects all three outcomes.
- A namespaced custom persistent status resolves when its provider is installed.
- An unavailable custom status logs a warning without crashing the server.
- Invalid team names, blank statuses, zero/negative weights, and oversized total
  weights reject only the malformed profile during datapack reload.
