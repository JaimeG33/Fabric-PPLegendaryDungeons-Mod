package porker.pp_legendarydungeons.features;

import porker.pp_legendarydungeons.features.wtraders.WanderingTraderFeature;

/**
 * Routes broad pp_feature markers to specific feature systems.
 *
 * For now, this only checks wandering trader markers.
 * Later this can also route crystal heart loops, altar particles, portal effects, etc.
 */
public final class FeatureService {
    private FeatureService() {
    }

    public static void checkFeatures(FeatureContext context) {
        WanderingTraderFeature.check(context);
    }
}
