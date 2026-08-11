# Reimplementing the Crystal Heart block-entity renderer

This document is intended for another Minecraft mod author who wants to recreate the current Crystal Heart system in a different mod.

The implementation described here matches the current **Minecraft 1.21.1 / Java 21** Crystal Heart architecture in Cobblemon: Explore Legendary Dungeons. The example project uses Architectury for a shared Fabric + NeoForge codebase, but the rendering concept itself is not Architectury-specific and does not require Cobblemon.

## What you are recreating

The system is not a normal block model and not a custom entity. It consists of:

```text
invisible anchor block
        ↓
block entity containing persistent visual settings
        ↓
client block-entity renderer
        ↓
normalized procedural crystal geometry
        ↓
top + bottom PNG textures
```

The result is a large floating crystal that can:

- rotate smoothly,
- bob vertically,
- be many blocks larger than its anchor,
- change width and height through NBT,
- change silhouette through an NBT preset,
- emit configurable vanilla block light,
- use the original green artwork or an arbitrary custom RGB tint,
- be saved inside normal Minecraft structure templates.

## Files in the reference implementation

The equivalent files in this project are:

```text
Block / state owner
common/src/main/java/porker/pp_legendarydungeons/
  blocks/animated_blocks/crystal_heart/CrystalHeartBlock.java

Persistent instance data
  blocks/entity/animated_blocks/crystal_heart/CrystalHeartBlockEntity.java
  blocks/entity/animated_blocks/crystal_heart/CrystalHeartColorMode.java
  blocks/entity/animated_blocks/crystal_heart/CrystalHeartColorPreset.java
  blocks/entity/animated_blocks/crystal_heart/CrystalHeartMiningMode.java
  blocks/entity/animated_blocks/crystal_heart/CrystalHeartPreset.java

Client rendering
  client/animated_blocks/crystal_heart/CrystalHeartGeometry.java
  client/animated_blocks/crystal_heart/CrystalHeartBlockEntityRenderer.java
  client/animated_blocks/AnimatedBlockEntityRenderers.java

Registration
  setup/ModBlocks.java
  setup/ModBlockEntities.java

Shared client bootstrap
  LegendaryDungeonsClient.java

Loader client entrypoints
fabric/src/main/java/.../ProfessorPorkersLegendaryDungeonsClient.java
neoforge/src/main/java/.../ProfessorPorkersLegendaryDungeonsNeoForgeClient.java
```

Assets:

```text
assets/<your_mod_id>/
├─ blockstates/crystal_heart.json
├─ models/block/crystal_heart.json
├─ models/item/crystal_heart.json
├─ textures/entity/animated_blocks/crystal_heart/
│  ├─ crystal_heart_a_top.png
│  ├─ crystal_heart_a_bottom.png
│  ├─ crystal_heart_blank_top.png
│  └─ crystal_heart_blank_bottom.png

data/<your_mod_id>/loot_table/blocks/
└─ default_ch_drop.json

data/minecraft/tags/block/mineable/
└─ pickaxe.json
```

You may rename every Java package, registry ID, and texture path for the destination mod. Keep the relationships between the pieces the same.

## Step 1: create the anchor block

Create a `BaseEntityBlock` whose world model is invisible and whose block entity is your Crystal Heart block entity.

The important behavior is equivalent to:

```java
@Override
public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new CrystalHeartBlockEntity(pos, state);
}

@Override
public RenderShape getRenderShape(BlockState state) {
    return RenderShape.INVISIBLE;
}

@Override
public VoxelShape getCollisionShape(...) {
    return Shapes.empty();
}
```

The reference implementation deliberately keeps a normal one-block selection shape so developers can still target the invisible anchor with `/data` commands.

Register the block and, if desired, a normal `BlockItem` for development/creative placement.

For the optional mining behavior, give the block a normal positive destroy time, require the correct tool for drops, put the block in `minecraft:mineable/pickaxe`, and override destroy progress so survival players can only mine it when the block entity has `MiningEnabled:1b` and the held item is a pickaxe.

For configurable environmental light, add an integer BlockState property such as `light_level` with a `0..15` range and make the block's `BlockBehaviour.Properties.lightLevel(...)` read it. Keep the editable value in block-entity NBT as `LightLevel`, then mirror the NBT value into the BlockState whenever it changes. This bridge is necessary because vanilla environmental block lighting is state-driven.

## Step 2: register the block entity type

Register a `BlockEntityType<CrystalHeartBlockEntity>` and associate it with the Crystal Heart anchor block.

In this project, the type also lists the old `crystal_heart_b` and `crystal_heart_c` blocks because those registry IDs are retained for compatibility. A new mod only needs one block unless it also has legacy aliases to preserve.

The important relationship is:

```text
CrystalHeartBlock
    creates
CrystalHeartBlockEntity
    whose registered BlockEntityType includes CrystalHeartBlock
```

Make sure block registration happens before code resolves the block supplier for the block-entity type.

## Step 3: store the visual settings in the block entity

The reference block entity stores:

```text
CrystalPreset       string
CrystalWidth        float
CrystalHeight       float
BobAmplitude        float
LightLevel          int
CrystalColorMode    string
CrystalColorPreset  string
CrystalColor        int / 24-bit RGB
MiningEnabled       boolean
MiningMode          string
MiningLootTable     string/resource location
```

Reference defaults:

```text
preset             = base
width              = 5.5 blocks
height             = 12.0 blocks
bob amplitude      = 0.35 blocks
light level        = 0
color mode         = preset
color preset       = green
custom RGB color   = 0xFFFFFF / 16777215
mining enabled     = false
mining mode        = silk_self_else_loot
mining loot table  = <your_mod_id>:blocks/default_ch_drop
```

Reference clamp ranges:

```text
width:         0.25 .. 64.0
height:        0.25 .. 128.0
bob amplitude: 0.0  .. 8.0
light level:   0    .. 15
```

Implement the standard block-entity NBT methods for your mappings/version and save all eleven values. Invalid or missing color modes should fall back to `preset`, authored color presets should fall back to `green`, mining modes should fall back to `silk_self_else_loot`, and invalid loot-table identifiers should fall back to the default table. Normalize `CrystalColor` to the low 24 bits so it remains a `0xRRGGBB` value.

### Mining/drop behavior

The reference implementation supports:

```text
self                 -> always drop the block item
loot                 -> always roll MiningLootTable
silk_self_else_loot  -> Silk Touch drops the block item; otherwise roll MiningLootTable
```

`MiningEnabled` is separate and defaults to false, so newly placed hearts remain survival-unmineable unless their NBT explicitly enables mining.

The reference block disables its ordinary block loot table and handles enabled mining drops server-side from `playerDestroy(...)`. For a loot-table drop, create a block loot context containing the origin, block state, tool, player/block entity where available, and player luck, then roll the ID stored in `MiningLootTable`.

For Minecraft 1.21.1 with Mojang mappings, keep the editable NBT value as a `ResourceLocation`, but convert it to a `ResourceKey<LootTable>` before calling `getLootTable(...)`:

```java
LootTable lootTable = level.getServer()
        .reloadableRegistries()
        .getLootTable(ResourceKey.create(
                Registries.LOOT_TABLE,
                heart.getMiningLootTable()
        ));
```

Passing the `ResourceLocation` directly to `getLootTable(...)` does not compile on this target because the method expects `ResourceKey<LootTable>`. Other Minecraft versions/mappings may expose a different lookup signature, so use the destination project's actual API.

The reference default table is `<your_mod_id>:blocks/default_ch_drop`. In this project it drops 16-64 emeralds. The block code additionally awards 20-40 XP when that exact built-in table is selected. Custom loot-table overrides replace that built-in reward behavior.

For Silk Touch, look up `Enchantments.SILK_TOUCH` from the server enchantment registry and test the held tool before choosing between the self-drop and loot-table branches.

### Environmental light behavior

Minecraft's vanilla light engine does not read arbitrary block-entity NBT directly. The reference implementation therefore keeps `LightLevel` in the block entity for structure/NBT configurability but mirrors it into a hidden `IntegerProperty` on `CrystalHeartBlock`.

Use `0` for no environmental light and clamp to vanilla's maximum of `15` (glowstone-level block light). The BER can remain `FULL_BRIGHT`; renderer brightness and environmental emission are intentionally independent.

When `LightLevel` changes through a setter or `/data merge block`, update the same block's state with the new property value. A structure template then preserves both the block state and the block-entity NBT.

### Color/tint behavior

Keep shape and color as separate concepts.

```text
CrystalColorMode:"preset"
    + CrystalColorPreset:"green"
    -> original authored green top/bottom textures

CrystalColorMode:"custom"
    + CrystalColor:<24-bit RGB integer>
    -> neutral grayscale top/bottom textures + vertex RGB tint
```

Create the neutral textures by converting the active green artwork to grayscale while preserving alpha and pixel luminance. Do not replace the current green preset with a tint if exact visual compatibility matters.

For custom colors, decode the NBT integer as:

```java
int red = (color >> 16) & 0xFF;
int green = (color >> 8) & 0xFF;
int blue = color & 0xFF;
```

The reference geometry's original face colors are slightly green-biased. In custom mode, first convert each face tint to a neutral brightness multiplier, then multiply the requested RGB channels by that brightness. This keeps the faceted appearance without adding a green cast to arbitrary colors.

Also provide the normal block-entity client synchronization path, equivalent to:

```java
@Override
public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return saveWithoutMetadata(registries);
}

@Override
public Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
}
```

When a server-side setter changes a visual value, call `setChanged()` and send a block update so connected clients receive the new data.

## Step 4: define named shape presets

The reference implementation uses an enum named `CrystalHeartPreset`.

Current values:

```text
base       waistY = 0.46
long_point waistY = 0.72
spire      waistY = 0.60
```

Each preset currently also points to the same active top/bottom texture family.

A useful minimal preset record is conceptually:

```java
preset id
normalized waist Y
texture folder
texture base name
```

Provide a `fromId(String)` lookup and fall back to `base` for null, blank, or unknown values. That gives old structure NBT and mistyped commands a safe rendering fallback.

## Step 5: build normalized procedural geometry

Do not bake the current Crystal Heart size directly into the mesh.

Use normalized coordinates:

```text
TOP    = ( 0.0, 1.0,  0.0)
BOTTOM = ( 0.0, 0.0,  0.0)

NORTH  = ( 0.0, waistY, -0.5)
EAST   = ( 0.5, waistY,  0.0)
SOUTH  = ( 0.0, waistY,  0.5)
WEST   = (-0.5, waistY,  0.0)
```

Create four upper triangular faces:

```text
TOP → EAST  → NORTH
TOP → SOUTH → EAST
TOP → WEST  → SOUTH
TOP → NORTH → WEST
```

Create four lower faces:

```text
BOTTOM → NORTH → EAST
BOTTOM → EAST  → SOUTH
BOTTOM → SOUTH → WEST
BOTTOM → WEST  → NORTH
```

The reference implementation calculates each face normal using the cross product of two face edges.

Because the entity-style render path expects quads, emit each triangle as four vertices and repeat the third vertex as the fourth. This produces a degenerate quad that renders as the intended triangle.

## Step 6: use two full-face textures

Use one texture for the four upper faces and another for the four lower faces.

The reference assets are 128x128 PNGs. Their important UV points are approximately:

```text
TOP
apex       (64, 2)
base-left  (2, 126)
base-right (126, 126)

BOTTOM
base-left  (2, 2)
base-right (126, 2)
apex       (64, 126)
```

Normalized UV equivalents used by the renderer are approximately:

```text
0.50,     0.015625
0.015625, 0.984375
0.984375, 0.984375
```

with the Y orientation reversed for the bottom texture.

Keep pixels outside the intended triangle transparent. Make sure the entire triangle itself has coverage; transparent gaps inside it become literal missing pieces in the rendered model.

The reference renderer mirrors alternate faces horizontally and applies small RGB tint differences to neighboring faces. This makes the rotating object read as faceted while requiring only two art files.

## Step 7: implement the block-entity renderer

In the BER's `render(...)` method:

1. Obtain `gameTime + partialTick`.
2. Compute rotation.
3. Compute sine-wave bobbing.
4. Read the block entity's preset, width, height, and bob amplitude.
5. Push the pose stack.
6. Translate to the center of the anchor.
7. Apply bobbing and the bottom-point offset.
8. Rotate around Y.
9. Scale X/Z by width and Y by height.
10. Render the preset's upper mesh with the top texture.
11. Obtain the bottom texture buffer only after the upper mesh is finished.
12. Render the lower mesh.
13. Pop the pose stack.

Reference animation constants:

```text
rotation = 0.5 degrees per tick
bob frequency = 0.04 radians per tick
bottom point offset = 0.25 blocks
```

Reference scale operation:

```java
poseStack.scale(
    blockEntity.getWidthBlocks(),
    blockEntity.getHeightBlocks(),
    blockEntity.getWidthBlocks()
);
```

Reference render type:

```java
RenderType.entityCutoutNoCull(texture)
```

Reference lighting:

```java
LightTexture.FULL_BRIGHT
```

### Important buffer-order rule

Do **not** request both texture `VertexConsumer`s first and then render them later.

Use this order:

```text
get top buffer
render top
get bottom buffer
render bottom
```

The reference project previously crashed with `IllegalStateException: Not building!` when the second buffer was requested before the first texture's geometry had been emitted.

## Step 8: handle large-renderer culling

A normal block entity is usually expected to render close to its one-block position. This model can be more than ten blocks tall, so the reference renderer expands its rendering allowances.

It currently:

```java
@Override
public boolean shouldRenderOffScreen(CrystalHeartBlockEntity blockEntity) {
    return true;
}
```

It also overrides `getViewDistance()` using the player's configured chunk render distance and exposes a very large finite `AABB` from `getRenderBoundingBox(...)`.

The finite bounding box is intentionally vanilla-compatible. Do not copy a loader-added constant such as NeoForge's `AABB.INFINITE` into shared/common source if the same code also has to compile for Fabric.

You can later tighten these bounds after confirming the destination renderer does not disappear at high viewing angles.

## Step 9: register the renderer on the physical client only

Register the BER against the Crystal Heart block entity type.

In the Architectury reference project:

```java
BlockEntityRendererRegistry.register(
    ModBlockEntities.CRYSTAL_HEART.get(),
    CrystalHeartBlockEntityRenderer::new
);
```

Keep this out of dedicated-server initialization.

### Fabric reference

The Fabric client entrypoint can call the shared client initializer directly from `ClientModInitializer.onInitializeClient()`.

### NeoForge reference

The reference NeoForge implementation waits for `FMLClientSetupEvent` and calls the shared client initializer through `event.enqueueWork(...)`.

This matters because an earlier implementation attempted to resolve the registered block-entity supplier too early from the NeoForge mod constructor and failed with a registry-object-not-present startup error.

If the destination project is single-loader, use that loader's normal BER registration lifecycle instead of copying the Architectury wrapper classes unnecessarily.

## Step 10: add blockstate/item assets

Even though the world block uses `RenderShape.INVISIBLE`, normal blockstate and model JSONs are still useful for the registered block/item and to avoid missing-model noise.

The reference project uses a simple placeholder block model for inventory representation. The large animated world model comes exclusively from the BER.

## Step 11: verify NBT control in game

Place the anchor and test:

```mcfunction
/data merge block X Y Z {CrystalPreset:"base"}
/data merge block X Y Z {CrystalPreset:"long_point"}
/data merge block X Y Z {CrystalPreset:"spire"}

/data merge block X Y Z {CrystalWidth:5.5f,CrystalHeight:12.0f,BobAmplitude:0.35f}
/data merge block X Y Z {LightLevel:0}
/data merge block X Y Z {LightLevel:15}
/data merge block X Y Z {CrystalColorMode:"preset",CrystalColorPreset:"green"}
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:16711680}
/data merge block X Y Z {CrystalColorMode:"custom",CrystalColor:255}
/data merge block X Y Z {MiningEnabled:1b}
/data merge block X Y Z {MiningEnabled:1b,MiningMode:"self"}
/data merge block X Y Z {MiningEnabled:1b,MiningMode:"loot"}
/data merge block X Y Z {MiningEnabled:1b,MiningMode:"silk_self_else_loot"}
/data merge block X Y Z {MiningLootTable:"minecraft:chests/simple_dungeon"}
/data merge block X Y Z {MiningLootTable:"<your_mod_id>:blocks/default_ch_drop"}
/data merge block X Y Z {MiningEnabled:0b}
/data get block X Y Z
```

Verify all of the following:

- preset changes update the silhouette,
- width and height work on every preset,
- bob amplitude updates on the client,
- `LightLevel:0` emits no environmental block light while the rendered heart remains full-bright,
- `LightLevel:15` emits maximum vanilla block light and updates immediately after `/data merge block`,
- preset/green mode looks identical to the existing authored green heart,
- custom mode preserves the grayscale texture pattern while tinting red, blue, purple, and other RGB values cleanly,
- the top/bottom seam has no holes,
- the crystal remains visible while looking upward/downward at steep angles,
- saving/loading the world preserves NBT,
- structure blocks preserve the shape, light, color, and mining settings,
- a default heart cannot be mined in survival,
- `MiningEnabled:1b` allows any pickaxe to make mining progress while non-pickaxes do not,
- `self` always drops the block item,
- `loot` always uses the configured loot table, including with Silk Touch,
- `silk_self_else_loot` gives the block item with Silk Touch and configured loot otherwise,
- the built-in default table gives 16-64 emeralds plus 20-40 XP,
- a `MiningLootTable` override rolls the replacement table without the built-in XP bonus,
- dedicated server startup does not load client-only renderer classes.

## Step 12: copy the heart into structure templates

Once the block entity is working, place it at the intended anchor position, apply the desired NBT, and save the surrounding build with a structure block.

Because the instance settings live in block-entity NBT, the structure template stores the selected shape preset, dimensions, light level, color configuration, and mining/drop rules with the anchor. The mirrored `light_level` BlockState is saved alongside it. No command needs to resize or reconfigure the Crystal Heart after world generation unless dynamic behavior is desired.

## Adding new shapes later

If the new shape is still a square bipyramid, add another preset and change `waistY`.

If the new design needs additional rings, an asymmetric silhouette, different numbers of sides, or separate protrusions, extend `CrystalHeartGeometry` so the preset can select a different mesh topology. Keep width/height scaling outside the geometry so the same NBT resizing system continues to work.

Dedicated textures are optional. Start by mapping the current base texture to the new shape and create new art only if the mapping visibly stretches or distorts important details.

## Minimal port checklist

A destination mod needs all of these concepts wired together:

- [ ] registered invisible anchor block
- [ ] registered block entity type
- [ ] block entity NBT persistence and update packets
- [ ] NBT `LightLevel` plus a `0..15` BlockState light-property bridge
- [ ] preset/custom color mode and 24-bit RGB persistence
- [ ] authored green and neutral grayscale top/bottom texture pairs
- [ ] named preset lookup with a safe fallback
- [ ] normalized geometry builder
- [ ] top/bottom UV mapping
- [ ] client BER with rotation, bobbing, and scaling
- [ ] sequential top/bottom texture-buffer rendering
- [ ] large block-entity culling/view-distance handling
- [ ] client-only renderer registration
- [ ] top and bottom PNG assets
- [ ] blockstate/item model assets
- [ ] pickaxe mineable tag and per-instance mining gate
- [ ] `MiningMode` branching for self / loot / Silk Touch behavior
- [ ] default block loot table plus optional NBT loot-table override
- [ ] Minecraft-version-correct loot-table registry lookup (`ResourceKey<LootTable>` on the reference 1.21.1 target)
- [ ] custom-color neutral facet shading without the original green bias
- [ ] in-game NBT, light, color, mining/drop, and structure-block testing

For exact implementation details, use the source files listed at the beginning of this document as the canonical reference.
