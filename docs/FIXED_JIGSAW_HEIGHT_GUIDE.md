# Fixed Jigsaw: Rotation and Surface-Clamped Height Guide

This guide applies to:

```text
pp_legendarydungeons:fixed_jigsaw
```

The custom type keeps vanilla jigsaw assembly, but adds two groups of controls:

1. Fixed root rotation.
2. Surface placement with a minimum clearance below the dimension ceiling.

Child jigsaw pieces still use vanilla connector rotation.

## Sky Pillar configuration

```json
{
  "type": "pp_legendarydungeons:fixed_jigsaw",
  "biomes": "#pp_legendarydungeons:topofmountains",
  "step": "surface_structures",
  "terrain_adaptation": "beard_box",
  "spawn_overrides": {},
  "start_pool": "pp_legendarydungeons:rt/sky_pillar",
  "size": 2,
  "start_height": {
    "absolute": -2
  },
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "minimum_clearance_below_top": 175,
  "terrain_search_radius": 32,
  "terrain_search_step": 8,
  "max_distance_from_center": 100,
  "use_expansion_hack": false,
  "rotation": "none"
}
```

For a normal Overworld with usable block Y values ending at 319:

```text
maximum root start Y = 319 - 175 = 144
```

The Sky Pillar is about 155 blocks tall, leaving roughly 20 blocks of extra safety.

## Placement algorithm

The custom type performs these steps:

1. Vanilla jigsaw generation selects and connects all pieces.
2. Vanilla projects the root onto the requested heightmap.
3. If the projected root is at or below the configured height cap, placement is unchanged.
4. If the projected root is too high, the mod checks deterministic nearby candidates.
5. Candidates outside the structure biome are ignored.
6. Candidates are ranked by distance and the terrain-height range under a 3x3 foundation sample.
7. The best valid lower candidate is used.
8. If no nearby lower candidate exists, the original structure is shifted straight downward to the cap.
9. A final actual-bounding-box check moves the whole structure down farther if any generated piece would exceed the dimension ceiling.
10. Jigsaw junction coordinates are moved with the pieces so `beard_box` terrain adaptation follows the relocated tower.

The structure start is never rejected merely because the mountain is too tall. This preserves the structure-set spacing and effective frequency.

## Custom height fields

### `minimum_clearance_below_top`

```json
"minimum_clearance_below_top": 175
```

The root ground/start Y may never be closer than this many blocks to the top usable block of the dimension.

Default: `175`

This default applies to every structure using `pp_legendarydungeons:fixed_jigsaw`; smaller structures should override it with a smaller value.

Examples:

```json
"minimum_clearance_below_top": 175
```

Suitable for the approximately 155-block Sky Pillar.

```json
"minimum_clearance_below_top": 80
```

Suitable for a smaller tall structure.

```json
"minimum_clearance_below_top": 0
```

Disables the extra root-start clearance cap. The final actual-bounding-box safety still prevents clipping.

Because this is relative to the dimension ceiling, it can be reused in dimensions with different build heights.

### `terrain_search_radius`

```json
"terrain_search_radius": 32
```

When the original surface is too high, this is the maximum horizontal distance the completed structure may be shifted while looking for lower ground.

Default: `32`

Use `0` to disable nearby searching and use vertical clamping only.

Recommended range: `16` to `48`.

### `terrain_search_step`

```json
"terrain_search_step": 8
```

This is the spacing between candidate locations inside the search radius.

Default: `8`

Smaller values search more locations and are more precise, but perform more terrain height queries. The step must be less than or equal to the radius unless the radius is zero.

## Vanilla height fields

### Surface-relative placement

```json
"start_height": {
  "absolute": -2
},
"project_start_to_heightmap": "WORLD_SURFACE_WG"
```

With heightmap projection present, the sampled `start_height` becomes an offset from the terrain.

- `0` places the root at the sampled surface.
- `-2` sinks it approximately two blocks into the surface.
- `3` lifts it approximately three blocks above the surface.

Do not use a large `below_top` value as `start_height` while heightmap projection is enabled. It would become a large offset added to the terrain height.

### Heightmap choices

For normal land structures:

```json
"project_start_to_heightmap": "WORLD_SURFACE_WG"
```

For structures that should use solid terrain beneath water:

```json
"project_start_to_heightmap": "OCEAN_FLOOR_WG"
```

Nearby searching only runs when `project_start_to_heightmap` is present. Without a heightmap, the system can still clamp the already-generated structure vertically.

## Rotation field

```json
"rotation": "none"
```

Supported values:

```text
none
clockwise_90
clockwise_180
counterclockwise_90
random
```

Only the root rotation is forced. Child pieces retain vanilla jigsaw connection behavior.

## Reusing this for another structure

```json
{
  "type": "pp_legendarydungeons:fixed_jigsaw",
  "biomes": "#your_namespace:your_biomes",
  "step": "surface_structures",
  "terrain_adaptation": "beard_box",
  "spawn_overrides": {},
  "start_pool": "your_namespace:your_pool",
  "size": 1,
  "start_height": {
    "absolute": -1
  },
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "minimum_clearance_below_top": 96,
  "terrain_search_radius": 24,
  "terrain_search_step": 8,
  "max_distance_from_center": 80,
  "use_expansion_hack": false,
  "rotation": "none"
}
```

Choose `minimum_clearance_below_top` using:

```text
structure height + desired safety margin
```

Example:

```text
Structure height: 70
Safety margin:    15
Clearance:        85
```

Then use:

```json
"minimum_clearance_below_top": 85
```

The final generated-bounding-box safety remains useful when jigsaw variants differ in height.

## What does not change

This patch does not change:

- Structure-set spacing, separation, or salt.
- The start pool.
- Pool weights.
- Jigsaw connector matching.
- Child-piece rotation.
- Existing generated structures.
- Dungeon-rule-zone behavior.

Only newly generated structures in newly generated chunks use the new placement logic.

## Testing checklist

1. Back up the test world.
2. Build both loaders.
3. Generate new chunks containing Sky Pillars.
4. Check ordinary mountains: the tower should remain surface-aligned.
5. Check very high mountains: the tower should move to a nearby lower valid site or shift downward.
6. Verify the complete top and Rayquaza summon armor stand exist.
7. Confirm the tower keeps the fixed orientation.
8. Confirm both dungeon-rule zones still cover the tower.
9. Restart the world and retest the generated tower.
10. Test several seeds and several towers.

Build commands:

```powershell
.\gradlew.bat clean :fabric:build :neoforge:build
```

```powershell
.\gradlew.bat :fabric:runClient
```

```powershell
.\gradlew.bat :neoforge:runClient
```
