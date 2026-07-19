package porker.pp_legendarydungeons.features.dungeon_pokemon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Narrow compatibility bridge for Minecraft effects whose behavior occurs from
 * {@link MobEffectInstance#onMobRemoved}.
 *
 * <p>Cobblemon owns the Pokémon faint/death lifecycle. Its 1.7.3 server delegate
 * performs ordinary drops at the beginning of the death sequence by default and
 * removes the entity at death tick 60. The bridge therefore separates callback
 * arming, death observation, and final execution:</p>
 *
 * <pre>
 * active configured effect -> armed callback
 * defeated Pokémon         -> pending callback
 * death tick 59            -> native KILLED callback
 * death tick 60            -> Cobblemon removes the entity
 * </pre>
 *
 * <p>Only explicitly opted-in effects on registered dungeon Pokémon enter this
 * bridge. No Pokémon species, effect implementation, loader event, or Cobblemon
 * AI/death class is replaced or mixed into.</p>
 */
public final class DungeonPokemonDeathEffectBridge {
    private static final String VANILLA_KILLED = "vanilla_killed";
    private static final String PROCESSED_ENTITY_TAG =
            "pp_dungeon_death_effects_processed";

    /**
     * Cobblemon 1.7.3 removes the Pokémon when deathTime reaches 60. The mod's
     * manager runs during SERVER_POST, so tick 59 is the final reliable point at
     * which the original Pokémon entity still exists.
     */
    private static final int CALLBACK_DEATH_TICK = 59;

    private static final Map<UUID, ArmedCallbacks> ARMED_CALLBACKS =
            new HashMap<>();
    private static final Map<UUID, PendingCallbacks> PENDING_CALLBACKS =
            new HashMap<>();

    private DungeonPokemonDeathEffectBridge() {
    }

    /**
     * Records an opted-in callback after its matching world effect has actually
     * been accepted by the Pokémon entity. The record is independent of the
     * effect's remaining duration so a long Cobblemon battle cannot disarm it.
     */
    public static void armConfiguredEffect(
            PokemonEntity pokemon,
            ResourceLocation profileId,
            DungeonPokemonProfileJson.CombatEffect configured,
            int amplifier
    ) {
        if (!(pokemon.level() instanceof ServerLevel)
                || !pokemon.isAlive()
                || configured == null
                || !VANILLA_KILLED.equals(
                        normalized(configured.death_callback)
                )
                || pokemon.getTags().contains(PROCESSED_ENTITY_TAG)) {
            return;
        }

        Optional<DungeonPokemonRecord> recordOptional =
                DungeonPokemonManager.getRecord(pokemon.getUUID());

        if (recordOptional.isEmpty()
                || !recordOptional.get().profileId().equals(profileId)) {
            return;
        }

        ResourceLocation effectId;

        try {
            effectId = ResourceLocation.parse(configured.effect);
        } catch (Exception exception) {
            LegendaryDungeons.LOGGER.warn(
                    "[Dungeon Pokemon Death Effects] Could not arm invalid effect={} entity={} profile={}: {}",
                    configured.effect,
                    pokemon.getUUID(),
                    profileId,
                    exception.toString()
            );
            return;
        }

        ArmedCallbacks armed = ARMED_CALLBACKS.get(pokemon.getUUID());

        if (armed == null || !armed.profileId.equals(profileId)) {
            armed = new ArmedCallbacks(profileId);
            ARMED_CALLBACKS.put(pokemon.getUUID(), armed);
        }

        int safeAmplifier = Math.max(0, Math.min(255, amplifier));
        ArmedDeathEffect existing = armed.effects.get(effectId);

        if (existing == null || safeAmplifier > existing.amplifier()) {
            armed.effects.put(
                    effectId,
                    new ArmedDeathEffect(effectId, safeAmplifier)
            );

            LegendaryDungeons.LOGGER.debug(
                    "[Dungeon Pokemon Death Effects] Armed effect={} amplifier={} entity={} profile={}.",
                    effectId,
                    safeAmplifier,
                    pokemon.getUUID(),
                    profileId
            );
        }
    }

    /**
     * Queues already armed callbacks. LOOT_DROPPED calls this as a compatibility
     * fallback, while the dungeon manager also observes death directly so the
     * behavior does not depend on loot gamerules or drop timing.
     */
    public static void queueConfiguredDeathEffects(
            PokemonEntity pokemon
    ) {
        if (!(pokemon.level() instanceof ServerLevel)
                || pokemon.isAlive()
                || pokemon.getTags().contains(PROCESSED_ENTITY_TAG)) {
            return;
        }

        UUID entityId = pokemon.getUUID();

        if (!PENDING_CALLBACKS.containsKey(entityId)) {
            ArmedCallbacks armed = ARMED_CALLBACKS.remove(entityId);

            if (armed == null || armed.effects.isEmpty()) {
                return;
            }

            Optional<DungeonPokemonRecord> recordOptional =
                    DungeonPokemonManager.getRecord(entityId);

            if (recordOptional.isEmpty()
                    || !recordOptional.get().profileId().equals(armed.profileId)) {
                clear(entityId);
                return;
            }

            PendingCallbacks pending = new PendingCallbacks(
                    armed.profileId,
                    List.copyOf(armed.effects.values())
            );
            PENDING_CALLBACKS.put(entityId, pending);

            LegendaryDungeons.LOGGER.debug(
                    "[Dungeon Pokemon Death Effects] Queued {} callback(s) entity={} profile={} deathTime={}.",
                    pending.effects().size(),
                    entityId,
                    pending.profileId(),
                    pokemon.deathTime
            );
        }

        triggerPendingIfReady(pokemon);
    }

    /**
     * Called once per post-server tick for a dead managed dungeon Pokémon.
     */
    public static void observeDeathAndTick(
            PokemonEntity pokemon
    ) {
        queueConfiguredDeathEffects(pokemon);
        triggerPendingIfReady(pokemon);
    }

    /**
     * Clears stale callback state when a living entity is freshly registered or
     * restored from chunk NBT. A valid active target can arm it again normally.
     */
    public static void resetForRegistration(
            PokemonEntity pokemon
    ) {
        if (!pokemon.isAlive()) {
            return;
        }

        clear(pokemon.getUUID());
        pokemon.removeTag(PROCESSED_ENTITY_TAG);
    }

    public static void clear(UUID entityId) {
        ARMED_CALLBACKS.remove(entityId);
        PENDING_CALLBACKS.remove(entityId);
    }

    public static void clearAll() {
        ARMED_CALLBACKS.clear();
        PENDING_CALLBACKS.clear();
    }

    private static void triggerPendingIfReady(
            PokemonEntity pokemon
    ) {
        UUID entityId = pokemon.getUUID();
        PendingCallbacks pending = PENDING_CALLBACKS.get(entityId);

        if (pending == null || pokemon.deathTime < CALLBACK_DEATH_TICK) {
            return;
        }

        if (!(pokemon.level() instanceof ServerLevel level)) {
            clear(entityId);
            return;
        }

        if (pokemon.getTags().contains(PROCESSED_ENTITY_TAG)) {
            clear(entityId);
            return;
        }

        /*
         * Mark before invoking any custom callback. An effect may indirectly cause
         * another event, and duplicate delivery must remain harmless.
         */
        pokemon.addTag(PROCESSED_ENTITY_TAG);

        for (ArmedDeathEffect armedEffect : pending.effects()) {
            Optional<Holder.Reference<MobEffect>> holder;

            try {
                holder = level.registryAccess()
                        .registryOrThrow(Registries.MOB_EFFECT)
                        .getHolder(armedEffect.effectId());
            } catch (Exception exception) {
                LegendaryDungeons.LOGGER.warn(
                        "[Dungeon Pokemon Death Effects] Failed to resolve effect={} entity={} profile={}: {}",
                        armedEffect.effectId(),
                        entityId,
                        pending.profileId(),
                        exception.toString()
                );
                continue;
            }

            if (holder.isEmpty()) {
                LegendaryDungeons.LOGGER.warn(
                        "[Dungeon Pokemon Death Effects] Unknown effect={} entity={} profile={}.",
                        armedEffect.effectId(),
                        entityId,
                        pending.profileId()
                );
                continue;
            }

            try {
                /*
                 * MobEffectInstance.onMobRemoved forwards only the effect,
                 * amplifier, entity, and removal reason. Reconstructing a one-tick
                 * instance preserves native/custom effect behavior even when the
                 * original short-duration world effect expired during battle.
                 */
                MobEffectInstance callbackInstance = new MobEffectInstance(
                        holder.get(),
                        1,
                        armedEffect.amplifier(),
                        false,
                        false,
                        false
                );

                callbackInstance.onMobRemoved(
                        pokemon,
                        Entity.RemovalReason.KILLED
                );

                LegendaryDungeons.LOGGER.debug(
                        "[Dungeon Pokemon Death Effects] Triggered effect={} amplifier={} entity={} profile={} deathTime={}.",
                        armedEffect.effectId(),
                        armedEffect.amplifier(),
                        entityId,
                        pending.profileId(),
                        pokemon.deathTime
                );
            } catch (Exception exception) {
                /*
                 * A custom effect callback must not interrupt Cobblemon drops,
                 * supplemental loot, replacement loot, or entity cleanup.
                 */
                LegendaryDungeons.LOGGER.warn(
                        "[Dungeon Pokemon Death Effects] Callback failed for effect={} entity={} profile={}: {}",
                        armedEffect.effectId(),
                        entityId,
                        pending.profileId(),
                        exception.toString()
                );
            } finally {
                /*
                 * Remove any still-active copy so Cobblemon's KILLED removal on the
                 * next tick cannot invoke the same opted-in behavior a second time.
                 */
                pokemon.removeEffect(holder.get());
            }
        }

        clear(entityId);
    }

    private static String normalized(String value) {
        return value == null
                ? "none"
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private static final class ArmedCallbacks {
        private final ResourceLocation profileId;
        private final Map<ResourceLocation, ArmedDeathEffect> effects =
                new LinkedHashMap<>();

        private ArmedCallbacks(ResourceLocation profileId) {
            this.profileId = profileId;
        }
    }

    private record ArmedDeathEffect(
            ResourceLocation effectId,
            int amplifier
    ) {
    }

    private record PendingCallbacks(
            ResourceLocation profileId,
            List<ArmedDeathEffect> effects
    ) {
        private PendingCallbacks {
            effects = new ArrayList<>(effects);
        }
    }
}
