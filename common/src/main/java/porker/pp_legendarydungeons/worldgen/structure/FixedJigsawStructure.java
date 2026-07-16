package porker.pp_legendarydungeons.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import porker.pp_legendarydungeons.setup.ModStructureTypes;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Vanilla-compatible jigsaw structure with configurable root rotation and
 * surface-aware high-terrain placement.
 *
 * <p>The normal placement is still produced by vanilla JigsawPlacement. After
 * vanilla builds the complete piece set, {@link FixedJigsawTerrainPlacement}
 * may move every piece together so that:</p>
 *
 * <ul>
 *     <li>the root start is never closer to the dimension ceiling than the
 *     configured clearance;</li>
 *     <li>a nearby lower site is preferred when the original terrain is too
 *     high;</li>
 *     <li>the complete generated bounding box remains inside the dimension.</li>
 * </ul>
 *
 * <p>Child pieces retain vanilla jigsaw rotation and connector behavior.</p>
 */
public final class FixedJigsawStructure extends Structure {
    public static final DimensionPadding DEFAULT_DIMENSION_PADDING =
            DimensionPadding.ZERO;
    public static final LiquidSettings DEFAULT_LIQUID_SETTINGS =
            LiquidSettings.APPLY_WATERLOGGING;

    public static final int DEFAULT_MINIMUM_CLEARANCE_BELOW_TOP = 175;
    public static final int DEFAULT_TERRAIN_SEARCH_RADIUS = 32;
    public static final int DEFAULT_TERRAIN_SEARCH_STEP = 8;

    private static final int MAX_TOTAL_STRUCTURE_RANGE = 128;
    private static final int MAX_CLEARANCE = 4096;
    private static final int MAX_SEARCH_RADIUS = 128;
    private static final int MAX_SEARCH_STEP = 64;

    public static final MapCodec<FixedJigsawStructure> CODEC =
            RecordCodecBuilder.<FixedJigsawStructure>mapCodec(instance ->
                    instance.group(
                            settingsCodec(instance),
                            StructureTemplatePool.CODEC
                                    .fieldOf("start_pool")
                                    .forGetter(structure -> structure.startPool),
                            ResourceLocation.CODEC
                                    .optionalFieldOf("start_jigsaw_name")
                                    .forGetter(structure -> structure.startJigsawName),
                            Codec.intRange(0, 20)
                                    .fieldOf("size")
                                    .forGetter(structure -> structure.maxDepth),
                            HeightProvider.CODEC
                                    .fieldOf("start_height")
                                    .forGetter(structure -> structure.startHeight),
                            Codec.BOOL
                                    .fieldOf("use_expansion_hack")
                                    .forGetter(structure -> structure.useExpansionHack),
                            Heightmap.Types.CODEC
                                    .optionalFieldOf("project_start_to_heightmap")
                                    .forGetter(structure -> structure.projectStartToHeightmap),
                            Codec.intRange(1, MAX_TOTAL_STRUCTURE_RANGE)
                                    .fieldOf("max_distance_from_center")
                                    .forGetter(structure -> structure.maxDistanceFromCenter),
                            Codec.list(PoolAliasBinding.CODEC)
                                    .optionalFieldOf("pool_aliases", List.of())
                                    .forGetter(structure -> structure.poolAliases),
                            DimensionPadding.CODEC
                                    .optionalFieldOf(
                                            "dimension_padding",
                                            DEFAULT_DIMENSION_PADDING
                                    )
                                    .forGetter(structure -> structure.dimensionPadding),
                            LiquidSettings.CODEC
                                    .optionalFieldOf(
                                            "liquid_settings",
                                            DEFAULT_LIQUID_SETTINGS
                                    )
                                    .forGetter(structure -> structure.liquidSettings),
                            FixedJigsawRotation.CODEC
                                    .optionalFieldOf(
                                            "rotation",
                                            FixedJigsawRotation.NONE
                                    )
                                    .forGetter(structure -> structure.rotation),
                            Codec.intRange(0, MAX_CLEARANCE)
                                    .optionalFieldOf(
                                            "minimum_clearance_below_top",
                                            DEFAULT_MINIMUM_CLEARANCE_BELOW_TOP
                                    )
                                    .forGetter(
                                            structure ->
                                                    structure.minimumClearanceBelowTop
                                    ),
                            Codec.intRange(0, MAX_SEARCH_RADIUS)
                                    .optionalFieldOf(
                                            "terrain_search_radius",
                                            DEFAULT_TERRAIN_SEARCH_RADIUS
                                    )
                                    .forGetter(
                                            structure ->
                                                    structure.terrainSearchRadius
                                    ),
                            Codec.intRange(1, MAX_SEARCH_STEP)
                                    .optionalFieldOf(
                                            "terrain_search_step",
                                            DEFAULT_TERRAIN_SEARCH_STEP
                                    )
                                    .forGetter(
                                            structure ->
                                                    structure.terrainSearchStep
                                    )
                    ).apply(instance, FixedJigsawStructure::new)
            ).validate(FixedJigsawStructure::verifyConfiguration);

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final List<PoolAliasBinding> poolAliases;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;
    private final FixedJigsawRotation rotation;
    private final int minimumClearanceBelowTop;
    private final int terrainSearchRadius;
    private final int terrainSearchStep;

    public FixedJigsawStructure(
            StructureSettings settings,
            Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName,
            int maxDepth,
            HeightProvider startHeight,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            int maxDistanceFromCenter,
            List<PoolAliasBinding> poolAliases,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings,
            FixedJigsawRotation rotation,
            int minimumClearanceBelowTop,
            int terrainSearchRadius,
            int terrainSearchStep
    ) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.poolAliases = poolAliases;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
        this.rotation = rotation;
        this.minimumClearanceBelowTop = minimumClearanceBelowTop;
        this.terrainSearchRadius = terrainSearchRadius;
        this.terrainSearchStep = terrainSearchStep;
    }

    private static DataResult<FixedJigsawStructure> verifyConfiguration(
            FixedJigsawStructure structure
    ) {
        int terrainMargin = switch (structure.terrainAdaptation()) {
            case NONE -> 0;
            case BURY, BEARD_THIN, BEARD_BOX, ENCAPSULATE -> 12;
        };

        if (structure.maxDistanceFromCenter + terrainMargin
                > MAX_TOTAL_STRUCTURE_RANGE) {
            return DataResult.error(
                    () -> "Structure size including terrain adaptation "
                            + "must not exceed "
                            + MAX_TOTAL_STRUCTURE_RANGE
            );
        }

        if (structure.terrainSearchRadius > 0
                && structure.terrainSearchStep
                > structure.terrainSearchRadius) {
            return DataResult.error(
                    () -> "terrain_search_step must be less than or equal to "
                            + "terrain_search_radius, unless the radius is 0"
            );
        }

        return DataResult.success(structure);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(
            GenerationContext context
    ) {
        ChunkPos chunkPos = context.chunkPos();
        WorldGenerationContext worldContext =
                new WorldGenerationContext(
                        context.chunkGenerator(),
                        context.heightAccessor()
                );

        /*
         * When heightmap projection is enabled, vanilla treats this sampled
         * value as an offset added to the terrain height.
         */
        int sampledStartHeight = startHeight.sample(
                context.random(),
                worldContext
        );

        BlockPos startPos = new BlockPos(
                chunkPos.getMinBlockX(),
                sampledStartHeight,
                chunkPos.getMinBlockZ()
        );

        Supplier<Optional<GenerationStub>> generate = () ->
                JigsawPlacement.addPieces(
                        context,
                        startPool,
                        startJigsawName,
                        maxDepth,
                        startPos,
                        useExpansionHack,
                        projectStartToHeightmap,
                        maxDistanceFromCenter,
                        PoolAliasLookup.create(
                                poolAliases,
                                startPos,
                                context.seed()
                        ),
                        dimensionPadding,
                        liquidSettings
                );

        Optional<GenerationStub> generated;

        if (rotation.isRandom()) {
            generated = generate.get();
        } else {
            generated = FixedJigsawRotationContext.withRotation(
                    rotation.fixedRotation(),
                    generate
            );
        }

        return generated.map(stub ->
                FixedJigsawTerrainPlacement.adjust(
                        context,
                        stub,
                        projectStartToHeightmap,
                        sampledStartHeight,
                        minimumClearanceBelowTop,
                        terrainSearchRadius,
                        terrainSearchStep,
                        dimensionPadding
                )
        );
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.FIXED_JIGSAW.get();
    }
}
