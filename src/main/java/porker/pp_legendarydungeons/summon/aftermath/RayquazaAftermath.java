
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
import porker.pp_legendarydungeons.summon.dungeon_completion.rayquaza.RayquazaDungeonCompletion;

/**
 * Rayquaza-specific aftermath.
 *
 * Runs only after normal Rayquaza successfully spawns.
 */
public final class RayquazaAftermath implements LegendarySummonAftermath {
    private static final double MESSAGE_RADIUS = 64.0D;
    private static final double MESSAGE_RADIUS_SQUARED =
            MESSAGE_RADIUS * MESSAGE_RADIUS;

    @Override
    public void run(
            SummonContext context,
            LegendarySummonDefinition definition,
            PokemonEntity spawnedPokemon
    ) {
        ArmorStand conditionMarker = context.conditionMarker();

        ArmorStand spawnScoreMarker = context.spawnMarker() != null
                ? context.spawnMarker()
                : conditionMarker;

        if (conditionMarker != null) {
            ModScoreboards.setEntityScore(
                    conditionMarker,
                    ModScoreboards.EVENT_TRIGGERED,
                    1
            );

            ModScoreboards.setEntityScore(
                    conditionMarker,
                    ModScoreboards.PP_TIMER,
                    0
            );

            conditionMarker.addTag(definition.usedTag());
        }

        if (spawnScoreMarker != null) {
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

        /*
         * This arms a UUID-based, persistent completion watch only after the
         * Rayquaza entity has successfully spawned and been configured.
         */
        RayquazaDungeonCompletion.arm(context, spawnedPokemon);

        playRayquazaSounds(context);
        messageNearbyPlayers(context);
    }

    private void setupSpawnedRayquaza(PokemonEntity spawnedPokemon) {
        spawnedPokemon.addTag("normal_rayquaza_pokemon");
        spawnedPokemon.addTag("rayquaza_pokemon");
        spawnedPokemon.setPersistenceRequired();
        applyRayquazaCombatEffects(spawnedPokemon);
    }

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

    private void playRayquazaSounds(SummonContext context) {
        BlockPos spawnPos = context.spawnPos();
        playRayquazaSoundsAt(context, spawnPos);

        ArmorStand conditionMarker = context.conditionMarker();

        if (conditionMarker == null) {
            return;
        }

        BlockPos conditionPos = conditionMarker.blockPosition();

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
