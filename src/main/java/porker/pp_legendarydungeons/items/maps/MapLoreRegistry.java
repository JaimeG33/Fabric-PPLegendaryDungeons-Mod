package porker.pp_legendarydungeons.items.maps;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MapLoreRegistry {
    private static final Map<String, MapLoreEntry> ENTRIES = Map.of(
            "skypillar",
            new MapLoreEntry(
                    "skypillar",
                    List.of(
                            Component.literal("This map shows the location to the legendary dungeon of Rayquaza.")
                                    .withStyle(ChatFormatting.GRAY)
                    )
            )
    );

    private MapLoreRegistry() {
    }

    public static Optional<MapLoreEntry> get(String targetId) {
        return Optional.ofNullable(ENTRIES.get(targetId));
    }
}
