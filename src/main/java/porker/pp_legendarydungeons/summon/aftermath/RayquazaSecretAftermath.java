package porker.pp_legendarydungeons.summon.aftermath;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import porker.pp_legendarydungeons.setup.ModScoreboards;
import porker.pp_legendarydungeons.summon.LegendarySummonDefinition;
import porker.pp_legendarydungeons.summon.SummonContext;

/**
 * Secret Rayquaza aftermath.
 *
 * This only runs after the secret shiny/perfect-IV Rayquaza successfully spawns.
 *
 * Current behavior:
 * - consumes the player's secret unlock by setting player scs_secrets = 0
 * - sets scs_secrets = 1 on pp_rayquaza_conditions as a secret-specific lock
 * - sets event_triggered = 1 on pp_rayquaza_conditions
 * - resets pp_timer = 0 on pp_rayquaza_conditions
 * - sets spawn_once = 1 on pp_summon_rayquaza, or fallback marker
 * - removes the nether star from pp_rayquaza_conditions
 * - adds backup used tags
 * - gives the spawned Rayquaza effects
 * - plays secret sounds
 * - sends a secret message to nearby players
 */
public final class RayquazaSecretAftermath implements LegendarySummonAftermath {
    private static final double MESSAGE_RADIUS = 500.0D;
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

        /*
         * Consume the player's secret unlock.
         *
         * Old datapack equivalent:
         * scoreboard players set <player> scs_secrets 0
         */
        if (context.player() != null) {
            ModScoreboards.setEntityScore(
                    context.player(),
                    ModScoreboards.SCS_SECRETS,
                    0
            );
        }

        if (conditionMarker != null) {
            /*
             * Secret-specific lock.
             *
             * This is separate from event_triggered so future secret logic can
             * distinguish "secret used" from other event states if needed.
             */
            ModScoreboards.setEntityScore(
                    conditionMarker,
                    ModScoreboards.SCS_SECRETS,
                    1
            );

            /*
             * Lock the main Rayquaza event so normal Rayquaza cannot also spawn
             * from the same condition marker after secret Rayquaza.
             */
            ModScoreboards.setEntityScore(
                    conditionMarker,
                    ModScoreboards.EVENT_TRIGGERED,
                    1
            );

            /*
             * Reset the empty-hand timer.
             */
            ModScoreboards.setEntityScore(
                    conditionMarker,
                    ModScoreboards.PP_TIMER,
                    0
            );

            /*
             * Remove the nether star from the condition marker now that the
             * secret summon succeeded.
             */
            removeNetherStarFromHand(conditionMarker);

            /*
             * Backup tag prevention.
             *
             * For this definition, the used tag will be:
             * pp_rayquaza_secret_summoned
             */
            conditionMarker.addTag(definition.usedTag());
        }

        if (spawnScoreMarker != null) {
            /*
             * Prevent the same spawn marker from producing another Rayquaza.
             */
            ModScoreboards.setEntityScore(
                    spawnScoreMarker,
                    ModScoreboards.SPAWN_ONCE,
                    1
            );

            /*
             * Optional secret-specific lock on the spawn marker too.
             */
            ModScoreboards.setEntityScore(
                    spawnScoreMarker,
                    ModScoreboards.SCS_SECRETS,
                    1
            );

            spawnScoreMarker.addTag(definition.usedTag());
        }

        if (context.summonMarker() != null) {
            context.summonMarker().addTag(definition.usedTag());
        }

        setupSpawnedSecretRayquaza(spawnedPokemon);
        playSecretRayquazaSounds(context);
        messageNearbyPlayers(context);
    }

    /**
     * Removes a vanilla nether star from either hand of the condition marker.
     */
    private void removeNetherStarFromHand(ArmorStand armorStand) {
        if (armorStand.getMainHandItem().is(Items.NETHER_STAR)) {
            armorStand.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return;
        }

        if (armorStand.getOffhandItem().is(Items.NETHER_STAR)) {
            armorStand.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    /**
     * Applies special behavior to the spawned secret Rayquaza.
     *
     * This mirrors your old datapack idea:
     * - tag the secret Rayquaza
     * - make it persistent
     * - give it combat effects
     */
    private void setupSpawnedSecretRayquaza(PokemonEntity spawnedPokemon) {
        spawnedPokemon.addTag("secret_rayquaza_pokemon");

        /*
         * Prevent normal despawn behavior.
         */
        spawnedPokemon.setPersistenceRequired();

        /*
         * Very long duration instead of command-side "infinite".
         *
         * Amplifier 1 = Strength II.
         * Amplifier 4 = Resistance V.
         */
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
                        4,
                        false,
                        false
                )
        );

        /*
         * Minecraft 1.21 has Wind Charged.
         * If IntelliJ complains about WIND_CHARGED, remove this effect block.
         */
        spawnedPokemon.addEffect(
                new MobEffectInstance(
                        MobEffects.WIND_CHARGED,
                        Integer.MAX_VALUE,
                        4,
                        false,
                        false
                )
        );
    }

    private void playSecretRayquazaSounds(SummonContext context) {
        BlockPos pos = context.spawnPos();

        context.level().playSound(
                null,
                pos,
                SoundEvents.END_PORTAL_SPAWN,
                SoundSource.HOSTILE,
                2.0F,
                0.8F
        );

        context.level().playSound(
                null,
                pos,
                SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE,
                2.0F,
                0.75F
        );

        context.level().playSound(
                null,
                pos,
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.WEATHER,
                2.0F,
                0.65F
        );
    }

    private void messageNearbyPlayers(SummonContext context) {
        BlockPos pos = context.spawnPos();

        Component message = Component
                .literal("SHINY Rayquaza has spawned!!!")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);

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