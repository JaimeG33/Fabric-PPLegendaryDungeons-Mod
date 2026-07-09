package porker.pp_legendarydungeons.features.wtraders.json;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.trading.MerchantOffers;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

/**
 * Temporary logging smoke test for the JSON trade generator.
 *
 * This does not spawn or modify any traders. It only proves that at least one
 * loaded custom_no_map profile can be converted into MerchantOffers.
 *
 * We intentionally skip generated-map profiles here because real generated map
 * ItemStacks require the existing map loot-table capture flow, which will be
 * connected in a later step.
 */
public final class WTraderJsonGeneratorSmokeTest {
    private WTraderJsonGeneratorSmokeTest() {
    }

    public static void logSmokeTest() {
        int testedProfiles = 0;

        for (TraderProfileJson profile : WTraderJsonRegistry.traderProfiles().values()) {
            if (profile == null) {
                continue;
            }

            if (!WTraderJsonTraderTypes.CUSTOM_NO_MAP.equals(profile.traderType)) {
                continue;
            }

            MerchantOffers offers = WTraderTradeGenerator.createOffersForProfile(
                    RandomSource.create(),
                    profile
            );

            ProfessorPorkersLegendaryDungeons.LOGGER.info(
                    "[WTrader JSON] Generator smoke test created {} offers for profile {}.",
                    offers.size(),
                    profile.id
            );

            testedProfiles++;
        }

        if (testedProfiles == 0) {
            ProfessorPorkersLegendaryDungeons.LOGGER.info(
                    "[WTrader JSON] Generator smoke test found no custom_no_map profiles to test."
            );
        }
    }
}
