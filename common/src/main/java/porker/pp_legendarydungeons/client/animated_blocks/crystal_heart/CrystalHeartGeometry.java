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
 * <p>The upper four faces share one full-resolution triangular texture and the
 * lower four faces share another. Alternate faces mirror the UVs and use subtle
 * tint differences so the crystal keeps visible facet changes while rotating
 * without forcing eight separate art files.</p>
 */
public final class CrystalHeartGeometry {
    private static final Position TOP = new Position(0.0F, 1.0F, 0.0F);
    private static final Position BOTTOM = new Position(0.0F, 0.0F, 0.0F);

    private static final Position NORTH = new Position(0.0F, 0.46F, -0.5F);
    private static final Position EAST = new Position(0.5F, 0.46F, 0.0F);
    private static final Position SOUTH = new Position(0.0F, 0.46F, 0.5F);
    private static final Position WEST = new Position(-0.5F, 0.46F, 0.0F);

    private static final Uv TOP_APEX = new Uv(0.50F, 0.015625F);
    private static final Uv TOP_LEFT = new Uv(0.015625F, 0.984375F);
    private static final Uv TOP_RIGHT = new Uv(0.984375F, 0.984375F);

    private static final Uv BOTTOM_APEX = new Uv(0.50F, 0.984375F);
    private static final Uv BOTTOM_LEFT = new Uv(0.015625F, 0.015625F);
    private static final Uv BOTTOM_RIGHT = new Uv(0.984375F, 0.015625F);

    private static final Face[] UPPER_FACES = new Face[] {
            face(TOP, EAST, NORTH, false, 255, 255, 255),
            face(TOP, SOUTH, EAST, true, 238, 255, 242),
            face(TOP, WEST, SOUTH, false, 208, 232, 215),
            face(TOP, NORTH, WEST, true, 244, 255, 247)
    };

    private static final Face[] LOWER_FACES = new Face[] {
            face(BOTTOM, NORTH, EAST, false, 242, 255, 245),
            face(BOTTOM, EAST, SOUTH, true, 222, 246, 229),
            face(BOTTOM, SOUTH, WEST, false, 196, 222, 205),
            face(BOTTOM, WEST, NORTH, true, 232, 251, 238)
    };

    private CrystalHeartGeometry() {
    }

    public static void renderTop(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay
    ) {
        for (Face face : UPPER_FACES) {
            Uv left = face.mirrored ? TOP_RIGHT : TOP_LEFT;
            Uv right = face.mirrored ? TOP_LEFT : TOP_RIGHT;

            renderTriangleAsQuad(
                    pose,
                    consumer,
                    face,
                    TOP_APEX,
                    right,
                    left,
                    packedLight,
                    packedOverlay
            );
        }
    }

    public static void renderBottom(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay
    ) {
        for (Face face : LOWER_FACES) {
            Uv left = face.mirrored ? BOTTOM_RIGHT : BOTTOM_LEFT;
            Uv right = face.mirrored ? BOTTOM_LEFT : BOTTOM_RIGHT;

            renderTriangleAsQuad(
                    pose,
                    consumer,
                    face,
                    BOTTOM_APEX,
                    left,
                    right,
                    packedLight,
                    packedOverlay
            );
        }
    }

    private static Face face(
            Position a,
            Position b,
            Position c,
            boolean mirrored,
            int red,
            int green,
            int blue
    ) {
        return new Face(a, b, c, mirrored, red, green, blue);
    }

    /**
     * Minecraft's entity-style RenderTypes use quad mode. A triangular crystal
     * face is emitted as a degenerate quad by repeating the third corner.
     */
    private static void renderTriangleAsQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            Face face,
            Uv uvA,
            Uv uvB,
            Uv uvC,
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
                pose, consumer, face.a, uvA, face, normal, packedLight, packedOverlay
        );
        emitVertex(
                pose, consumer, face.b, uvB, face, normal, packedLight, packedOverlay
        );
        emitVertex(
                pose, consumer, face.c, uvC, face, normal, packedLight, packedOverlay
        );
        emitVertex(
                pose, consumer, face.c, uvC, face, normal, packedLight, packedOverlay
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
            boolean mirrored,
            int red,
            int green,
            int blue
    ) {
    }
}
