package porker.pp_legendarydungeons.features.wtraders;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.decoration.ArmorStand;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;
import porker.pp_legendarydungeons.features.FeatureContext;

/**
 * Temporary bridge spawner for wandering trader test implementation.
 *
 * This intentionally runs a known-working summon command based on the old datapack trader NBT.
 * Once the scanner is confirmed stable, the offer construction can be migrated to fully native Java.
 */
public final class WanderingTraderCommandSpawner {
    private WanderingTraderCommandSpawner() {
    }

    public static boolean spawnSkyPillarTrader(FeatureContext context, ArmorStand traderMarker) {
        return runCommandAtMarker(context, traderMarker, skyPillarTraderCommand());
    }

    private static boolean runCommandAtMarker(FeatureContext context, ArmorStand marker, String command) {
        try {
            CommandSourceStack source = context.level()
                    .getServer()
                    .createCommandSourceStack()
                    .withLevel(context.level())
                    .withPosition(marker.position())
                    .withPermission(4)
                    .withSuppressedOutput();

            context.level().getServer().getCommands().performPrefixedCommand(source, command);
            return true;
        } catch (Exception exception) {
            ProfessorPorkersLegendaryDungeons.LOGGER.error(
                    "Failed to spawn wandering trader feature at {}",
                    marker.blockPosition(),
                    exception
            );
            return false;
        }
    }

    /**
     * Test map trader copied from the old to_skypillar datapack function.
     *
     * Notes:
     * - This still sells the placeholder/custom rare filled_map with custom_model_data 3311101.
     * - That lets you test the feature scanner and trader spawning first.
     * - Later, this trade can sell a Java-generated real exploration map directly.
     */
    private static String skyPillarTraderCommand() {
        return """
                summon minecraft:wandering_trader ~ ~ ~ {PersistenceRequired:1b,Tags:["pp_spawned_feature_trader","pp_map_trader","pp_to_skypillar"],Offers:{Recipes:[{rewardExp:1b,maxUses:3,buy:{id:"minecraft:emerald",count:3},sell:{id:"minecraft:cherry_sapling",count:5}},{rewardExp:1b,maxUses:1,buy:{id:"minecraft:emerald",count:32},buyB:{id:"cobblemon:relic_coin",count:32},sell:{id:"minecraft:filled_map",count:1,components:{"minecraft:item_name":'{"bold":true,"text":"Sky Pillar Exploration Map"}',"minecraft:lore":['"This map shows the location to the legendary dungeon of Rayquaza"'],"minecraft:rarity":"rare","minecraft:custom_model_data":3311101}}},{rewardExp:1b,maxUses:4,buy:{id:"minecraft:emerald",count:8},sell:{id:"cobblemon:flying_gem",count:1}},{rewardExp:1b,maxUses:8,buy:{id:"minecraft:apple",count:1},sell:{id:"cobblemon:leftovers",count:1}},{rewardExp:1b,maxUses:6,buy:{id:"minecraft:emerald",count:5},sell:{id:"cobblemon:green_apricorn_seed",count:1}},{rewardExp:1b,maxUses:1,buy:{id:"minecraft:emerald",count:10},buyB:{id:"cobblemon:relic_coin",count:16},sell:{id:"minecraft:iron_hoe",count:1,components:{"minecraft:enchantments":{levels:{"minecraft:fortune":3,"minecraft:unbreaking":3}}}}}]}}
                """.replace('\n', ' ').trim();
    }
}
