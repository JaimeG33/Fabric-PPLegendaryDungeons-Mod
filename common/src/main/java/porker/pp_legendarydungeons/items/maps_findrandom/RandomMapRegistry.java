package porker.pp_legendarydungeons.items.maps_findrandom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central registry for random map groups and their possible specific destinations.
 *
 * To add a new random map category later:
 * 1. Add a new RandomMapGroup here.
 * 2. Create a matching loot table with pp_random_map_group set to that group id.
 * 3. Make sure each RandomMapTarget points to a valid structure tag.
 */
public final class RandomMapRegistry {
    private static final Map<String, RandomMapGroup> GROUPS = Map.of(
            "dungeons",
            new RandomMapGroup(
                    "dungeons",
                    "Random Legendary Dungeon",
                    List.of(
                            Component.literal("This map points toward one of the legendary dungeon structures.")
                                    .withStyle(ChatFormatting.GRAY)
                    ),
                    List.of(
                            new RandomMapTarget(
                                    "skypillar",
                                    "Sky Pillar",
                                    "pp_legendarydungeons:skypillar_tag",
                                    List.of(
                                            Component.literal("A towering pillar in the sky connected to Rayquaza.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            )
                    )
            ),

            "misc",
            new RandomMapGroup(
                    "misc",
                    "Random Strange Structure",
                    List.of(
                            Component.literal("This map points toward one of the smaller strange structures.")
                                    .withStyle(ChatFormatting.GRAY)
                    ),
                    List.of(
                            new RandomMapTarget(
                                    "overtaken_outpost_1",
                                    "Overtaken Outpost",
                                    "pp_legendarydungeons:random/specific/overtaken_outpost_1",
                                    List.of(
                                            Component.literal("A known hideout of a local gang of troublemaking squirtles.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            ),
                            new RandomMapTarget(
                                    "ancient_battle_gvk_island_1",
                                    "Ancient Battle Island",
                                    "pp_legendarydungeons:random/specific/ancient_battle_gvk_island_1",
                                    List.of(
                                            Component.literal("A remote island tied to an ancient clash between powerful forces of both land and sea.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            )
                    )
            ),

            "research_outposts",
            new RandomMapGroup(
                    "research_outposts",
                    "Random Abandoned Research Outpost",
                    List.of(
                            Component.literal("This map points toward one of Professor Sans' abandoned research outposts.")
                                    .withStyle(ChatFormatting.GRAY)
                    ),
                    List.of(
                            new RandomMapTarget(
                                    "research_001_torterra_variants",
                                    "Torterra Variants Research Outpost",
                                    "pp_legendarydungeons:fix_multi/001_torterra_variants",
                                    List.of(
                                            Component.literal("An outpost studying unusual Torterra variants.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            ),
                            new RandomMapTarget(
                                    "research_002_ap_and_tumblestones",
                                    "Apricorn and Tumblestone Research Outpost",
                                    "pp_legendarydungeons:random/specific/research_002_ap_and_tumblestones",
                                    List.of(
                                            Component.literal("An outpost studying ancient Poke Balls and tumblestones.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            ),
                            new RandomMapTarget(
                                    "research_003_medicinal_brews",
                                    "Medicinal Brews Research Outpost",
                                    "pp_legendarydungeons:random/specific/research_003_medicinal_brews",
                                    List.of(
                                            Component.literal("An outpost studying unusual brews and medicinal supplies.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            ),
                            new RandomMapTarget(
                                    "research_004_magikarpjump",
                                    "Magikarp Jump Research Outpost",
                                    "pp_legendarydungeons:fix_multi/004_magicarpjump",
                                    List.of(
                                            Component.literal("An outpost connected to aquatic research and rumors of Kyogre ruins.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            )
                    )
            )
    );

    private RandomMapRegistry() {
    }

    public static Optional<RandomMapGroup> getGroup(String groupId) {
        return Optional.ofNullable(GROUPS.get(groupId));
    }
}
