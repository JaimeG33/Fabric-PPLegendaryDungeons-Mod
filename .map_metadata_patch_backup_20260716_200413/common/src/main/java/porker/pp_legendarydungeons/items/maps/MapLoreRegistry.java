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
                            Component.literal("This map shows the location to the legendary skypillar of Rayquaza.")
                                    .withStyle(ChatFormatting.GRAY)
                    )
            ),

            "crystal_caves",
            new MapLoreEntry(
                    "crystal_caves",
                    List.of(
                            Component.literal("This map leads to a Carbink colony said to be watched over by Diancie.")
                                    .withStyle(ChatFormatting.GRAY)
                    )
            ),

            "ancient_city",
            new MapLoreEntry(
                    "ancient_city",
                    List.of(
                            Component.literal("This map leads toward an Ancient City deep beneath the surface.")
                                    .withStyle(ChatFormatting.GRAY)
                    )
            ),

            "fishing_boats",
            new MapLoreEntry(
                    "fishing_boats",
                    List.of(
                            Component.literal("This map points toward an old fishing boat structure.")
                                    .withStyle(ChatFormatting.GRAY)
                    )
            ),

            "shipwreck_coves",
            new MapLoreEntry(
                    "shipwreck_coves",
                    List.of(
                            Component.literal("This map points toward a shipwreck cove along the coast.")
                                    .withStyle(ChatFormatting.GRAY)
                    )
            ),

            "wishing_weald",
            new MapLoreEntry(
                    "wishing_weald",
                    List.of(
                            Component.literal("This map points toward a Wishing Weald site.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.literal("If no destination appears, the required structure may not exist in this world or modpack.")
                                    .withStyle(ChatFormatting.DARK_GRAY)
                    )
            ),

            "mega_site",
            new MapLoreEntry(
                    "mega_site",
                    List.of(
                            Component.literal("This map points toward a Mega Site.")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.literal("If no destination appears, the required structure may not exist in this world or modpack.")
                                    .withStyle(ChatFormatting.DARK_GRAY)
                    )
            ),
            "trial_chambers",
            new MapLoreEntry(
                    "trial_chambers",
                    List.of(
                            Component.literal("This map leads toward Trial Chambers deep underground.")
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