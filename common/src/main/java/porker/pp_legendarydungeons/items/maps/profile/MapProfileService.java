package porker.pp_legendarydungeons.items.maps.profile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.features.wtraders.json.MapOfferJson;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonRegistry;
import porker.pp_legendarydungeons.features.wtraders.runtime.LootTableTradeStackFactory;

import java.util.Optional;

/**
 * Resolves a shared map-profile id to the existing data-driven map-offer
 * definition and executes its functional exploration-map loot table.
 *
 * <p>The first implementation intentionally reuses the map-offer registry that
 * already powers wandering traders. This keeps one public profile id and one
 * loot-table definition for every generated map, regardless of where the map
 * is obtained.</p>
 */
public final class MapProfileService {
    private MapProfileService() {
    }

    public static Optional<ItemStack> generate(
            ServerLevel level,
            Vec3 origin,
            Entity contextEntity,
            ResourceLocation profileId
    ) {
        if (level == null || origin == null || profileId == null) {
            return Optional.empty();
        }

        Optional<MapOfferJson> mapOffer =
                WTraderJsonRegistry.getMapOffer(profileId);

        if (mapOffer.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "[Map Profile] Unknown map profile id {}.",
                    profileId
            );
            return Optional.empty();
        }

        String lootTableId = mapOffer.get().lootTable;

        if (lootTableId == null || lootTableId.isBlank()) {
            LegendaryDungeons.LOGGER.warn(
                    "[Map Profile] Map profile {} has no loot table.",
                    profileId
            );
            return Optional.empty();
        }

        return LootTableTradeStackFactory.generate(
                level,
                origin,
                contextEntity,
                lootTableId
        );
    }
}
