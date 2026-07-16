package porker.pp_legendarydungeons.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Post-processes one generated fixed-jigsaw piece set without rebuilding or
 * separating its pieces.
 *
 * <p>The nearby search is deterministic. It does not create extra structure
 * starts and it never rejects the original start merely because no lower nearby
 * site was found. The fallback is always a vertical clamp at the original X/Z
 * location.</p>
 */
public final class FixedJigsawTerrainPlacement {
    private static final int FOUNDATION_INSET = 2;
    private static final long SLOPE_WEIGHT = 16L;

    private FixedJigsawTerrainPlacement() {
    }

    public static Structure.GenerationStub adjust(
            Structure.GenerationContext context,
            Structure.GenerationStub originalStub,
            Optional<Heightmap.Types> projectedHeightmap,
            int sampledStartHeight,
            int minimumClearanceBelowTop,
            int searchRadius,
            int searchStep,
            DimensionPadding dimensionPadding
    ) {
        /*
         * Materialize the vanilla jigsaw generator exactly once. All later
         * movement is applied to the already-connected piece set as one unit.
         */
        StructurePiecesBuilder materializedBuilder =
                originalStub.getPiecesBuilder();
        List<StructurePiece> pieces =
                materializedBuilder.build().pieces();

        if (pieces.isEmpty()
                || !(pieces.getFirst()
                instanceof PoolElementStructurePiece rootPiece)) {
            return materializedStub(originalStub.position(), pieces);
        }

        BoundingBox rootBounds = rootPiece.getBoundingBox();
        BoundingBox completeBounds = materializedBuilder.getBoundingBox();

        int currentGroundY =
                rootBounds.minY() + rootPiece.getGroundLevelDelta();

        WorldGenerationContext worldContext =
                new WorldGenerationContext(
                        context.chunkGenerator(),
                        context.heightAccessor()
                );

        /*
         * This exactly matches a vanilla below_top anchor. In the normal
         * Overworld, a clearance of 175 resolves to Y=144:
         * top usable block 319 minus 175.
         */
        int maximumStartY = VerticalAnchor
                .belowTop(minimumClearanceBelowTop)
                .resolveY(worldContext);

        int moveX = 0;
        int moveZ = 0;
        int targetGroundY = Math.min(currentGroundY, maximumStartY);

        /*
         * Search only when the normal projected surface is above the cap.
         * Normal-height structures therefore retain vanilla placement.
         */
        if (currentGroundY > maximumStartY
                && projectedHeightmap.isPresent()
                && searchRadius > 0) {
            Optional<Candidate> candidate = findBestCandidate(
                    context,
                    originalStub.position(),
                    rootBounds,
                    currentGroundY,
                    projectedHeightmap.get(),
                    sampledStartHeight,
                    maximumStartY,
                    searchRadius,
                    searchStep
            );

            if (candidate.isPresent()) {
                Candidate chosen = candidate.get();
                moveX = chosen.offsetX();
                moveZ = chosen.offsetZ();
                targetGroundY = chosen.groundY();
            }
        }

        int moveY = targetGroundY - currentGroundY;

        /*
         * Independent final safety net: even if a future pool creates a piece
         * taller than expected, move the complete result down far enough that
         * its actual top bounding box remains inside the dimension.
         */
        int maximumStructureBlockY =
                context.heightAccessor().getMaxBuildHeight()
                        - 1
                        - dimensionPadding.top();

        int shiftedTop = completeBounds.maxY() + moveY;

        if (shiftedTop > maximumStructureBlockY) {
            moveY -= shiftedTop - maximumStructureBlockY;
        }

        if (moveX != 0 || moveY != 0 || moveZ != 0) {
            for (StructurePiece piece : pieces) {
                movePieceAndJunctions(piece, moveX, moveY, moveZ);
            }

            LegendaryDungeons.LOGGER.debug(
                    "Adjusted fixed-jigsaw structure by ({}, {}, {}) "
                            + "to preserve {} blocks of top clearance",
                    moveX,
                    moveY,
                    moveZ,
                    minimumClearanceBelowTop
            );
        }

        /*
         * Keep the original generation-stub position as the biome-validation
         * marker. Candidate X/Z positions are checked explicitly above, while
         * retaining the original marker prevents a downward-only clamp from
         * becoming rarer because Minecraft samples a different 3D biome at the
         * lower Y value.
         */
        return materializedStub(originalStub.position(), pieces);
    }

    private static void movePieceAndJunctions(
            StructurePiece piece,
            int moveX,
            int moveY,
            int moveZ
    ) {
        /*
         * PoolElementStructurePiece.move updates its block position and
         * bounding box, but its stored jigsaw junction coordinates are separate
         * immutable values used by terrain adaptation. Rebuild them at the
         * translated coordinates before moving the piece.
         */
        if (piece instanceof PoolElementStructurePiece poolPiece
                && !poolPiece.getJunctions().isEmpty()) {
            List<JigsawJunction> movedJunctions =
                    poolPiece.getJunctions()
                            .stream()
                            .map(junction ->
                                    new JigsawJunction(
                                            junction.getSourceX() + moveX,
                                            junction.getSourceGroundY() + moveY,
                                            junction.getSourceZ() + moveZ,
                                            junction.getDeltaY(),
                                            junction.getDestProjection()
                                    )
                            )
                            .toList();

            poolPiece.getJunctions().clear();
            poolPiece.getJunctions().addAll(movedJunctions);
        }

        piece.move(moveX, moveY, moveZ);
    }

    private static Optional<Candidate> findBestCandidate(
            Structure.GenerationContext context,
            BlockPos originalStubPosition,
            BoundingBox originalRootBounds,
            int currentGroundY,
            Heightmap.Types heightmap,
            int sampledStartHeight,
            int maximumStartY,
            int searchRadius,
            int searchStep
    ) {
        Candidate best = null;
        Comparator<Candidate> comparator =
                Comparator.comparingLong(Candidate::score)
                        .thenComparingInt(
                                candidate -> -candidate.groundY()
                        )
                        .thenComparingInt(Candidate::offsetX)
                        .thenComparingInt(Candidate::offsetZ);

        long maximumDistanceSquared =
                (long) searchRadius * searchRadius;

        int maximumStepIndex = searchRadius / searchStep;

        for (int xIndex = -maximumStepIndex;
             xIndex <= maximumStepIndex;
             xIndex++) {
            int offsetX = xIndex * searchStep;

            for (int zIndex = -maximumStepIndex;
                 zIndex <= maximumStepIndex;
                 zIndex++) {
                int offsetZ = zIndex * searchStep;

                if (offsetX == 0 && offsetZ == 0) {
                    continue;
                }

                long distanceSquared =
                        (long) offsetX * offsetX
                                + (long) offsetZ * offsetZ;

                if (distanceSquared > maximumDistanceSquared) {
                    continue;
                }

                int centerX =
                        xCenter(originalRootBounds)
                                + offsetX;
                int centerZ =
                        zCenter(originalRootBounds)
                                + offsetZ;

                int surfaceY = terrainHeight(
                        context,
                        centerX,
                        centerZ,
                        heightmap
                );
                int candidateGroundY =
                        surfaceY + sampledStartHeight;

                if (candidateGroundY > maximumStartY) {
                    continue;
                }

                BlockPos candidateStubPosition =
                        originalStubPosition.offset(
                                offsetX,
                                candidateGroundY - currentGroundY,
                                offsetZ
                        );

                if (!hasValidBiome(context, candidateStubPosition)) {
                    continue;
                }

                int slopeRange = sampleFoundationSlope(
                        context,
                        originalRootBounds,
                        offsetX,
                        offsetZ,
                        heightmap
                );

                long score =
                        distanceSquared
                                + (long) slopeRange
                                * slopeRange
                                * SLOPE_WEIGHT;

                Candidate candidate =
                        new Candidate(
                                offsetX,
                                offsetZ,
                                candidateGroundY,
                                slopeRange,
                                score
                        );

                if (best == null
                        || comparator.compare(candidate, best) < 0) {
                    best = candidate;
                }
            }
        }

        return Optional.ofNullable(best);
    }

    /**
     * Samples a 3x3 grid under the root piece. The range is used only to rank
     * valid candidates; it never rejects a structure and therefore cannot
     * reduce structure frequency.
     */
    private static int sampleFoundationSlope(
            Structure.GenerationContext context,
            BoundingBox rootBounds,
            int offsetX,
            int offsetZ,
            Heightmap.Types heightmap
    ) {
        int minX = rootBounds.minX() + offsetX;
        int maxX = rootBounds.maxX() + offsetX;
        int minZ = rootBounds.minZ() + offsetZ;
        int maxZ = rootBounds.maxZ() + offsetZ;

        int insetX = Math.min(
                FOUNDATION_INSET,
                Math.max(0, (maxX - minX) / 4)
        );
        int insetZ = Math.min(
                FOUNDATION_INSET,
                Math.max(0, (maxZ - minZ) / 4)
        );

        int[] sampleXs = {
                minX + insetX,
                (minX + maxX) / 2,
                maxX - insetX
        };
        int[] sampleZs = {
                minZ + insetZ,
                (minZ + maxZ) / 2,
                maxZ - insetZ
        };

        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;

        for (int sampleX : sampleXs) {
            for (int sampleZ : sampleZs) {
                int height = terrainHeight(
                        context,
                        sampleX,
                        sampleZ,
                        heightmap
                );
                minimum = Math.min(minimum, height);
                maximum = Math.max(maximum, height);
            }
        }

        return maximum - minimum;
    }

    private static int terrainHeight(
            Structure.GenerationContext context,
            int x,
            int z,
            Heightmap.Types heightmap
    ) {
        return context.chunkGenerator().getFirstFreeHeight(
                x,
                z,
                heightmap,
                context.heightAccessor(),
                context.randomState()
        );
    }

    private static boolean hasValidBiome(
            Structure.GenerationContext context,
            BlockPos position
    ) {
        Holder<Biome> biome =
                context.chunkGenerator()
                        .getBiomeSource()
                        .getNoiseBiome(
                                QuartPos.fromBlock(position.getX()),
                                QuartPos.fromBlock(position.getY()),
                                QuartPos.fromBlock(position.getZ()),
                                context.randomState().sampler()
                        );

        return context.validBiome().test(biome);
    }

    private static int xCenter(BoundingBox bounds) {
        return (bounds.minX() + bounds.maxX()) / 2;
    }

    private static int zCenter(BoundingBox bounds) {
        return (bounds.minZ() + bounds.maxZ()) / 2;
    }

    private static Structure.GenerationStub materializedStub(
            BlockPos position,
            List<StructurePiece> pieces
    ) {
        return new Structure.GenerationStub(
                position,
                targetBuilder ->
                        pieces.forEach(targetBuilder::addPiece)
        );
    }

    private record Candidate(
            int offsetX,
            int offsetZ,
            int groundY,
            int slopeRange,
            long score
    ) {
    }
}
