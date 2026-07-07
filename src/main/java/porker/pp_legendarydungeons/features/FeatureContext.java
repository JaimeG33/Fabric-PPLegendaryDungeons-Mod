package porker.pp_legendarydungeons.features;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

/**
 * Context object passed from FeatureTicker into feature handlers.
 *
 * The feature marker is the broad scanner marker, usually tagged:
 * - pp_feature
 *
 * Other nearby markers, like pp_wtraders, are discovered by the specific feature handler.
 */
public record FeatureContext(
        ServerLevel level,
        ServerPlayer player,
        ArmorStand featureMarker
) {
}
