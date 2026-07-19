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

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Narrow compatibility bridge for Minecraft effects whose behavior occurs from
 * {@link MobEffectInstance#onMobRemoved}.
 *
 * <p>Cobblemon owns the Pokémon faint/death lifecycle and can remove a fainted
 * Pokémon without Minecraft's ordinary {@link Entity.RemovalReason#KILLED}
 * callback. This class emulates only explicitly requested callbacks, only for
 * registered dungeon Pokémon, and only at Cobblemon's final LOOT_DROPPED stage.</p>
 *
 * <p>No Pokémon species, effect implementation, loader event, or Cobblemon AI
 * class is replaced or mixed into.</p>
 */
public final class DungeonPokemonDeathEffectBridge {
    private static final String VANILLA_KILLED = "vanilla_killed";
    private static final String PROCESSED_ENTITY_TAG =
            "pp_dungeon_death_effects_processed";

    private DungeonPokemonDeathEffectBridge() {
    }

    /**
     * Invokes each explicitly configured and currently active effect's ordinary
     * Minecraft KILLED removal callback at most once for this entity.
     */
    public static void triggerConfiguredDeathEffects(
            PokemonEntity pokemon
    ) {
        if (!(pokemon.level() instanceof ServerLevel level)
                || pokemon.isAlive()) {
            return;
        }

        Optional<DungeonPokemonRecord> recordOptional =
                DungeonPokemonManager.getRecord(pokemon.getUUID());

        if (recordOptional.isEmpty()) {
            return;
        }

        DungeonPokemonRecord record = recordOptional.get();
        Optional<DungeonPokemonProfileJson> profileOptional =
                DungeonPokemonProfileRegistry.get(record.profileId());

        if (profileOptional.isEmpty()) {
            return;
        }

        DungeonPokemonProfileJson profile = profileOptional.get();

        if (!hasConfiguredDeathCallback(profile)) {
            return;
        }

        /*
         * Entity tags are persisted by Minecraft and disappear with the entity,
         * avoiding a global UUID cache while still protecting against duplicate
         * LOOT_DROPPED delivery or a chunk save during the death sequence.
         */
        if (pokemon.getTags().contains(PROCESSED_ENTITY_TAG)) {
            LegendaryDungeons.LOGGER.debug(
                    "[Dungeon Pokemon Death Effects] Skipped duplicate callback processing for entity={} profile={}.",
                    pokemon.getUUID(),
                    record.profileId()
            );
            return;
        }

        pokemon.addTag(PROCESSED_ENTITY_TAG);

        Set<ResourceLocation> handledEffectIds = new HashSet<>();

        for (DungeonPokemonProfileJson.CombatEffect configured :
                profile.combat_effects) {
            if (configured == null
                    || !VANILLA_KILLED.equals(
                            normalized(configured.death_callback)
                    )) {
                continue;
            }

            ResourceLocation effectId;

            try {
                effectId = ResourceLocation.parse(configured.effect);
            } catch (Exception exception) {
                /*
                 * Profile validation normally prevents this. Keep finalization
                 * defensive so malformed runtime data cannot disrupt Pokémon loot.
                 */
                LegendaryDungeons.LOGGER.warn(
                        "[Dungeon Pokemon Death Effects] Invalid effect ID {} for entity={} profile={}: {}",
                        configured.effect,
                        pokemon.getUUID(),
                        record.profileId(),
                        exception.toString()
                );
                continue;
            }

            if (!handledEffectIds.add(effectId)) {
                LegendaryDungeons.LOGGER.warn(
                        "[Dungeon Pokemon Death Effects] Ignored duplicate callback configuration for effect={} entity={} profile={}.",
                        effectId,
                        pokemon.getUUID(),
                        record.profileId()
                );
                continue;
            }

            Optional<Holder.Reference<MobEffect>> holder =
                    level.registryAccess()
                            .registryOrThrow(Registries.MOB_EFFECT)
                            .getHolder(effectId);

            if (holder.isEmpty()) {
                LegendaryDungeons.LOGGER.warn(
                        "[Dungeon Pokemon Death Effects] Unknown effect={} for entity={} profile={}.",
                        effectId,
                        pokemon.getUUID(),
                        record.profileId()
                );
                continue;
            }

            MobEffectInstance active = pokemon.getEffect(holder.get());

            if (active == null) {
                LegendaryDungeons.LOGGER.debug(
                        "[Dungeon Pokemon Death Effects] Configured effect={} was not active on entity={} profile={} at finalization.",
                        effectId,
                        pokemon.getUUID(),
                        record.profileId()
                );
                continue;
            }

            try {
                active.onMobRemoved(
                        pokemon,
                        Entity.RemovalReason.KILLED
                );

                LegendaryDungeons.LOGGER.debug(
                        "[Dungeon Pokemon Death Effects] Triggered effect={} entity={} profile={}.",
                        effectId,
                        pokemon.getUUID(),
                        record.profileId()
                );
            } catch (Exception exception) {
                /*
                 * A custom effect callback must not interrupt Cobblemon drops,
                 * supplemental loot, replacement loot, or entity cleanup.
                 */
                LegendaryDungeons.LOGGER.warn(
                        "[Dungeon Pokemon Death Effects] Callback failed for effect={} entity={} profile={}: {}",
                        effectId,
                        pokemon.getUUID(),
                        record.profileId(),
                        exception.toString()
                );
            } finally {
                /*
                 * Remove the effect after its callback so a later KILLED removal
                 * supplied by Cobblemon or another compatibility mod cannot invoke
                 * the same death behavior a second time.
                 */
                pokemon.removeEffect(holder.get());
            }
        }
    }

    private static boolean hasConfiguredDeathCallback(
            DungeonPokemonProfileJson profile
    ) {
        if (profile.combat_effects == null
                || profile.combat_effects.isEmpty()) {
            return false;
        }

        for (DungeonPokemonProfileJson.CombatEffect configured :
                profile.combat_effects) {
            if (configured != null
                    && VANILLA_KILLED.equals(
                            normalized(configured.death_callback)
                    )) {
                return true;
            }
        }

        return false;
    }

    private static String normalized(String value) {
        return value == null
                ? "none"
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
