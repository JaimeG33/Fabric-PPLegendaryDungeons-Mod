package porker.pp_legendarydungeons.worldgen.frozen_adventurers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shared implementation behind the IceSpikeFeature and IcebergFeature mixins.
 *
 * <p>The vanilla feature call remains responsible for deciding when and where
 * an attempt occurs. This class only performs a second deterministic roll. A
 * successful roll substitutes one prebuilt exterior and one independently
 * selected armor-stand/map interior for that single vanilla attempt.</p>
 *
 * <p>The exterior and interior are joined through the saved jigsaw whose
 * {@code name} is {@value #ANCHOR_PATH}. The jigsaw pool and target fields are
 * intentionally not used: features do not create a StructureStart or run the
 * recursive jigsaw engine. Reading the connector directly gives the same stable
 * alignment while keeping the replacement safe for feature generation.</p>
 */
public final class FrozenAdventurerFeatureSubstitution {
    private static final String ANCHOR_PATH = "frozen_adventurers/the_chud_anchor";
    private static final ResourceLocation ANCHOR_ID = id(ANCHOR_PATH);

    /**
     * Odds are per actual vanilla Feature.place invocation. Vanilla or modded
     * placed-feature count/rarity modifiers still run before these rolls.
     * Temporarily change a denominator to 1 when visually testing generation.
     */
    private static final int ICE_SPIKE_REPLACEMENT_DENOMINATOR = 100;
    private static final int ICEBERG_REPLACEMENT_DENOMINATOR = 50;

    private static final long ICE_SPIKE_SALT = 0x49C85F5D49B72D13L;
    private static final long ICEBERG_SALT = 0x6E1D8A7C2F314B59L;

    private static final TagKey<Block> ICEBERG_BLOCKS = TagKey.create(
            Registries.BLOCK,
            id("frozen_adventurers/iceberg_blocks")
    );

    private static final List<ExteriorVariant> ICE_SPIKE_EXTERIORS = List.of(
            new ExteriorVariant(id("special/frozen_adventurers/exterior/ice_spike/icespike_1"), 0),
            new ExteriorVariant(id("special/frozen_adventurers/exterior/ice_spike/icespike_2"), 0),
            new ExteriorVariant(id("special/frozen_adventurers/exterior/ice_spike/icespike_tall_1"), 0),
            new ExteriorVariant(id("special/frozen_adventurers/exterior/ice_spike/icespike_tall_2"), 0),
            new ExteriorVariant(id("special/frozen_adventurers/exterior/ice_spike/icespike_tall_3"), 0)
    );

    /**
     * All four current iceberg templates were built with ten local layers below
     * their intended waterline. The local Y=10 layer is aligned with the chunk
     * generator's sea level. Change an individual value if a future exterior
     * uses a different waterline.
     */
    private static final List<ExteriorVariant> ICEBERG_EXTERIORS = List.of(
            new ExteriorVariant(id("special/frozen_adventurers/exterior/iceberg/ice_berg_1"), 10),
            new ExteriorVariant(id("special/frozen_adventurers/exterior/iceberg/ice_berg_2"), 10),
            new ExteriorVariant(id("special/frozen_adventurers/exterior/iceberg/ice_berg_3"), 10),
            new ExteriorVariant(id("special/frozen_adventurers/exterior/iceberg/ice_berg_4"), 10)
    );

    /**
     * Equal selection gives each planned map category a 25% chance after an
     * exterior replacement has been chosen.
     */
    private static final List<ResourceLocation> INTERIORS = List.of(
            id("special/frozen_adventurers/the_chud/chud_aboutpost"),
            id("special/frozen_adventurers/the_chud/chud_dungeon"),
            id("special/frozen_adventurers/the_chud/chud_misc"),
            id("special/frozen_adventurers/the_chud/chud_outside")
    );

    private static final Rotation[] ROTATIONS = {
            Rotation.NONE,
            Rotation.CLOCKWISE_90,
            Rotation.CLOCKWISE_180,
            Rotation.COUNTERCLOCKWISE_90
    };

    private FrozenAdventurerFeatureSubstitution() {
    }

    public static boolean tryPlaceIceSpike(
            FeaturePlaceContext<NoneFeatureConfiguration> context
    ) {
        BlockPos surface = findVanillaSpikeSurface(context.level(), context.origin());

        if (surface == null) {
            return false;
        }

        RandomSource replacementRandom = replacementRandom(
                context.level(),
                context.origin(),
                ICE_SPIKE_SALT
        );

        if (replacementRandom.nextInt(ICE_SPIKE_REPLACEMENT_DENOMINATOR) != 0) {
            return false;
        }

        ExteriorVariant exterior = choose(ICE_SPIKE_EXTERIORS, replacementRandom);

        return placeEncounter(
                context.level(),
                surface,
                surface.getY() - exterior.referenceLocalY(),
                exterior,
                replacementRandom
        );
    }

    public static boolean tryPlaceIceberg(
            FeaturePlaceContext<BlockStateConfiguration> context
    ) {
        if (!context.config().state.is(ICEBERG_BLOCKS)) {
            return false;
        }

        RandomSource replacementRandom = replacementRandom(
                context.level(),
                context.origin(),
                ICEBERG_SALT
        );

        if (replacementRandom.nextInt(ICEBERG_REPLACEMENT_DENOMINATOR) != 0) {
            return false;
        }

        ExteriorVariant exterior = choose(ICEBERG_EXTERIORS, replacementRandom);
        int seaLevel = context.chunkGenerator().getSeaLevel();
        BlockPos center = new BlockPos(
                context.origin().getX(),
                seaLevel,
                context.origin().getZ()
        );

        return placeEncounter(
                context.level(),
                center,
                seaLevel - exterior.referenceLocalY(),
                exterior,
                replacementRandom
        );
    }

    private static BlockPos findVanillaSpikeSurface(
            WorldGenLevel level,
            BlockPos origin
    ) {
        BlockPos.MutableBlockPos surface = origin.mutable();

        while (level.isEmptyBlock(surface)
                && surface.getY() > level.getMinBuildHeight() + 2) {
            surface.move(Direction.DOWN);
        }

        return level.getBlockState(surface).is(Blocks.SNOW_BLOCK)
                ? surface.immutable()
                : null;
    }

    private static boolean placeEncounter(
            WorldGenLevel level,
            BlockPos horizontalCenter,
            int exteriorBaseY,
            ExteriorVariant exteriorVariant,
            RandomSource random
    ) {
        StructureTemplateManager manager = level.getLevel().getStructureManager();
        Optional<StructureTemplate> exteriorOptional = manager.get(exteriorVariant.templateId());
        ResourceLocation interiorId = choose(INTERIORS, random);
        Optional<StructureTemplate> interiorOptional = manager.get(interiorId);

        if (exteriorOptional.isEmpty() || interiorOptional.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "Could not prepare frozen-adventurer feature: exterior={}, interior={}",
                    exteriorVariant.templateId(),
                    interiorId
            );
            return false;
        }

        StructureTemplate exterior = exteriorOptional.get();
        StructureTemplate interior = interiorOptional.get();
        Rotation exteriorRotation = Rotation.getRandom(random);
        StructurePlaceSettings exteriorMetadataSettings = metadataSettings(exteriorRotation);
        List<StructureTemplate.StructureBlockInfo> exteriorAnchors = findAnchors(
                exterior,
                exteriorMetadataSettings
        );

        if (exteriorAnchors.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "Frozen-adventurer exterior {} has no named anchor {}.",
                    exteriorVariant.templateId(),
                    ANCHOR_ID
            );
            return false;
        }

        StructureTemplate.StructureBlockInfo exteriorAnchor = choose(
                exteriorAnchors,
                random
        );
        Direction exteriorFront = JigsawBlock.getFrontFacing(exteriorAnchor.state());
        Optional<RotatedAnchor> interiorAnchorOptional = findCompatibleInteriorAnchor(
                interior,
                exteriorFront,
                random
        );

        if (interiorAnchorOptional.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "Frozen-adventurer interior {} has no connector facing opposite {}.",
                    interiorId,
                    exteriorFront
            );
            return false;
        }

        RotatedAnchor interiorAnchor = interiorAnchorOptional.get();
        BlockPos exteriorOrigin = centeredOrigin(
                exterior,
                exteriorMetadataSettings,
                horizontalCenter,
                exteriorBaseY
        );
        BlockPos exteriorAnchorWorld = exteriorOrigin.offset(exteriorAnchor.pos());
        BlockPos desiredInteriorAnchorWorld = exteriorAnchorWorld.relative(exteriorFront);
        BlockPos interiorOrigin = desiredInteriorAnchorWorld.subtract(
                interiorAnchor.info().pos()
        );

        StructurePlaceSettings interiorMetadataSettings = metadataSettings(
                interiorAnchor.rotation()
        );
        BoundingBox exteriorBox = exterior.getBoundingBox(
                exteriorMetadataSettings,
                exteriorOrigin
        );
        BoundingBox interiorBox = interior.getBoundingBox(
                interiorMetadataSettings,
                interiorOrigin
        );

        if (!insideBuildHeight(level, exteriorBox)
                || !insideBuildHeight(level, interiorBox)) {
            return false;
        }

        boolean exteriorPlaced = exterior.placeInWorld(
                level,
                exteriorOrigin,
                exteriorOrigin,
                placementSettings(exteriorRotation, random),
                random,
                2
        );

        if (!exteriorPlaced) {
            return false;
        }

        boolean interiorPlaced = interior.placeInWorld(
                level,
                interiorOrigin,
                interiorOrigin,
                placementSettings(interiorAnchor.rotation(), random),
                random,
                2
        );

        if (!interiorPlaced) {
            /*
             * The exterior has already been committed. Report success so the
             * vanilla feature does not generate over a partially placed custom
             * exterior. Valid saved interiors should not reach this branch.
             */
            LegendaryDungeons.LOGGER.error(
                    "Placed frozen-adventurer exterior {}, but interior {} failed.",
                    exteriorVariant.templateId(),
                    interiorId
            );
        }

        return true;
    }

    private static List<StructureTemplate.StructureBlockInfo> findAnchors(
            StructureTemplate template,
            StructurePlaceSettings settings
    ) {
        List<StructureTemplate.StructureBlockInfo> result = new ArrayList<>();

        for (StructureTemplate.StructureBlockInfo info
                : template.filterBlocks(BlockPos.ZERO, settings, Blocks.JIGSAW)) {
            if (info.nbt() != null
                    && ANCHOR_ID.toString().equals(info.nbt().getString("name"))) {
                result.add(info);
            }
        }

        return result;
    }

    private static Optional<RotatedAnchor> findCompatibleInteriorAnchor(
            StructureTemplate interior,
            Direction exteriorFront,
            RandomSource random
    ) {
        int start = random.nextInt(ROTATIONS.length);

        for (int rotationOffset = 0;
             rotationOffset < ROTATIONS.length;
             rotationOffset++) {
            Rotation rotation = ROTATIONS[
                    (start + rotationOffset) % ROTATIONS.length
            ];
            StructurePlaceSettings settings = metadataSettings(rotation);

            for (StructureTemplate.StructureBlockInfo anchor
                    : findAnchors(interior, settings)) {
                if (JigsawBlock.getFrontFacing(anchor.state())
                        == exteriorFront.getOpposite()) {
                    return Optional.of(new RotatedAnchor(rotation, anchor));
                }
            }
        }

        return Optional.empty();
    }

    private static BlockPos centeredOrigin(
            StructureTemplate template,
            StructurePlaceSettings settings,
            BlockPos desiredCenter,
            int desiredMinimumY
    ) {
        BoundingBox localBox = template.getBoundingBox(settings, BlockPos.ZERO);
        int localCenterX = Math.floorDiv(localBox.minX() + localBox.maxX(), 2);
        int localCenterZ = Math.floorDiv(localBox.minZ() + localBox.maxZ(), 2);

        return new BlockPos(
                desiredCenter.getX() - localCenterX,
                desiredMinimumY - localBox.minY(),
                desiredCenter.getZ() - localCenterZ
        );
    }

    private static boolean insideBuildHeight(
            WorldGenLevel level,
            BoundingBox box
    ) {
        return box.minY() >= level.getMinBuildHeight()
                && box.maxY() < level.getMaxBuildHeight();
    }

    private static StructurePlaceSettings metadataSettings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation)
                .setIgnoreEntities(false);
    }

    private static StructurePlaceSettings placementSettings(
            Rotation rotation,
            RandomSource random
    ) {
        return metadataSettings(rotation)
                .setRandom(random)
                .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
                .addProcessor(JigsawReplacementProcessor.INSTANCE);
    }

    private static RandomSource replacementRandom(
            WorldGenLevel level,
            BlockPos origin,
            long salt
    ) {
        long seed = level.getSeed() ^ salt;
        seed ^= (long) origin.getX() * 341873128712L;
        seed ^= (long) origin.getY() * 132897987541L;
        seed ^= (long) origin.getZ() * 42317861L;
        seed ^= seed >>> 33;
        seed *= 0xff51afd7ed558ccdL;
        seed ^= seed >>> 33;
        seed *= 0xc4ceb9fe1a85ec53L;
        seed ^= seed >>> 33;
        return RandomSource.create(seed);
    }

    private static <T> T choose(List<T> values, RandomSource random) {
        return values.get(random.nextInt(values.size()));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                LegendaryDungeons.MOD_ID,
                path
        );
    }

    private record ExteriorVariant(
            ResourceLocation templateId,
            int referenceLocalY
    ) {
    }

    private record RotatedAnchor(
            Rotation rotation,
            StructureTemplate.StructureBlockInfo info
    ) {
    }
}
