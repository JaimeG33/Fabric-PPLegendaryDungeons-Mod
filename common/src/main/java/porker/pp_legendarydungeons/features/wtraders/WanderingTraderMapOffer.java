package porker.pp_legendarydungeons.features.wtraders;

public enum WanderingTraderMapOffer {
    SKY_PILLAR(
            "sky_pillar",
            "pp_legendarydungeons:maps/find_skypillar"
    ),
    RANDOM_DUNGEON(
            "random_dungeon",
            "pp_legendarydungeons:maps/random/find_random_dungeon"
    ),

    RANDOM_MISC(
            "random_misc",
            "pp_legendarydungeons:maps/random/find_random_misc"
    ),

    RANDOM_RESEARCH_OUTPOST(
            "random_research_outpost",
            "pp_legendarydungeons:maps/random/find_random_research_outpost"
    );

    private final String id;
    private final String lootTableId;

    WanderingTraderMapOffer(String id, String lootTableId) {
        this.id = id;
        this.lootTableId = lootTableId;
    }

    public String id() {
        return id;
    }

    public String lootTableId() {
        return lootTableId;
    }
}