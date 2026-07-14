package porker.pp_legendarydungeons.features;

import porker.pp_legendarydungeons.features.custom_mobs.CustomMobFeature;
import porker.pp_legendarydungeons.features.wtraders.WanderingTraderFeature;

/**
 * Routes broad pp_feature markers to specific feature systems.
 *
 * Current routed systems:
 * - WTrader markers
 * - Custom dungeon enemy markers
 *
 * Later this can also route crystal heart loops, altar particles, portal effects,
 * dungeon rule zones, vault fix markers, etc.
 */
public final class FeatureService {
    private FeatureService() {
    }

    public static void checkFeatures(FeatureContext context) {
        WanderingTraderFeature.check(context);
        CustomMobFeature.check(context);
    }
}
