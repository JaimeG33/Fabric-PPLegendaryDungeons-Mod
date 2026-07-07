package porker.pp_legendarydungeons.summon.aftermath;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ArmorStand;
import porker.pp_legendarydungeons.setup.ModScoreboards;
import porker.pp_legendarydungeons.summon.LegendarySummonDefinition;
import porker.pp_legendarydungeons.summon.SummonContext;

/**
 * Rayquaza-specific aftermath.
 *
 * This only runs after normal Rayquaza successfully spawns.
 *
 * Current behavior:
 * - set event_triggered = 1 on pp_rayquaza_conditions
 * - set spawn_once = 1 on pp_summon_rayquaza, or fallback marker
 * - reset pp_timer = 0 on pp_rayquaza_conditions
 * - add pp_rayquaza_summoned tags as an extra backup
 * - make Rayquaza persistent
 * - give Rayquaza Strength II and Resistance II with no particles
 * - play sounds at the spawn marker
 * - also play sounds at the condition marker if it is separate
 * - send a message to nearby players
 *
 * Important:
 * This file does NOT remove the emerald block anymore.
 * The normal summon now happens after the player already removed the emerald block
 * and the condition stand stayed empty long enough.
 */
public final class RayquazaAftermath implements LegendarySummonAftermath {
    private static final double MESSAGE_RADIUS = 64.0D;
    private static final double MESSAGE_RADIUS_SQUARED = MESSAGE_RADIUS * MESSAGE_RADIUS;

    @Override
    public void run(
            SummonContext context,
            LegendarySummonDefinition definition,
            PokemonEntity spawnedPokemon
    ) {
        ArmorStand conditionMarker = context.conditionMarker();

        /*
         * If a separate pp_summon_rayquaza marker exists, use that for spawn_once.
         * If there is no separate spawn marker, fall back to the condition marker.
         */
        ArmorStand spawnScoreMarker = context.spawnMarker() != null
                ? context.spawnMarker()
                : conditionMarker;

        if (conditionMarker != null) {
            /*
             * Mark the Rayquaza condition stand as triggered.
             *
             * Java equivalent of:
             * scoreboard players set <pp_rayquaza_conditions> event_triggered 1
             */
            ModScoreboards.setEntityScore(
                    conditionMarker,
                    ModScoreboards.EVENT_TRIGGERED,
                    1
            );

            /*
             * Reset the empty-hand timer after the summon succeeds.
             *
             * This keeps the condition marker clean after normal Rayquaza spawns.
             */
            ModScoreboards.setEntityScore(
                    conditionMarker,
                    ModScoreboards.PP_TIMER,
                    0
            );

            /*
             * Extra backup prevention.
             *
             * The scoreboard is the main state check, but this tag gives another
             * layer of protection against duplicate normal summons.
             */
            conditionMarker.addTag(definition.usedTag());
        }

        if (spawnScoreMarker != null) {
            /*
             * Mark the spawn marker as used.
             *
             * Java equivalent of:
             * scoreboard players set <pp_summon_rayquaza> spawn_once 1
             */
            ModScoreboards.setEntityScore(
                    spawnScoreMarker,
                    ModScoreboards.SPAWN_ONCE,
                    1
            );

            spawnScoreMarker.addTag(definition.usedTag());
        }

        if (context.summonMarker() != null) {
            context.summonMarker().addTag(definition.usedTag());
        }

        setupSpawnedRayquaza(spawnedPokemon);
        playRayquazaSounds(context);
        messageNearbyPlayers(context);
    }

    /**
     * Applies special behavior to the spawned normal Rayquaza.
     */
    private void setupSpawnedRayquaza(PokemonEntity spawnedPokemon) {
        spawnedPokemon.addTag("normal_rayquaza_pokemon");
        spawnedPokemon.addTag("rayquaza_pokemon");

        /*
         * Prevent normal despawn behavior.
         */
        spawnedPokemon.setPersistenceRequired();

        applyRayquazaCombatEffects(spawnedPokemon);
    }

    /**
     * Gives Rayquaza permanent Strength II and Resistance II.
     *
     * Amplifier 1 = level II.
     * The two false values hide potion particles and the visible effect display.
     */
    private void applyRayquazaCombatEffects(PokemonEntity spawnedPokemon) {
        spawnedPokemon.addEffect(
                new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        Integer.MAX_VALUE,
                        1,
                        false,
                        false
                )
        );

        spawnedPokemon.addEffect(
                new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        Integer.MAX_VALUE,
                        1,
                        false,
                        false
                )
        );
    }

    /**
     * Plays the Rayquaza summon sounds at both:
     * - the actual Rayquaza spawn location
     * - the condition armor stand location, if it is separate
     *
     * This lets players near the interaction/condition stand hear the summon,
     * while still letting players near the actual spawn point hear it too.
     */
    private void playRayquazaSounds(SummonContext context) {
        BlockPos spawnPos = context.spawnPos();
        playRayquazaSoundsAt(context, spawnPos);

        ArmorStand conditionMarker = context.conditionMarker();

        if (conditionMarker == null) {
            return;
        }

        BlockPos conditionPos = conditionMarker.blockPosition();

        /*
         * Avoid double-playing the same sounds if the condition marker and spawn
         * marker are the same block/location.
         */
        if (!conditionPos.equals(spawnPos)) {
            playRayquazaSoundsAt(context, conditionPos);
        }
    }

    private void playRayquazaSoundsAt(SummonContext context, BlockPos pos) {
        context.level().playSound(
                null,
                pos,
                SoundEvents.END_PORTAL_SPAWN,
                SoundSource.HOSTILE,
                1.5F,
                1.0F
        );

        context.level().playSound(
                null,
                pos,
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.WEATHER,
                2.0F,
                0.85F
        );

        context.level().playSound(
                null,
                pos,
                SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE,
                1.25F,
                1.15F
        );
    }

    private void messageNearbyPlayers(SummonContext context) {
        BlockPos pos = context.spawnPos();

        Component message = Component
                .literal("Rayquaza has descended from the sky!")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);

        for (ServerPlayer player : context.level().players()) {
            double distanceSquared = player.distanceToSqr(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D
            );

            if (distanceSquared <= MESSAGE_RADIUS_SQUARED) {
                player.sendSystemMessage(message);
            }
        }
    }
}