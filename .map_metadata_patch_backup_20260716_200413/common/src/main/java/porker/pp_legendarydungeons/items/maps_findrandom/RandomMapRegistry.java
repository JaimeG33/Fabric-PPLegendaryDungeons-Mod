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
            ),

            "outside_structures",
            new RandomMapGroup(
                    "outside_structures",
                    "Random Outside Structure",
                    List.of(
                            Component.literal("This map points toward a notable structure originating outside Professor Porker's dungeon collection.")
                                    .withStyle(ChatFormatting.GRAY)
                    ),
                    List.of(
                            new RandomMapTarget(
                                    "trial_chambers",
                                    "Trial Chambers",
                                    "pp_legendarydungeons:trial_chamber",
                                    List.of(
                                            Component.literal("A sprawling underground complex built around trial spawners and vaults.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            ),
                            new RandomMapTarget(
                                    "ancient_city",
                                    "Ancient City",
                                    "pp_legendarydungeons:ancient_city",
                                    List.of(
                                            Component.literal("An Ancient City buried deep beneath the Overworld.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            ),
                            new RandomMapTarget(
                                    "stronghold",
                                    "Stronghold",
                                    "pp_legendarydungeons:stronghold",
                                    List.of(
                                            Component.literal("A buried stronghold containing the path to the End.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            ),
                            new RandomMapTarget(
                                    "shipwreck_coves",
                                    "Shipwreck Cove",
                                    "pp_legendarydungeons:cobblemon_shipwreck_coves",
                                    List.of(
                                            Component.literal("A Cobblemon shipwreck cove found along a coastline.")
                                                    .withStyle(ChatFormatting.GRAY),
                                            Component.literal("The shared Cobblemon tag allows additional cove variants to be included automatically.")
                                                    .withStyle(ChatFormatting.DARK_GRAY)
                                    )
                            ),
                            new RandomMapTarget(
                                    "wishing_weald",
                                    "Wishing Weald",
                                    "pp_legendarydungeons:mega_showdown_wishing_weald",
                                    List.of(
                                            Component.literal("A secluded Wishing Weald site generated by Mega Showdown.")
                                                    .withStyle(ChatFormatting.GRAY)
                                    )
                            ),
                            new RandomMapTarget(
                                    "megaroid",
                                    "Mega Showdown Megaroid",
                                    "pp_legendarydungeons:mega_showdown_megaroid",
                                    List.of(
                                            Component.literal("An underground Mega Showdown megaroid.")
                                                    .withStyle(ChatFormatting.GRAY),
                                            Component.literal("The same structure ID covers megaroids associated with Keystone and blank Mega Stone finds.")
                                                    .withStyle(ChatFormatting.DARK_GRAY)
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
