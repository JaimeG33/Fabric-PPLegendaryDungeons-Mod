package porker.pp_legendarydungeons.summon.aftermath;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import porker.pp_legendarydungeons.summon.LegendarySummonDefinition;
import porker.pp_legendarydungeons.summon.SummonContext;

/**
 * Rayquaza-specific aftermath.
 *
 * This only runs after Rayquaza successfully spawns.
 *
 * Current behavior:
 * - remove emerald block from pp_rayquaza_conditions armor stand
 * - add pp_rayquaza_summoned tag to condition marker
 * - add pp_rayquaza_summoned tag to main pp_legendary_summon marker
 * - play several dramatic sounds
 * - send a message to nearby players
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
        // This should be the pp_rayquaza_conditions armor stand.
        ArmorStand conditionMarker = context.conditionMarker();

        if (conditionMarker != null) {
            // Remove the emerald block only after Rayquaza successfully spawned.
            removeEmeraldBlockFromHand(conditionMarker);

            // Mark this condition marker as used so it cannot summon again.
            conditionMarker.addTag(definition.usedTag());
        }

        if (context.summonMarker() != null) {
            // Also mark the main pp_legendary_summon marker as used for this summon.
            context.summonMarker().addTag(definition.usedTag());
        }

        playRayquazaSounds(context);
        messageNearbyPlayers(context);
    }

    /**
     * Removes an emerald block from either hand of the condition marker.
     */
    private void removeEmeraldBlockFromHand(ArmorStand armorStand) {
        if (armorStand.getMainHandItem().is(Items.EMERALD_BLOCK)) {
            armorStand.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return;
        }

        if (armorStand.getOffhandItem().is(Items.EMERALD_BLOCK)) {
            armorStand.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    /**
     * Plays dramatic spawn sounds at Rayquaza's spawn location.
     *
     * These are heard by nearby players automatically because level.playSound(null, ...)
     * broadcasts the sound from the server.
     */
    private void playRayquazaSounds(SummonContext context) {
        BlockPos pos = context.spawnPos();

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

    /**
     * Sends a message to players within 64 blocks of the spawn position.
     *
     * This is the Java-side equivalent of a nearby tellraw.
     */
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