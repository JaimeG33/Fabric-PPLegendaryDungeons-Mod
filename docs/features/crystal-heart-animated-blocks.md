# Crystal Heart animated block system

## Purpose

The Crystal Heart is an animated block-entity landmark intended for large dungeon set pieces such as the Crystal Cave heart chamber. It borrows the visual idea of an End Crystal without inheriting End Crystal gameplay behavior.

It is implemented as a **block + block entity + block-entity renderer** rather than a standalone entity. The placed block acts as an invisible anchor while the client renderer draws the large floating crystal.

## Why a block entity

A normal block model is not a good fit because the Crystal Heart:

- is much larger than one block,
- needs smooth continuous rotation,
- needs a subtle hover/bob animation,
- supports different dimensions without creating a new model for every size.

A full custom entity is also unnecessary at this stage because the heart is a fixed dungeon landmark rather than a moving or combat-capable creature/object.

## Code organization

```text
common/src/main/java/porker/pp_legendarydungeons/
├─ blocks/animated_blocks/crystal_heart/
├─ blocks/entity/animated_blocks/crystal_heart/
├─ client/animated_blocks/
│  └─ crystal_heart/
└─ setup/
```

This layout is intentionally reusable for future animated block entities.

## Size control

Each Crystal Heart block entity stores two NBT values:

- `CrystalWidth`
- `CrystalHeight`

Example:

```mcfunction
/data merge block X Y Z {CrystalWidth:5.5f,CrystalHeight:12.0f}
```

Structure blocks preserve block-entity NBT, so a dungeon structure can save its intended heart dimensions without another registered block/model combination.

## Animation

Animation is client-side and is calculated from world time. The renderer currently provides:

- slow Y-axis rotation,
- subtle sine-wave vertical bobbing.

No server-side animation tick or per-frame network packet is required.

## Per-face texture atlas variants

Three placeable variants currently exist so the final art direction can be tested directly in game:

- `pp_legendarydungeons:crystal_heart` -> Candidate A
- `pp_legendarydungeons:crystal_heart_b` -> Candidate B
- `pp_legendarydungeons:crystal_heart_c` -> Candidate C

All three share the same block entity type, geometry, scale system, animation, and renderer. They differ only in which 64x32 face atlas is selected.

Each atlas contains eight independent triangular face regions rather than one square image stretched around the whole crystal:

```text
upper 0 | upper 1 | upper 2 | upper 3
lower 0 | lower 1 | lower 2 | lower 3
```

Each atlas cell is 16x16 pixels. `CrystalHeartGeometry` maps one physical triangular face to each cell and applies a small per-face tint so neighboring facets remain visually distinct while the crystal rotates.

Test commands:

```mcfunction
/give @s pp_legendarydungeons:crystal_heart
/give @s pp_legendarydungeons:crystal_heart_b
/give @s pp_legendarydungeons:crystal_heart_c
```

The original `crystal_heart` id is deliberately retained as Candidate A so existing structures/worlds using that id are not renamed.

## Render distance and culling

The renderer uses the player's configured chunk render distance rather than Minecraft's short default block-entity render distance. This does **not** force chunks to load; the crystal can only render when its chunk is available to the client.

Because the visible crystal can be much larger than its one-block anchor, the renderer also calculates a render bounding box from the stored width and height. The horizontal radius includes a `sqrt(2)` allowance for Y-axis rotation, while the vertical bounds include the bobbing amplitude and a small safety margin.

This prevents camera-frustum culling from treating the invisible one-block anchor as if it were the entire rendered object.

## Texture / UV notes

The original proof-of-concept texture was stretched across each long triangular face, which made it resemble an enlarged block texture. The current implementation separates geometry positions from UV coordinates so every face can use a purpose-built section of the atlas.

The renderer also uses a cutout/no-cull render type rather than translucent blending. The crystal can still look luminous or glassy through its colors, highlights, and internal facet patterns without the depth-sorting artifacts that can appear on a very large translucent object.

When editing an atlas:

- keep each face inside its 16x16 cell,
- leave a small margin near the cell boundary to reduce texture sampling bleed,
- review `CrystalHeartGeometry` whenever the atlas layout or crystal shape changes.

## Future expansion

Potential later additions include:

- activation/inactive states,
- custom aura particles,
- dungeon progression hooks,
- Diancie-related interactions,
- lighting effects,
- other animated dungeon landmarks using the same folder/registration pattern.
