package porker.pp_legendarydungeons.items.maps_findrandom;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

/**
 * A broad random-map category, such as all dungeons or all research outposts.
 */
public record RandomMapGroup(
        String id,
        String displayName,
        List<Component> fallbackDescriptionLines,
        List<RandomMapTarget> targets
) {
    public Optional<RandomMapTarget> getTarget(String targetId) {
        return targets.stream()
                .filter(target -> target.id().equals(targetId))
                .findFirst();
    }
}
