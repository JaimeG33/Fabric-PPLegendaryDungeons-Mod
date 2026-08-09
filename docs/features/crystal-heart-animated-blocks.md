# Crystal Heart animated block system

## Current purpose

The Crystal Heart is a large animated dungeon landmark implemented as an **invisible anchor block + block entity + custom block-entity renderer (BER)**. The anchor can be saved inside normal Minecraft structure files while the renderer draws a crystal that is much larger than one block.

The system is loader-neutral in `common` except for the normal Fabric/NeoForge client bootstrap that eventually calls the shared renderer-registration boundary.

The Crystal Heart itself does not depend on Cobblemon behavior. Another mod can recreate the same rendering system with vanilla Minecraft client/block-entity APIs plus that mod's preferred registration API.

## Current source layout

```text
common/src/main/java/porker/pp_legendarydungeons/
├─ blocks/animated_blocks/crystal_heart/
│  └─ CrystalHeartBlock.java
├─ blocks/entity/animated_blocks/crystal_heart/
│  ├─ CrystalHeartBlockEntity.java
│  ├─ CrystalHeartMiningMode.java
│  └─ CrystalHeartPreset.java
├─ client/animated_blocks/
│  ├─ AnimatedBlockEntityRenderers.java
│  └─ crystal_heart/
│     ├─ CrystalHeartBlockEntityRenderer.java
│     └─ CrystalHeartGeometry.java
└─ setup/
   ├─ ModBlocks.java
   └─ ModBlockEntities.java
```

The active runtime textures are:

```text
common/src/main/resources/assets/pp_legendarydungeons/
└─ textures/entity/animated_blocks/crystal_heart/
   ├─ crystal_heart_a_top.png
   └─ crystal_heart_a_bottom.png
```

Old B/C texture experiments and the former `crystal_heart2` / `crystal_heart3` texture trees are no longer part of the active renderer. Git history can be used if those experiments ever need to be recovered for reference.

## Registered block IDs

`pp_legendarydungeons:crystal_heart` is the canonical block used for new content.

The older IDs below remain registered only for world/structure compatibility:

- `pp_legendarydungeons:crystal_heart_b`
- `pp_legendarydungeons:crystal_heart_c`

They are no longer exposed in the mod creative tab and no longer select separate B/C texture families. They render through the same NBT preset system as the canonical block.

Do not remove those registry IDs casually if existing worlds or structure NBT may still contain them.

## How the block works

`CrystalHeartBlock` extends `BaseEntityBlock` and acts only as an anchor:

- `RenderShape.INVISIBLE` prevents the normal block model from drawing in-world.
- collision is empty so players can move through the anchor.
- the selection/outline shape remains a full block so the anchor can still be targeted while developing.
- `newBlockEntity(...)` creates `CrystalHeartBlockEntity`.

The block entity stores the per-instance visual settings. The BER reads those settings every frame and renders the normalized geometry above the anchor.

## NBT settings

The block entity currently stores seven persistent values:

| NBT key | Purpose | Default |
|---|---|---:|
| `CrystalPreset` | Shape preset ID | `base` |
| `CrystalWidth` | Total rendered X/Z width in blocks | `5.5` |
| `CrystalHeight` | Total rendered Y height in blocks | `12.0` |
| `BobAmplitude` | Vertical bob distance in blocks | `0.35` |
| `MiningEnabled` | Whether survival players may mine the heart | `0b` / false |
| `MiningMode` | Drop behavior when mining is enabled | `silk_self_else_loot` |
| `MiningLootTable` | Loot table rolled for normal mining | `pp_legendarydungeons:blocks/default_ch_drop` |

Width, height, and bobbing are clamped by `CrystalHeartBlockEntity` before use. Missing or invalid preset names fall back to `base`. Invalid mining modes fall back to `silk_self_else_loot`, and invalid loot-table IDs fall back to the built-in default table.

Examples:

```mcfunction
/data merge block X Y Z {CrystalPreset:"base"}
/data merge block X Y Z {CrystalPreset:"long_point"}
/data merge block X Y Z {CrystalPreset:"spire"}

/data merge block X Y Z {CrystalWidth:5.5f,CrystalHeight:12.0f}
/data merge block X Y Z {BobAmplitude:0.85f}
/data merge block X Y Z {CrystalPreset:"spire",CrystalWidth:3.5f,CrystalHeight:12.0f,BobAmplitude:0.35f}

# Mining examples
/data merge block X Y Z {MiningEnabled:1b}
/data merge block X Y Z {MiningEnabled:1b,MiningMode:"self"}
/data merge block X Y Z {MiningEnabled:1b,MiningMode:"loot"}
/data merge block X Y Z {MiningEnabled:1b,MiningMode:"silk_self_else_loot"}
/data merge block X Y Z {MiningLootTable:"minecraft:chests/simple_dungeon"}
/data merge block X Y Z {MiningLootTable:"pp_legendarydungeons:blocks/default_ch_drop"}
/data merge block X Y Z {MiningEnabled:0b}

/data get block X Y Z
```

Structure blocks preserve this block-entity NBT, so different dungeon structures can save different shapes, dimensions, and mining rules while still using the same registered block.

## Mining and drops

The Crystal Heart is survival-unmineable by default. Creative players can still remove the anchor for development. When `MiningEnabled:1b` is set, any pickaxe may mine it; non-pickaxe tools receive no mining progress.

`MiningMode` supports exactly three values:

- `self` - always drop the registered Crystal Heart block item.
- `loot` - always roll `MiningLootTable`, including when the tool has Silk Touch.
- `silk_self_else_loot` - Silk Touch drops the heart item; other pickaxes roll `MiningLootTable`. This is the default enabled-mode behavior.

The built-in table is `pp_legendarydungeons:blocks/default_ch_drop`, stored at `data/pp_legendarydungeons/loot_table/blocks/default_ch_drop.json`. It drops 16-64 emeralds. When that exact built-in table is rolled, `CrystalHeartBlock` also awards 20-40 experience points.

Changing `MiningLootTable` replaces the built-in normal-mining reward. Custom tables do not automatically receive the built-in XP bonus. The selected table should be compatible with a block loot context.

All three retained Crystal Heart registry IDs are included in the vanilla `mineable/pickaxe` block tag for compatibility, although new content should continue to use `pp_legendarydungeons:crystal_heart`.

## Shape presets

`CrystalHeartPreset` currently defines:

| Preset | Waist Y in normalized mesh | Intended silhouette |
|---|---:|---|
| `base` | `0.46` | Current/original Crystal Heart |
| `long_point` | `0.72` | Short upper cap with a long lower point |
| `spire` | `0.60` | Taller/slimmer crystal profile |

All meshes use normalized coordinates:

- X/Z: `-0.5` to `+0.5`
- Y: `0.0` to `1.0`

The renderer then scales that normalized mesh using `CrystalWidth` and `CrystalHeight`. This is why one preset can be reused at many sizes without creating another block or model file.

Changing the waist Y changes the relative length of the top and bottom halves. A future preset can add more geometry parameters if a silhouette can no longer be represented by the same square-bipyramid topology.

## Geometry and UV mapping

`CrystalHeartGeometry` creates eight triangular faces:

- four upper faces from the top point to the waist ring,
- four lower faces from the waist ring to the bottom point.

Minecraft's entity-style render buffer is quad-oriented, so each triangle is emitted as a degenerate quad by repeating the third vertex.

All four upper faces share one 128x128 top texture and all four lower faces share one 128x128 bottom texture. Alternate faces mirror their U coordinates and use slight tint differences so rotation still shows visible facets.

Expected UV triangle positions:

```text
top texture (128x128)
        apex ≈ (64, 2)
              /\
             /  \
            /    \
  ≈ (2,126)______≈ (126,126)

bottom texture (128x128)
  ≈ (2,2)________≈ (126,2)
            \    /
             \  /
              \/
        apex ≈ (64,126)
```

Pixels outside the triangle should be transparent. Avoid transparent holes inside the triangle unless an actual hole in the rendered crystal is desired.

The current presets all reuse the same active texture pair. A future shape only needs dedicated textures if the shared artwork stretches poorly on that geometry.

## Renderer behavior

`CrystalHeartBlockEntityRenderer` performs the following operations each frame:

1. Read world time and the block entity's NBT-backed settings.
2. Calculate slow Y-axis rotation at `0.5` degrees/tick.
3. Calculate sine-wave bobbing at `0.04` radians/tick.
4. Translate to the center of the anchor block and slightly above its base.
5. Rotate around Y.
6. Scale the normalized mesh by `CrystalWidth`, `CrystalHeight`, `CrystalWidth`.
7. Resolve the texture pair from `CrystalHeartPreset`.
8. Render the upper four faces.
9. Request the bottom texture buffer only after the top half has been emitted, then render the lower four faces.

That top-then-bottom buffer ordering is intentional. Requesting both `VertexConsumer`s before finishing the first texture previously caused a `BufferBuilder: Not building!` crash.

Rendering currently uses:

```text
RenderType.entityCutoutNoCull(...)
LightTexture.FULL_BRIGHT
```

This keeps the large pixel-art texture crisp, avoids translucent depth-sorting problems, and gives the heart its luminous appearance.

## Animation and networking

Rotation and bobbing are derived from client world time, so there is no server animation tick and no per-frame network traffic.

NBT changes still need normal block-entity synchronization. `CrystalHeartBlockEntity` therefore supplies an update tag/update packet and its setters call `sendBlockUpdated(...)` on the server.

## Render distance and culling

Because the visual extends far outside its one-block anchor, the renderer currently:

- returns `true` from `shouldRenderOffScreen(...)`,
- uses the player's configured render distance for `getViewDistance()`,
- keeps a very large finite render bounding box as a cross-loader culling workaround/diagnostic.

The finite vanilla `AABB` is deliberate. A prior attempt to reference NeoForge's loader-added `AABB.INFINITE` from `common` broke common/Fabric compilation.

The render-distance code does not force chunks to load. The anchor's chunk still has to be available to the client before its block entity can render.

## Client registration

The shared renderer registration occurs in `AnimatedBlockEntityRenderers`:

```java
BlockEntityRendererRegistry.register(
        ModBlockEntities.CRYSTAL_HEART.get(),
        CrystalHeartBlockEntityRenderer::new
);
```

`LegendaryDungeonsClient.init()` calls that shared registration method.

Fabric calls `LegendaryDungeonsClient.init()` from its `ClientModInitializer`.

NeoForge defers the same call until `FMLClientSetupEvent` and uses `event.enqueueWork(...)`. Do not resolve the block-entity registry supplier too early from the NeoForge mod constructor; that previously caused a registry-object-not-present startup failure.

## Adding another shape

For another square-bipyramid-style silhouette:

1. Add a value to `CrystalHeartPreset`.
2. Give it a unique lowercase NBT ID.
3. Choose its normalized `waistY`.
4. Reuse the base texture path initially.
5. Test with `/data merge block ... {CrystalPreset:"new_id"}`.
6. Only add a dedicated texture pair if the existing art no longer maps well.

No new block registration, block entity type, blockstate, or item is required for an NBT-only shape preset.

## Related porting guide

For a self-contained guide intended for another mod author, including the files to copy/recreate and the loader-registration steps, see:

[`../development/CRYSTAL_HEART_REIMPLEMENTATION_GUIDE.md`](../development/CRYSTAL_HEART_REIMPLEMENTATION_GUIDE.md)
