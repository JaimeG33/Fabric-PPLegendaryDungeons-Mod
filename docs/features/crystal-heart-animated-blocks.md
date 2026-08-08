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

## High-resolution top / bottom texture variants

Three placeable variants still exist so the final art direction can be compared directly in game:

- `pp_legendarydungeons:crystal_heart` -> Candidate A
- `pp_legendarydungeons:crystal_heart_b` -> Candidate B
- `pp_legendarydungeons:crystal_heart_c` -> Candidate C

Each candidate now uses two independent 128x128 textures:

```text
crystal_heart_<variant>_top.png
crystal_heart_<variant>_bottom.png
```

The four upper physical faces share the top texture. The four lower physical faces share the bottom texture. Alternate sides mirror the UV coordinates and use subtle per-face tint differences so the rotating object still reads as a faceted gemstone.

This replaces the previous 64x32 eight-face atlas approach. The older atlas PNGs are currently left in the asset folder for comparison/history, but the renderer no longer references them.

Test commands:

```mcfunction
/give @s pp_legendarydungeons:crystal_heart
/give @s pp_legendarydungeons:crystal_heart_b
/give @s pp_legendarydungeons:crystal_heart_c
```

The original `crystal_heart` id is deliberately retained as Candidate A so existing structures/worlds using that id are not renamed.

## Render distance and culling

The renderer uses the player's configured chunk render distance rather than Minecraft's short default block-entity render distance. This does **not** force chunks to load; the crystal can only render when its chunk is available to the client.

The earlier calculated bounding box did not eliminate the look-angle disappearance. The current diagnostic therefore returns a very large finite vanilla `AABB` from `getRenderBoundingBox()` while continuing to return `true` from `shouldRenderOffScreen()`.

NeoForge exposes an `AABB.INFINITE` helper for this kind of renderer diagnostic, but that constant is loader-added and cannot be referenced from the shared `common` source set because Fabric/common compilation only sees the vanilla `AABB` API. The large finite box gives us the same practical culling test while remaining compile-safe on both loaders.

The diagnostic bounds do **not** force chunks to load and do not replace the renderer's configured view distance. If the look-angle disappearance remains after this change, the next investigation should focus on render-section/global block-entity handling rather than making the bounding box larger again.

## Texture / UV notes

The upper and lower textures are full-face assets rather than atlases. Each PNG dedicates almost the full 128x128 image to one triangular face, giving far more detail than the previous 16x16 atlas cells.

Current mapping:

```text
top texture:
        apex
         /\
        /  \
       /    \
 left /______\ right

bottom texture:
 left ________ right
       \    /
        \  /
         \/
        apex
```

The renderer uses a cutout/no-cull render type rather than translucent blending. The crystal can still look luminous or glassy through its colors, highlights, and internal facet patterns without the depth-sorting artifacts that can appear on a very large translucent object.

When editing one of the new textures:

- keep the important artwork inside the triangular region,
- keep edge highlights a few pixels away from the outermost texture border,
- design the top and bottom as a visual pair,
- remember that alternate sides mirror the texture horizontally,
- review `CrystalHeartGeometry` if the crystal shape itself changes.

## Future expansion

Potential later additions include:

- activation/inactive states,
- custom aura particles,
- dungeon progression hooks,
- Diancie-related interactions,
- lighting effects,
- other animated dungeon landmarks using the same folder/registration pattern.
