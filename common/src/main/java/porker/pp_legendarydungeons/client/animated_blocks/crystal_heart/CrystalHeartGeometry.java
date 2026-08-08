package porker.pp_legendarydungeons.client.animated_blocks.crystal_heart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;

/**
 * Loader-neutral normalized mesh for the Crystal Heart.
 *
 * <p>The mesh is a square bipyramid (an elongated plumbob/diamond). X/Z span
 * -0.5..0.5 and Y spans 0..1. The renderer scales this normalized mesh to the
 * block entity's requested width and height, so no alternate model files are
 * needed for different sizes.</p>
 *
 * <p>Unlike the original proof-of-concept, geometry positions and texture UVs
 * are separate. Each of the eight triangular faces owns its own section of a
 * 4x2 face atlas. This avoids projecting one square image across every side of
 * the crystal and allows the artwork to be designed specifically for each
 * physical face.</p>
 */
public final class CrystalHeartGeometry {
    private static final float ATLAS_WIDTH = 64.0F;
    private static final float ATLAS_HEIGHT = 32.0F;

    private static final Position TOP = new Position(0.0F, 1.0F, 0.0F);
    private static final Position BOTTOM = new Position(0.0F, 0.0F, 0.0F);

    private static final Position NORTH = new Position(0.0F, 0.46F, -0.5F);
    private static final Position EAST = new Position(0.5F, 0.46F, 0.0F);
    private static final Position SOUTH = new Position(0.0F, 0.46F, 0.5F);
    private static final Position WEST = new Position(-0.5F, 0.46F, 0.0F);

    private static final Face[] FACES = new Face[] {
            // Upper row: cells 0..3.
            upperFace(0, TOP, EAST, NORTH, 255, 255, 255),
            upperFace(1, TOP, SOUTH, EAST, 235, 255, 240),
            upperFace(2, TOP, WEST, SOUTH, 210, 235, 218),
            upperFace(3, TOP, NORTH, WEST, 242, 255, 246),

            // Lower row: cells 0..3.
            lowerFace(0, BOTTOM, NORTH, EAST, 238, 255, 242),
            lowerFace(1, BOTTOM, EAST, SOUTH, 220, 245, 228),
            lowerFace(2, BOTTOM, SOUTH, WEST, 198, 225, 208),
            lowerFace(3, BOTTOM, WEST, NORTH, 228, 250, 235)
    };

    private CrystalHeartGeometry() {
    }

    public static void render(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay
    ) {
        for (Face face : FACES) {
            renderTriangleAsQuad(pose, consumer, face, packedLight, packedOverlay);
        }
    }

    private static Face upperFace(
            int cell,
            Position apex,
            Position rightBase,
            Position leftBase,
            int red,
            int green,
            int blue
    ) {
        float x = cell * 16.0F;

        return new Face(
                apex,
                rightBase,
                leftBase,
                uv(x + 8.0F, 1.0F),
                uv(x + 15.0F, 15.0F),
                uv(x + 1.0F, 15.0F),
                red,
                green,
                blue
        );
    }

    private static Face lowerFace(
            int cell,
            Position apex,
            Position leftBase,
            Position rightBase,
            int red,
            int green,
            int blue
    ) {
        float x = cell * 16.0F;

        return new Face(
                apex,
                leftBase,
                rightBase,
                uv(x + 8.0F, 31.0F),
                uv(x + 1.0F, 17.0F),
                uv(x + 15.0F, 17.0F),
                red,
                green,
                blue
        );
    }

    private static Uv uv(float pixelX, float pixelY) {
        return new Uv(pixelX / ATLAS_WIDTH, pixelY / ATLAS_HEIGHT);
    }

    /**
     * Minecraft's entity-style RenderTypes use quad mode. A triangular crystal
     * face is therefore emitted as a degenerate quad by repeating the third
     * corner. Visually this is still one triangle.
     */
    private static void renderTriangleAsQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            Face face,
            int packedLight,
            int packedOverlay
    ) {
        Vector3f ab = new Vector3f(
                face.b.x - face.a.x,
                face.b.y - face.a.y,
                face.b.z - face.a.z
        );
        Vector3f ac = new Vector3f(
                face.c.x - face.a.x,
                face.c.y - face.a.y,
                face.c.z - face.a.z
        );
        Vector3f normal = ab.cross(ac).normalize();

        emitVertex(
                pose, consumer, face.a, face.uvA, face, normal, packedLight, packedOverlay
        );
        emitVertex(
                pose, consumer, face.b, face.uvB, face, normal, packedLight, packedOverlay
        );
        emitVertex(
                pose, consumer, face.c, face.uvC, face, normal, packedLight, packedOverlay
        );
        emitVertex(
                pose, consumer, face.c, face.uvC, face, normal, packedLight, packedOverlay
        );
    }

    private static void emitVertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            Position position,
            Uv uv,
            Face face,
            Vector3f normal,
            int packedLight,
            int packedOverlay
    ) {
        consumer.addVertex(pose, position.x, position.y, position.z)
                .setColor(face.red, face.green, face.blue, 255)
                .setUv(uv.u, uv.v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, normal.x(), normal.y(), normal.z());
    }

    private record Position(float x, float y, float z) {
    }

    private record Uv(float u, float v) {
    }

    private record Face(
            Position a,
            Position b,
            Position c,
            Uv uvA,
            Uv uvB,
            Uv uvC,
            int red,
            int green,
            int blue
    ) {
    }
}
