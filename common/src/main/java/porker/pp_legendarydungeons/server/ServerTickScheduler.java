package porker.pp_legendarydungeons.server;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;
import porker.pp_legendarydungeons.features.FeatureTicker;
import porker.pp_legendarydungeons.features.dungeon_mobs.DungeonMobManager;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonManager;
import porker.pp_legendarydungeons.items.ItemGimmickTicker;
import porker.pp_legendarydungeons.secrets.SecretTicker;
import porker.pp_legendarydungeons.summon.trigger.EntityLegendarySummonTicker;

/**
 * One shared post-server-tick listener for the mod's interval-driven systems.
 *
 * <p>Each subsystem keeps its own interval and gameplay implementation.
 * Consolidating the event boundary prevents separate loader event
 * registrations and gives the common module one scheduler to own.</p>
 */
public final class ServerTickScheduler {
    private static boolean registered = false;
    private static long tickCount = 0L;

    private ServerTickScheduler() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        TickEvent.SERVER_POST.register(ServerTickScheduler::tick);
    }

    private static void tick(MinecraftServer server) {
        tickCount++;

        /*
         * Preserve the existing subsystem order, then update only loaded
         * entities explicitly registered with the dungeon encounter managers.
         */
        EntityLegendarySummonTicker.tick(server, tickCount);
        FeatureTicker.tick(server, tickCount);
        DungeonPokemonManager.tick(server, tickCount);
        DungeonMobManager.tick(server, tickCount);
        ItemGimmickTicker.tick(server, tickCount);
        SecretTicker.tick(server, tickCount);
    }

    /**
     * Resets interval alignment between integrated or dedicated server runs in
     * the same JVM. The Architectury event itself remains registered once.
     */
    public static void reset() {
        tickCount = 0L;
    }
}
