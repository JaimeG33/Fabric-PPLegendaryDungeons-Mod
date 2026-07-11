package porker.pp_legendarydungeons.features.wtraders.natural;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffers;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;
import porker.pp_legendarydungeons.features.wtraders.json.TraderProfileJson;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonProfileSelector;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonTraderTypes;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonValues;
import porker.pp_legendarydungeons.features.wtraders.runtime.WTraderProfileOfferFactory;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.level.storage.ServerLevelData;

/**
 * Applies the vanilla_wtrader_spawns selection table to naturally spawned
 * wandering traders after vanilla has generated its normal offers.
 *
 * <p>Vanilla results are left completely unchanged. A custom result is generated
 * in full before the normal offers are cleared, so any JSON/map-generation
 * failure safely falls back to the original vanilla trader.</p>
 */
public final class NaturalWanderingTraderService {
    private static final ResourceLocation NATURAL_SPAWN_SELECTION_TABLE =
            ResourceLocation.fromNamespaceAndPath(
                    ProfessorPorkersLegendaryDungeons.MOD_ID,
                    "vanilla_wtrader_spawns"
            );

    private static final String ROLL_COMPLETE_TAG =
            "pp_natural_wtrader_roll_complete";

    private static final String CUSTOM_TRADER_TAG =
            "pp_natural_custom_trader";

    /*
     * Production default: false.
     *
     * Temporarily change this to true if you want `/summon wandering_trader`
     * entities to use the selection table during development. Change it back to
     * false before a release so only the trader tracked by vanilla's natural
     * wandering-trader spawner is affected.
     */
    private static final boolean ALLOW_NON_NATURAL_TRADERS_FOR_TESTING = false;

    private NaturalWanderingTraderService() {
    }

    public static void applyAfterVanillaOffers(WanderingTrader trader) {
        if (!(trader.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!isCurrentNaturalTrader(serverLevel, trader)
                && !ALLOW_NON_NATURAL_TRADERS_FOR_TESTING) {
            return;
        }

        /*
         * updateTrades normally runs only once, but this persistent tag prevents
         * another mod or an unusual offer reset from rolling the trader again.
         */
        if (trader.getTags().contains(ROLL_COMPLETE_TAG)) {
            return;
        }

        trader.addTag(ROLL_COMPLETE_TAG);

        Optional<String> rolledResult =
                WTraderJsonProfileSelector.pickTopLevelResult(
                        serverLevel.getRandom(),
                        NATURAL_SPAWN_SELECTION_TABLE
                );

        if (rolledResult.isEmpty()) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "[Natural WTrader] Selection table {} was missing or had no valid result. Keeping vanilla offers.",
                    NATURAL_SPAWN_SELECTION_TABLE
            );
            return;
        }

        if (WTraderJsonTraderTypes.VANILLA_WANDERING_TRADER.equals(
                rolledResult.get()
        )) {
            ProfessorPorkersLegendaryDungeons.LOGGER.debug(
                    "[Natural WTrader] {} rolled the unaltered vanilla result.",
                    trader.getUUID()
            );
            return;
        }

        Optional<TraderProfileJson> selectedProfile;

        if (WTraderJsonTraderTypes.CUSTOM_NO_MAP.equals(rolledResult.get())) {
            selectedProfile = WTraderJsonProfileSelector.pickNoMapProfile(
                    serverLevel.getRandom(),
                    NATURAL_SPAWN_SELECTION_TABLE
            );
        } else if (WTraderJsonTraderTypes.CUSTOM_MAP.equals(
                rolledResult.get()
        )) {
            selectedProfile = WTraderJsonProfileSelector.pickMapProfile(
                    serverLevel.getRandom(),
                    NATURAL_SPAWN_SELECTION_TABLE
            );
        } else {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "[Natural WTrader] Selection table {} returned unknown result {}. Keeping vanilla offers.",
                    NATURAL_SPAWN_SELECTION_TABLE,
                    rolledResult.get()
            );
            return;
        }

        if (selectedProfile.isEmpty()) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "[Natural WTrader] Result {} had no valid profile in table {}. Keeping vanilla offers.",
                    rolledResult.get(),
                    NATURAL_SPAWN_SELECTION_TABLE
            );
            return;
        }

        Optional<MerchantOffers> generatedOffers =
                WTraderProfileOfferFactory.createOffers(
                        serverLevel,
                        trader,
                        selectedProfile.get()
                );

        if (generatedOffers.isEmpty()) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "[Natural WTrader] Profile {} failed to generate. Keeping vanilla offers.",
                    selectedProfile.get().id
            );
            return;
        }

        /*
         * The complete replacement list already exists at this point. Only now
         * are vanilla's offers replaced.
         */
        MerchantOffers liveOffers = trader.getOffers();
        liveOffers.clear();
        liveOffers.addAll(generatedOffers.get());

        applyProfileName(trader, selectedProfile.get());

        /*
         * Reuse the same descriptive tags as structure-spawned JSON traders,
         * while intentionally omitting pp_spawned_feature_trader.
         */
        trader.addTag(CUSTOM_TRADER_TAG);
        trader.addTag("pp_custom_trader");
        trader.addTag("pp_json_trader");
        trader.addTag(
                "pp_wtrader_json_" + sanitizeTagFragment(
                        selectedProfile.get().id
                )
        );

        if (WTraderJsonTraderTypes.CUSTOM_MAP.equals(
                selectedProfile.get().traderType
        )) {
            trader.addTag("pp_map_trader");
        } else {
            trader.addTag("pp_no_map_trader");
        }

        ProfessorPorkersLegendaryDungeons.LOGGER.info(
                "[Natural WTrader] Replaced natural trader {} with profile {} containing {} offers.",
                trader.getUUID(),
                selectedProfile.get().id,
                liveOffers.size()
        );
    }

    /**
     * Checks whether this trader is the entity currently tracked by vanilla's
     * natural wandering-trader spawner.
     *
     * <p>ServerLevel#getLevelData() is declared as the general LevelData type,
     * even though the server-side implementation also implements ServerLevelData.
     * The explicit type check is therefore required before accessing the stored
     * wandering-trader UUID.</p>
     */
    private static boolean isCurrentNaturalTrader(
            ServerLevel level,
            WanderingTrader trader
    ) {
        if (!(level.getLevelData() instanceof ServerLevelData serverLevelData)) {
            ProfessorPorkersLegendaryDungeons.LOGGER.warn(
                    "[Natural WTrader] Server level data did not implement ServerLevelData; "
                            + "natural trader detection was skipped."
            );
            return false;
        }

        UUID naturalTraderId = serverLevelData.getWanderingTraderId();

        return naturalTraderId != null
                && naturalTraderId.equals(trader.getUUID());
    }

    private static String sanitizeTagFragment(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value
                .toLowerCase()
                .replaceAll("[^a-z0-9_]+", "_");
    }

    private static void applyProfileName(
            WanderingTrader trader,
            TraderProfileJson profile
    ) {
        boolean showName = profile.showDisplayNameOrDefault(false);

        if (showName) {
            String displayName = WTraderJsonValues.stringOr(
                    profile.displayName,
                    profile.id
            );
            trader.setCustomName(Component.literal(displayName));
            trader.setCustomNameVisible(true);
        } else {
            trader.setCustomName(null);
            trader.setCustomNameVisible(false);
        }
    }
}
