package porker.pp_legendarydungeons.client.animated_blocks.crystal_heart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;
import porker.pp_legendarydungeons.blocks.entity.animated_blocks.crystal_heart.CrystalHeartPreset;

import java.util.EnumMap;
import java.util.Map;

/**
 * Loader-neutral normalized meshes for the Crystal Heart shape presets.
 *
 * <p>Every mesh keeps X/Z in -0.5..0.5 and Y in 0..1. The renderer then scales
 * the selected normalized mesh to the block entity's requested width and height,
 * so every preset keeps the same NBT sizing controls.</p>
 *
 * <p>The upper four faces share one full-resolution triangular texture and the
 * lower four faces share another. Changing the waist position changes the model
 * silhouette without requiring a resized texture.</p>
 */
public final class CrystalHeartGeometry {
    private static final Position TOP = new Position(0.0F, 1.0F, 0.0F);
    private static final Position BOTTOM = new Position(0.0F, 0.0F, 0.0F);

    private static final Uv TOP_APEX = new Uv(0.50F, 0.015625F);
    private static final Uv TOP_LEFT = new Uv(0.015625F, 0.984375F);
    private static final Uv TOP_RIGHT = new Uv(0.984375F, 0.984375F);

    private static final Uv BOTTOM_APEX = new Uv(0.50F, 0.984375F);
    private static final Uv BOTTOM_LEFT = new Uv(0.015625F, 0.015625F);
    private static final Uv BOTTOM_RIGHT = new Uv(0.984375F, 0.015625F);

    private static final Map<CrystalHeartPreset, Mesh> MESHES = createMeshes();

    private CrystalHeartGeometry() {
    }

    public static void renderTop(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            CrystalHeartPreset preset,
            int tintColor,
            boolean customTint,
            int packedLight,
            int packedOverlay
    ) {
        for (Face face : meshFor(preset).upperFaces()) {
            Uv left = face.mirrored ? TOP_RIGHT : TOP_LEFT;
            Uv right = face.mirrored ? TOP_LEFT : TOP_RIGHT;

            renderTriangleAsQuad(
                    pose,
                    consumer,
                    face,
                    TOP_APEX,
                    right,
                    left,
                    tintColor,
                    customTint,
                    packedLight,
                    packedOverlay
            );
        }
    }

    public static void renderBottom(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            CrystalHeartPreset preset,
            int tintColor,
            boolean customTint,
            int packedLight,
            int packedOverlay
    ) {
        for (Face face : meshFor(preset).lowerFaces()) {
            Uv left = face.mirrored ? BOTTOM_RIGHT : BOTTOM_LEFT;
            Uv right = face.mirrored ? BOTTOM_LEFT : BOTTOM_RIGHT;

            renderTriangleAsQuad(
                    pose,
                    consumer,
                    face,
                    BOTTOM_APEX,
                    left,
                    right,
                    tintColor,
                    customTint,
                    packedLight,
                    packedOverlay
            );
        }
    }

    private static Map<CrystalHeartPreset, Mesh> createMeshes() {
        EnumMap<CrystalHeartPreset, Mesh> meshes = new EnumMap<>(CrystalHeartPreset.class);

        for (CrystalHeartPreset preset : CrystalHeartPreset.values()) {
            meshes.put(preset, createMesh(preset.waistY()));
        }

        return meshes;
    }

    private static Mesh createMesh(float waistY) {
        Position north = new Position(0.0F, waistY, -0.5F);
        Position east = new Position(0.5F, waistY, 0.0F);
        Position south = new Position(0.0F, waistY, 0.5F);
        Position west = new Position(-0.5F, waistY, 0.0F);

        Face[] upperFaces = new Face[] {
                face(TOP, east, north, false, 255, 255, 255),
                face(TOP, south, east, true, 238, 255, 242),
                face(TOP, west, south, false, 208, 232, 215),
                face(TOP, north, west, true, 244, 255, 247)
        };

        Face[] lowerFaces = new Face[] {
                face(BOTTOM, north, east, false, 242, 255, 245),
                face(BOTTOM, east, south, true, 222, 246, 229),
                face(BOTTOM, south, west, false, 196, 222, 205),
                face(BOTTOM, west, north, true, 232, 251, 238)
        };

        return new Mesh(upperFaces, lowerFaces);
    }

    private static Mesh meshFor(CrystalHeartPreset preset) {
        Mesh mesh = MESHES.get(preset);
        return mesh != null ? mesh : MESHES.get(CrystalHeartPreset.BASE);
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
            int tintColor,
            boolean customTint,
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
                pose, consumer, face.a, uvA, face, normal,
                tintColor, customTint, packedLight, packedOverlay
        );
        emitVertex(
                pose, consumer, face.b, uvB, face, normal,
                tintColor, customTint, packedLight, packedOverlay
        );
        emitVertex(
                pose, consumer, face.c, uvC, face, normal,
                tintColor, customTint, packedLight, packedOverlay
        );
        emitVertex(
                pose, consumer, face.c, uvC, face, normal,
                tintColor, customTint, packedLight, packedOverlay
        );
    }

    private static void emitVertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            Position position,
            Uv uv,
            Face face,
            Vector3f normal,
            int tintColor,
            boolean customTint,
            int packedLight,
            int packedOverlay
    ) {
        int red = face.red;
        int green = face.green;
        int blue = face.blue;

        if (customTint) {
            /*
             * Convert the authored facet tint into neutral brightness, then
             * multiply the selected RGB color by that brightness. This preserves
             * facet contrast without carrying the original green bias into
             * arbitrary red/blue/purple colors.
             */
            int brightness =
                    (face.red * 54 + face.green * 183 + face.blue * 19 + 128) >> 8;
            red = scaleChannel((tintColor >> 16) & 0xFF, brightness);
            green = scaleChannel((tintColor >> 8) & 0xFF, brightness);
            blue = scaleChannel(tintColor & 0xFF, brightness);
        }

        consumer.addVertex(pose, position.x, position.y, position.z)
                .setColor(red, green, blue, 255)
                .setUv(uv.u, uv.v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, normal.x(), normal.y(), normal.z());
    }

    private static int scaleChannel(int channel, int brightness) {
        return (channel * brightness + 127) / 255;
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

    private record Mesh(Face[] upperFaces, Face[] lowerFaces) {
    }
}
