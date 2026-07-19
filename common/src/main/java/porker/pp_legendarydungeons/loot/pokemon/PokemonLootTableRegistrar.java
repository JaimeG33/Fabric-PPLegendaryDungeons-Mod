package porker.pp_legendarydungeons.loot.pokemon;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.features.dungeon_pokemon.DungeonPokemonDeathEffectBridge;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.Unit;

/**
 * Bridges Cobblemon's Pokémon DropTable events to ordinary Minecraft loot tables.
 *
 * <p>Only LOOT_DROPPED is allowed to execute an injected loot table. The
 * BATTLE_FAINTED listener records temporary context so it can recover the
 * winning player if Cobblemon's later LOOT_DROPPED event does not contain one.</p>
 *
 * <p>This means battle defeats do not receive duplicate rewards:</p>
 *
 * <pre>
 * BATTLE_FAINTED -> cache context only
 * LOOT_DROPPED   -> optionally clear native drops, then execute one table
 * </pre>
 */
public final class PokemonLootTableRegistrar {
    /*
     * Master switch for the Cobblemon-to-Minecraft loot-table bridge.
     * Keep enabled in release builds.
     */
    private static final boolean EXECUTE_LOOT_TABLES = true;

    /*
     * Battle faint data only needs to survive until the Pokémon entity performs
     * its normal death-drop routine.
     */
    private static final long BATTLE_CONTEXT_LIFETIME_MILLIS = 30_000L;

    private static final Map<UUID, BattleFaintContext> RECENT_BATTLE_FAINTS =
            new ConcurrentHashMap<>();

    private static boolean registered = false;

    private PokemonLootTableRegistrar() {
    }

    /**
     * Registers the Cobblemon event listeners once.
     */
    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        CobblemonEvents.BATTLE_FAINTED.subscribe(
                Priority.NORMAL,
                event -> {
                    onBattleFainted(event);
                    return Unit.INSTANCE;
                }
        );

        CobblemonEvents.LOOT_DROPPED.subscribe(
                Priority.LOWEST,
                event -> {
                    onLootDropped(event);
                    return Unit.INSTANCE;
                }
        );

        LegendaryDungeons.LOGGER.info(
                "[Pokemon Loot] Registered Cobblemon battle-faint and loot-drop listeners. Execution enabled: {}",
                EXECUTE_LOOT_TABLES
        );
    }

    /**
     * Clears temporary battle information when the server stops.
     */
    public static void clear() {
        RECENT_BATTLE_FAINTS.clear();
    }

    /**
     * Records the player participating in a battle where a configured wild
     * Pokémon fainted.
     *
     * <p>This does not generate loot.</p>
     */
    private static void onBattleFainted(BattleFaintedEvent event) {
        cleanupExpiredContexts();

        Pokemon faintedPokemon = event.getKilled().getEffectedPokemon();

        Optional<DungeonPokemonLootResolver.Selection> configuredLoot =
                DungeonPokemonLootResolver.resolve(faintedPokemon);

        if (configuredLoot.isEmpty()) {
            return;
        }

        ServerPlayer battlePlayer = event
                .getBattle()
                .getPlayers()
                .stream()
                .findFirst()
                .orElse(null);

        RECENT_BATTLE_FAINTS.put(
                faintedPokemon.getUuid(),
                new BattleFaintContext(
                        battlePlayer == null
                                ? null
                                : battlePlayer.getUUID(),
                        System.currentTimeMillis()
                )
        );

        DungeonPokemonLootResolver.Selection selection =
                configuredLoot.get();

        LegendaryDungeons.LOGGER.debug(
                "[Pokemon Loot] BATTLE_FAINTED species={} pokemonUuid={} wild={} player={} mode={} table={} source={}",
                faintedPokemon.getSpecies().getResourceIdentifier(),
                faintedPokemon.getUuid(),
                faintedPokemon.isWild(),
                playerName(battlePlayer),
                selection.mode(),
                selection.table().location(),
                selection.source()
        );
    }

    /**
     * Handles the point where Cobblemon is about to perform a Pokémon's native
     * species drops.
     *
     * <p>Additional mode preserves Cobblemon's chosen native drops. Replace
     * mode clears that mutable list immediately before running the configured
     * Minecraft loot table.</p>
     */
    private static void onLootDropped(LootDroppedEvent event) {
        cleanupExpiredContexts();

        if (!(event.getEntity() instanceof PokemonEntity pokemonEntity)) {
            LegendaryDungeons.LOGGER.debug(
                    "[Pokemon Loot] LOOT_DROPPED without PokemonEntity. entityPresent={} player={} selectedNativeDrops={}",
                    event.getEntity() != null,
                    playerName(event.getPlayer()),
                    event.getDrops().size()
            );

            return;
        }

        Pokemon pokemon = pokemonEntity.getPokemon();

        /*
         * Queue explicitly armed death callbacks before the custom-loot lookup.
         * The dungeon manager executes them near the end of Cobblemon's death
         * animation. If dropAfterDeathAnimation is enabled, this event itself
         * occurs at the final death tick and the bridge executes immediately.
         */
        DungeonPokemonDeathEffectBridge.queueConfiguredDeathEffects(
                pokemonEntity
        );

        Optional<DungeonPokemonLootResolver.Selection> configuredLoot =
                DungeonPokemonLootResolver.resolve(pokemon);

        if (configuredLoot.isEmpty()) {
            return;
        }

        BattleFaintContext battleContext = RECENT_BATTLE_FAINTS.remove(
                pokemon.getUuid()
        );

        ServerPlayer eventPlayer = event.getPlayer();
        ServerPlayer cachedBattlePlayer = resolveCachedPlayer(
                pokemonEntity,
                battleContext
        );
        ServerPlayer vanillaKillCredit = resolveVanillaKillCredit(
                pokemonEntity
        );

        ServerPlayer resolvedPlayer = firstNonNull(
                eventPlayer,
                cachedBattlePlayer,
                vanillaKillCredit
        );

        long battleContextAge = battleContext == null
                ? -1L
                : System.currentTimeMillis()
                - battleContext.createdAtMillis();

        if (!EXECUTE_LOOT_TABLES) {
            return;
        }

        if (!(pokemonEntity.level()
                instanceof ServerLevel serverLevel)) {
            LegendaryDungeons.LOGGER.warn(
                    "[Pokemon Loot] Ignored loot execution outside a ServerLevel for species {}.",
                    pokemon.getSpecies().getResourceIdentifier()
            );

            return;
        }

        DungeonPokemonLootResolver.Selection selection =
                configuredLoot.get();
        int selectedNativeDropsBefore = event.getDrops().size();

        if (selection.mode()
                == DungeonPokemonLootResolver.Mode.REPLACE) {
            event.getDrops().clear();
        }

        LegendaryDungeons.LOGGER.debug(
                "[Pokemon Loot] LOOT_DROPPED species={} pokemonUuid={} entityPresent=true wild={} eventPlayer={} cachedBattlePlayer={} killCredit={} resolvedPlayer={} battleContextAgeMs={} nativeDropsBefore={} nativeDropsAfter={} mode={} table={} source={} executionEnabled={}",
                pokemon.getSpecies().getResourceIdentifier(),
                pokemon.getUuid(),
                pokemon.isWild(),
                playerName(eventPlayer),
                playerName(cachedBattlePlayer),
                playerName(vanillaKillCredit),
                playerName(resolvedPlayer),
                battleContextAge,
                selectedNativeDropsBefore,
                event.getDrops().size(),
                selection.mode(),
                selection.table().location(),
                selection.source(),
                EXECUTE_LOOT_TABLES
        );

        PokemonLootTableRunner.run(
                serverLevel,
                pokemonEntity,
                resolvedPlayer,
                selection.table()
        );
    }

    private static ServerPlayer resolveCachedPlayer(
            PokemonEntity pokemonEntity,
            BattleFaintContext context
    ) {
        if (context == null || context.playerId() == null) {
            return null;
        }

        if (!(pokemonEntity.level()
                instanceof ServerLevel serverLevel)) {
            return null;
        }

        return serverLevel
                .getServer()
                .getPlayerList()
                .getPlayer(context.playerId());
    }

    /**
     * Vanilla LivingEntity kill credit can recover some projectile or indirect
     * player kills when Cobblemon's event player is null.
     */
    private static ServerPlayer resolveVanillaKillCredit(
            PokemonEntity pokemonEntity
    ) {
        LivingEntity killCredit = pokemonEntity.getKillCredit();

        if (killCredit instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        return null;
    }

    private static ServerPlayer firstNonNull(
            ServerPlayer first,
            ServerPlayer second,
            ServerPlayer third
    ) {
        if (first != null) {
            return first;
        }

        if (second != null) {
            return second;
        }

        return third;
    }

    private static String playerName(ServerPlayer player) {
        return player == null
                ? "<none>"
                : player.getGameProfile().getName();
    }

    private static void cleanupExpiredContexts() {
        long oldestAllowedTime =
                System.currentTimeMillis()
                        - BATTLE_CONTEXT_LIFETIME_MILLIS;

        RECENT_BATTLE_FAINTS.entrySet().removeIf(
                entry ->
                        entry.getValue().createdAtMillis()
                                < oldestAllowedTime
        );
    }

    private record BattleFaintContext(
            UUID playerId,
            long createdAtMillis
    ) {
    }
}
