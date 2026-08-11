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
│  ├─ CrystalHeartColorMode.java
│  ├─ CrystalHeartColorPreset.java
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
   ├─ crystal_heart_a_bottom.png
   ├─ crystal_heart_blank_top.png
   └─ crystal_heart_blank_bottom.png
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

The block entity stores the per-instance visual and gameplay configuration. The BER reads the rendering-related settings every frame and renders normalized geometry above the anchor, while the block uses the same stored data for environmental light, mining rules, loot behavior, and configuration-preserving self drops.

## NBT settings

The block entity currently stores eleven persistent values:

| NBT key | Purpose | Default |
|---|---|---:|
| `CrystalPreset` | Shape preset ID | `base` |
| `CrystalWidth` | Total rendered X/Z width in blocks | `5.5` |
| `CrystalHeight` | Total rendered Y height in blocks | `12.0` |
| `BobAmplitude` | Vertical bob distance in blocks | `0.35` |
| `LightLevel` | Environmental block-light emission | `0` |
| `CrystalColorMode` | Authored preset texture or custom RGB tint | `preset` |
| `CrystalColorPreset` | Authored color preset used in preset mode | `green` |
| `CrystalColor` | 24-bit `0xRRGGBB` integer used in custom mode | `16777215` / white |
| `MiningEnabled` | Whether survival players may mine the heart | `0b` / false |
| `MiningMode` | Drop behavior when mining is enabled | `silk_self_else_loot` |
| `MiningLootTable` | Loot table rolled for normal mining | `pp_legendarydungeons:blocks/default_ch_drop` |

Width, height, bobbing, and `LightLevel` are clamped by `CrystalHeartBlockEntity` before use. `LightLevel` is limited to vanilla's `0..15` block-light range. Missing or invalid shape/color preset names fall back to `base` / `green`, invalid color modes fall back to `preset`, invalid mining modes fall back to `silk_self_else_loot`, and invalid loot-table IDs fall back to the built-in default table. `CrystalColor` is normalized to the low 24 RGB bits.

Examples:

```mcfunction
/data merge block X Y Z {CrystalPreset:"base"}
/data merge block X Y Z {CrystalPreset:"long_point"}
/data merge block X Y Z {CrystalPreset:"spire"}

/data merge block X Y Z {CrystalWidth:5.5f,CrystalHeight:12.0f}
/data merge block X Y Z {BobAmplitude:0.85f}
/data merge block X Y Z {CrystalPreset:"spire",CrystalWidth:3.5f,CrystalHeight:12.0f,BobAmplitude:0.35f}

# Environmental light examples
/data merge block X Y Z {LightLevel:0}
/data merge block X Y Z {LightLevel:7}
/data merge block X Y Z {LightLevel:15}

# Color examples
/data merge block X Y Z {CrystalColorMode:"preset",CrystalColorPreset:"green"}
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:16711680}
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:255}
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:11141375}

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

Structure blocks preserve this block-entity NBT, so different dungeon structures can save different shapes, dimensions, light levels, color settings, and mining rules while still using the same registered block. The `self` mining path and the Silk Touch branch of `silk_self_else_loot` also copy the same configuration into the dropped Crystal Heart item so placing it again recreates the configured heart.

## Builder and testing command reference

Replace `X Y Z` with the coordinates of the invisible Crystal Heart anchor block. The easiest way to confirm a change is to run `/data get block X Y Z` after editing it.

### Inspect the heart

```mcfunction
/data get block X Y Z
```

Shows the complete block-entity data currently stored by that Crystal Heart.

### Shape

```mcfunction
/data merge block X Y Z {CrystalPreset:"base"}
/data merge block X Y Z {CrystalPreset:"long_point"}
/data merge block X Y Z {CrystalPreset:"spire"}
```

- `base` is the original/current silhouette.
- `long_point` moves the waist upward and creates a longer lower point.
- `spire` uses the taller/slimmer preset.

### Width, height, and bobbing

```mcfunction
/data merge block X Y Z {CrystalWidth:5.5f,CrystalHeight:12.0f}
/data merge block X Y Z {BobAmplitude:0.0f}
/data merge block X Y Z {BobAmplitude:0.85f}
```

`CrystalWidth` controls total X/Z width, `CrystalHeight` controls total Y height, and `BobAmplitude` controls how far the crystal moves vertically while hovering. Width is clamped to `0.25..64`, height to `0.25..128`, and bob amplitude to `0..8`.

### Environmental light

```mcfunction
/data merge block X Y Z {LightLevel:0}
/data merge block X Y Z {LightLevel:7}
/data merge block X Y Z {LightLevel:15}
```

`LightLevel` controls real vanilla block light around the anchor. `0` emits no environmental light and `15` is the vanilla maximum, comparable to glowstone. The rendered crystal itself remains full-bright even at `0`.

### Original green artwork

```mcfunction
/data merge block X Y Z {CrystalColorMode:"preset",CrystalColorPreset:"green"}
```

Uses the authored green top/bottom textures exactly rather than recoloring the grayscale custom-color textures.

### Custom RGB colors

Custom colors must use `CrystalColorMode:"custom"` and a decimal 24-bit RGB integer in `CrystalColor`.

```mcfunction
# White
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:16777215}

# Red
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:16711680}

# Green
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:65280}

# Blue
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:255}

# Purple
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:11141375}

# Cyan
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:65535}

# Yellow / gold
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:16766720}

# Orange
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:16744192}

# Black
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:0}

# Very dark gray
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:2105376}
```

The integer format is the same general 24-bit `0xRRGGBB` representation used for dyed-leather colors. A decimal RGB value copied from a leather-color picker such as MCStacker can therefore be pasted into `CrystalColor`; the general hue will match, although the crystal's grayscale artwork and facet shading can make perceived brightness differ from leather armor.

### Mining and drops

```mcfunction
# Enable mining with the default behavior:
# Silk Touch -> configured Crystal Heart item
# normal pickaxe -> configured loot table
/data merge block X Y Z {MiningEnabled:1b,MiningMode:"silk_self_else_loot"}

# Always drop this configured Crystal Heart item
/data merge block X Y Z {MiningEnabled:1b,MiningMode:"self"}

# Always roll the configured loot table, even with Silk Touch
/data merge block X Y Z {MiningEnabled:1b,MiningMode:"loot"}

# Change the normal-mining loot table
/data merge block X Y Z {MiningLootTable:"minecraft:chests/simple_dungeon"}

# Restore the built-in Crystal Heart reward
/data merge block X Y Z {MiningLootTable:"pp_legendarydungeons:blocks/default_ch_drop"}

# Disable survival mining
/data merge block X Y Z {MiningEnabled:0b}
```

When a mining mode returns the Crystal Heart item itself, the dropped item carries the current block-entity configuration. Replacing that item restores its shape, dimensions, bobbing, light level, color mode/color, mining enabled state, mining mode, and mining loot table.

### Combined example

```mcfunction
/data merge block X Y Z {CrystalPreset:"spire",CrystalWidth:4.0f,CrystalHeight:18.0f,BobAmplitude:0.6f,LightLevel:15,CrystalColorMode:"custom",CrystalColor:11141375,MiningEnabled:1b,MiningMode:"silk_self_else_loot",MiningLootTable:"pp_legendarydungeons:blocks/default_ch_drop"}
```

This produces a tall purple, glowing, bobbing heart that can be mined with a pickaxe; Silk Touch returns the configured heart while an ordinary pickaxe uses the built-in reward.

## Mining and drops

The Crystal Heart is survival-unmineable by default. Creative players can still remove the anchor for development. When `MiningEnabled:1b` is set, any pickaxe may mine it; non-pickaxe tools receive no mining progress.

`MiningMode` supports exactly three values:

- `self` - always drop the configured Crystal Heart block item, preserving the current block-entity settings for re-placement.
- `loot` - always roll `MiningLootTable`, including when the tool has Silk Touch.
- `silk_self_else_loot` - Silk Touch drops the configured heart item with its current block-entity settings; other pickaxes roll `MiningLootTable`. This is the default enabled-mode behavior.

For either self-drop path, `CrystalHeartBlock` creates the block `ItemStack` and calls `CrystalHeartBlockEntity.saveToItem(...)`. The block entity writes all eleven persistent settings and stores them on the stack through vanilla `BlockItem.setBlockEntityData(...)`. Minecraft then reapplies that block-entity data when the item is placed again. This means a heart configured with `MiningMode:"self"` remains configured that way after being mined, picked up, and placed again; the same is true for its shape, size, bobbing, light, color, and loot-table settings.

The built-in table is `pp_legendarydungeons:blocks/default_ch_drop`, stored at `data/pp_legendarydungeons/loot_table/blocks/default_ch_drop.json`. It drops 16-64 emeralds. When that exact built-in table is rolled, `CrystalHeartBlock` also awards 20-40 experience points.

Changing `MiningLootTable` replaces the built-in normal-mining reward. Custom tables do not automatically receive the built-in XP bonus. The selected table should be compatible with a block loot context.

### Minecraft 1.21.1 loot-table lookup detail

`MiningLootTable` is persisted in block-entity NBT as a resource-location string because that is convenient to edit with `/data merge`. In Minecraft 1.21.1, however, `reloadableRegistries().getLootTable(...)` expects a `ResourceKey<LootTable>`, not a raw `ResourceLocation`. Convert the stored ID at lookup time:

```java
LootTable lootTable = level.getServer()
        .reloadableRegistries()
        .getLootTable(ResourceKey.create(
                Registries.LOOT_TABLE,
                heart.getMiningLootTable()
        ));
```

Keeping the NBT-facing value as a `ResourceLocation` still allows arbitrary datapack loot-table IDs while the runtime lookup uses the type required by 1.21.1.

All three retained Crystal Heart registry IDs are included in the vanilla `mineable/pickaxe` block tag for compatibility, although new content should continue to use `pp_legendarydungeons:crystal_heart`.

## Environmental light

`LightLevel` controls real environmental block light independently from the renderer's visual brightness.

- `0` emits no block light and is the default.
- `1..14` emit progressively more vanilla block light.
- `15` is the vanilla maximum, equal to glowstone-level emission.

The renderer still uses `LightTexture.FULL_BRIGHT`, so the crystal graphic remains visually luminous even when `LightLevel:0`.

Vanilla environmental light is derived from `BlockState`, not arbitrary block-entity NBT. The Crystal Heart therefore mirrors its NBT `LightLevel` into a hidden integer `light_level` BlockState property. `ModBlocks` reads that property through `BlockBehaviour.Properties.lightLevel(...)`. When NBT is reloaded in-world (including `/data merge block`), the block entity synchronizes the property so Minecraft's lighting engine recalculates the surrounding light.

## Color modes and custom tinting

Color is independent from the geometry preset.

`CrystalColorMode:"preset"` uses an authored texture/color preset. The only current authored preset is:

```text
CrystalColorPreset:"green"
```

`green` uses the existing `crystal_heart_a_top.png` and `crystal_heart_a_bottom.png` artwork and preserves the current green face shading exactly.

`CrystalColorMode:"custom"` instead uses:

```text
crystal_heart_blank_top.png
crystal_heart_blank_bottom.png
```

These are grayscale copies of the active artwork: the same transparency, highlights, shadows, and pixel pattern are retained, but the green hue is removed. The renderer then applies `CrystalColor` as a 24-bit RGB tint (`0xRRGGBB`). Examples:

| Color | RGB hex | NBT integer |
|---|---:|---:|
| White | `FFFFFF` | `16777215` |
| Red | `FF0000` | `16711680` |
| Green | `00FF00` | `65280` |
| Blue | `0000FF` | `255` |
| Purple | `AA00FF` | `11141375` |

Custom mode converts the existing per-face authored tint into neutral brightness before applying the requested RGB value. This preserves visible facets without contaminating custom red/blue/purple colors with the original green-biased face tint.

`CrystalColorPreset` is ignored while custom mode is active, and `CrystalColor` is ignored while preset mode is active. Keeping those values separate allows additional authored presets to be added later without tying color choices to `CrystalPreset`.

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
7. Resolve the texture pair from the shape preset plus `CrystalColorMode` / `CrystalColorPreset`.
8. In custom mode, apply the NBT `CrystalColor` tint using neutral facet brightness.
9. Render the upper four faces.
10. Request the bottom texture buffer only after the top half has been emitted, then render the lower four faces.

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
