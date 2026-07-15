package porker.pp_legendarydungeons.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.level.block.Rotation;

import java.util.Arrays;

/**
 * Rotation mode used by {@link FixedJigsawStructure}.
 *
 * <p>The four fixed values map directly to Minecraft's structure-template
 * rotations. RANDOM is provided so a structure can use this custom type while
 * retaining vanilla's normal random root rotation.</p>
 */
public enum FixedJigsawRotation {
    NONE("none", Rotation.NONE),
    CLOCKWISE_90("clockwise_90", Rotation.CLOCKWISE_90),
    CLOCKWISE_180("clockwise_180", Rotation.CLOCKWISE_180),
    COUNTERCLOCKWISE_90("counterclockwise_90", Rotation.COUNTERCLOCKWISE_90),
    RANDOM("random", null);

    public static final Codec<FixedJigsawRotation> CODEC = Codec.STRING.comapFlatMap(
            name -> Arrays.stream(values())
                    .filter(value -> value.serializedName.equals(name))
                    .findFirst()
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(
                            () -> "Unknown fixed jigsaw rotation: " + name
                    )),
            FixedJigsawRotation::serializedName
    );

    private final String serializedName;
    private final Rotation fixedRotation;

    FixedJigsawRotation(String serializedName, Rotation fixedRotation) {
        this.serializedName = serializedName;
        this.fixedRotation = fixedRotation;
    }

    public String serializedName() {
        return serializedName;
    }

    public boolean isRandom() {
        return fixedRotation == null;
    }

    public Rotation fixedRotation() {
        if (fixedRotation == null) {
            throw new IllegalStateException("RANDOM does not have a fixed rotation");
        }

        return fixedRotation;
    }
}
