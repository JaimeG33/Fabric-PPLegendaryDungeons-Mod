package porker.pp_legendarydungeons.worldgen.structure;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Generation-scoped bridge between {@link FixedJigsawStructure} and the
 * targeted JigsawPlacement mixin.
 *
 * <p>World generation can run on worker threads, so this state is thread-local.
 * A stack is used rather than a single value so nested generation cannot leak or
 * overwrite an outer structure's requested rotation.</p>
 */
public final class FixedJigsawRotationContext {
    private static final ThreadLocal<Deque<Rotation>> ROTATIONS =
            ThreadLocal.withInitial(ArrayDeque::new);

    private FixedJigsawRotationContext() {
    }

    public static <T> T withRotation(Rotation rotation, Supplier<T> action) {
        Objects.requireNonNull(rotation, "rotation");
        Objects.requireNonNull(action, "action");

        Deque<Rotation> stack = ROTATIONS.get();
        stack.push(rotation);

        try {
            return action.get();
        } finally {
            stack.pop();

            if (stack.isEmpty()) {
                ROTATIONS.remove();
            }
        }
    }

    /**
     * Used only by the JigsawPlacement redirect. Vanilla behavior is preserved
     * whenever no fixed-jigsaw structure is currently generating.
     */
    public static Rotation chooseRootRotation(RandomSource random) {
        Deque<Rotation> stack = ROTATIONS.get();

        if (stack.isEmpty()) {
            ROTATIONS.remove();
            return Rotation.getRandom(random);
        }

        return stack.peek();
    }
}
