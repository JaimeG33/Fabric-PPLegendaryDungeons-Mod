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
 * Vanilla-compatible jigsaw structure whose root piece can use a configured
 * horizontal rotation instead of vanilla's random root rotation.
 *
 * <p>Only the initial/root rotation is overridden. Child pieces continue to use
 * vanilla jigsaw connector logic and may rotate as needed to connect.</p>
 */
public final class FixedJigsawStructure extends Structure {
    public static final DimensionPadding DEFAULT_DIMENSION_PADDING =
            DimensionPadding.ZERO;
    public static final LiquidSettings DEFAULT_LIQUID_SETTINGS =
            LiquidSettings.APPLY_WATERLOGGING;

    private static final int MAX_TOTAL_STRUCTURE_RANGE = 128;

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
                                    .forGetter(structure -> structure.rotation)
                    ).apply(instance, FixedJigsawStructure::new)
            ).validate(FixedJigsawStructure::verifyRange);

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
            FixedJigsawRotation rotation
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
    }

    private static DataResult<FixedJigsawStructure> verifyRange(
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

        return DataResult.success(structure);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(
            GenerationContext context
    ) {
        ChunkPos chunkPos = context.chunkPos();
        int startY = startHeight.sample(
                context.random(),
                new WorldGenerationContext(
                        context.chunkGenerator(),
                        context.heightAccessor()
                )
        );
        BlockPos startPos = new BlockPos(
                chunkPos.getMinBlockX(),
                startY,
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

        if (rotation.isRandom()) {
            return generate.get();
        }

        return FixedJigsawRotationContext.withRotation(
                rotation.fixedRotation(),
                generate
        );
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.FIXED_JIGSAW.get();
    }
}
