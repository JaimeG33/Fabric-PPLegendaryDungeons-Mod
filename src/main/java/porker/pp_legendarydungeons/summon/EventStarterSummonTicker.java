package porker.pp_legendarydungeons.summon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

public final class EventStarterSummonTicker {
    private static final String EVENT_STARTER_TAG = "event_starter";
    private static final double TRIGGER_DISTANCE = 10.0D;
    private static final double TRIGGER_DISTANCE_SQUARED = TRIGGER_DISTANCE * TRIGGER_DISTANCE;

    private static int ticks = 0;

    private EventStarterSummonTicker() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ticks++;

            // Check once per second instead of every tick.
            if (ticks % 20 != 0) {
                return;
            }

            for (ServerLevel level : server.getAllLevels()) {
                checkLevel(level);
            }
        });
    }

    private static void checkLevel(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            AABB searchBox = player.getBoundingBox().inflate(TRIGGER_DISTANCE);

            List<ArmorStand> eventStarters = level.getEntitiesOfClass(
                    ArmorStand.class,
                    searchBox,
                    armorStand ->
                            armorStand.isAlive()
                                    && armorStand.getTags().contains(EVENT_STARTER_TAG)
                                    && hasEmeraldBlockInHand(armorStand)
                                    && armorStand.distanceToSqr(player) <= TRIGGER_DISTANCE_SQUARED
            );

            for (ArmorStand armorStand : eventStarters) {
                trySummonRayquaza(level, armorStand, player);
            }
        }
    }

    private static void trySummonRayquaza(
            ServerLevel level,
            ArmorStand armorStand,
            ServerPlayer player
    ) {
        // Double-check this so two nearby players do not cause duplicate summons.
        if (!hasEmeraldBlockInHand(armorStand)) {
            return;
        }

        Optional<PokemonEntity> spawned = LegendarySummonHelper.spawnPokemon(
                level,
                "rayquaza",
                70,
                armorStand.getX(),
                armorStand.getY(),
                armorStand.getZ(),
                armorStand.getYRot(),
                0.0F
        );

        spawned.ifPresent(rayquaza ->
                SummonAftermath.onRayquazaSummoned(level, armorStand, player, rayquaza)
        );
    }

    private static boolean hasEmeraldBlockInHand(ArmorStand armorStand) {
        return armorStand.getMainHandItem().is(Items.EMERALD_BLOCK)
                || armorStand.getOffhandItem().is(Items.EMERALD_BLOCK);
    }
}