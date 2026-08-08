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
 */
public final class CrystalHeartGeometry {
    private static final Vertex TOP = new Vertex(0.0F, 1.0F, 0.0F, 0.5F, 0.0F);
    private static final Vertex BOTTOM = new Vertex(0.0F, 0.0F, 0.0F, 0.5F, 1.0F);

    private static final Vertex NORTH = new Vertex(0.0F, 0.46F, -0.5F, 0.0F, 0.5F);
    private static final Vertex EAST = new Vertex(0.5F, 0.46F, 0.0F, 1.0F, 0.5F);
    private static final Vertex SOUTH = new Vertex(0.0F, 0.46F, 0.5F, 0.0F, 0.5F);
    private static final Vertex WEST = new Vertex(-0.5F, 0.46F, 0.0F, 1.0F, 0.5F);

    private CrystalHeartGeometry() {
    }

    public static void render(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay
    ) {
        // Upper pyramid.
        renderTriangleAsQuad(pose, consumer, TOP, EAST, NORTH, packedLight, packedOverlay);
        renderTriangleAsQuad(pose, consumer, TOP, SOUTH, EAST, packedLight, packedOverlay);
        renderTriangleAsQuad(pose, consumer, TOP, WEST, SOUTH, packedLight, packedOverlay);
        renderTriangleAsQuad(pose, consumer, TOP, NORTH, WEST, packedLight, packedOverlay);

        // Lower pyramid.
        renderTriangleAsQuad(pose, consumer, BOTTOM, NORTH, EAST, packedLight, packedOverlay);
        renderTriangleAsQuad(pose, consumer, BOTTOM, EAST, SOUTH, packedLight, packedOverlay);
        renderTriangleAsQuad(pose, consumer, BOTTOM, SOUTH, WEST, packedLight, packedOverlay);
        renderTriangleAsQuad(pose, consumer, BOTTOM, WEST, NORTH, packedLight, packedOverlay);
    }

    /**
     * Minecraft's entity-style RenderTypes use quad mode. A triangular crystal
     * face is therefore emitted as a degenerate quad by repeating the third
     * corner. Visually this is still one triangle.
     */
    private static void renderTriangleAsQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            Vertex a,
            Vertex b,
            Vertex c,
            int packedLight,
            int packedOverlay
    ) {
        Vector3f ab = new Vector3f(b.x - a.x, b.y - a.y, b.z - a.z);
        Vector3f ac = new Vector3f(c.x - a.x, c.y - a.y, c.z - a.z);
        Vector3f normal = ab.cross(ac).normalize();

        emitVertex(pose, consumer, a, normal, packedLight, packedOverlay);
        emitVertex(pose, consumer, b, normal, packedLight, packedOverlay);
        emitVertex(pose, consumer, c, normal, packedLight, packedOverlay);
        emitVertex(pose, consumer, c, normal, packedLight, packedOverlay);
    }

    private static void emitVertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            Vertex vertex,
            Vector3f normal,
            int packedLight,
            int packedOverlay
    ) {
        consumer.addVertex(pose, vertex.x, vertex.y, vertex.z)
                .setColor(255, 255, 255, 255)
                .setUv(vertex.u, vertex.v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, normal.x(), normal.y(), normal.z());
    }

    private record Vertex(float x, float y, float z, float u, float v) {
    }
}
