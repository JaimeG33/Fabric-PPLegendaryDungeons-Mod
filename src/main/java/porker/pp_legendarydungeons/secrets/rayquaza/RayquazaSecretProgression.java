package porker.pp_legendarydungeons.secrets.rayquaza;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.secrets.SecretContext;
import porker.pp_legendarydungeons.secrets.SecretItemChecks;
import porker.pp_legendarydungeons.setup.ModScoreboards;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class RayquazaSecretProgression {
    private static final String STEP_1_TAG = "scs_secret_step1";
    private static final String STEP_2_TAG = "scs_secret_step2";

    private static final double STEP_SEARCH_RADIUS = 100.0D;

    private RayquazaSecretProgression() {
    }

    public static void check(SecretContext context) {
        ArmorStand starter = context.starter();

        if (ModScoreboards.getEntityScore(starter, ModScoreboards.EVENT_TRIGGERED) >= 1) {
            return;
        }

        int progress = ModScoreboards.getEntityScore(starter, ModScoreboards.SCS_SECRETS);

        Optional<ArmorStand> step1 = findNearestTaggedStand(context, STEP_1_TAG);
        Optional<ArmorStand> step2 = findNearestTaggedStand(context, STEP_2_TAG);

        if (progress < 1 && step1.isPresent() && SecretItemChecks.hasBlueOrbItem(step1.get())) {
            ModScoreboards.setEntityScore(starter, ModScoreboards.SCS_SECRETS, 1);
            progress = 1;
            playStepSound(context);
        }

        if (progress == 1 && step2.isPresent() && SecretItemChecks.hasRedOrbItem(step2.get())) {
            ModScoreboards.setEntityScore(starter, ModScoreboards.SCS_SECRETS, 2);
            progress = 2;
            playStepSound(context);
        }

        if (progress == 2 && SecretItemChecks.hasMeteoriteItem(starter)) {
            ModScoreboards.setEntityScore(starter, ModScoreboards.SCS_SECRETS, 3);
            progress = 3;
            playStepSound(context);
        }

        if (progress == 3) {
            unlockPlayer(context);
        }
    }

    private static void unlockPlayer(SecretContext context) {
        ArmorStand starter = context.starter();

        ModScoreboards.setEntityScore(context.player(), ModScoreboards.SCS_SECRETS, 10);
        ModScoreboards.setEntityScore(starter, ModScoreboards.EVENT_TRIGGERED, 1);

        context.player().sendSystemMessage(
                net.minecraft.network.chat.Component
                        .literal("Memories surface of a battle long forgotten...")
                        .withStyle(net.minecraft.ChatFormatting.DARK_GRAY, net.minecraft.ChatFormatting.BOLD)
        );

        context.level().playSound(
                null,
                starter.blockPosition(),
                SoundEvents.ENDER_DRAGON_AMBIENT,
                SoundSource.HOSTILE,
                1.5F,
                0.8F
        );
    }

    private static Optional<ArmorStand> findNearestTaggedStand(
            SecretContext context,
            String tag
    ) {
        ArmorStand starter = context.starter();
        AABB searchBox = starter.getBoundingBox().inflate(STEP_SEARCH_RADIUS);

        List<ArmorStand> matches = context.level().getEntitiesOfClass(
                ArmorStand.class,
                searchBox,
                armorStand ->
                        armorStand.isAlive()
                                && armorStand.getTags().contains(tag)
        );

        return matches.stream()
                .min(Comparator.comparingDouble(
                        armorStand -> armorStand.distanceToSqr(starter)
                ));
    }

    private static void playStepSound(SecretContext context) {
        context.level().playSound(
                null,
                context.starter().blockPosition(),
                SoundEvents.WITHER_SPAWN,
                SoundSource.HOSTILE,
                1.0F,
                1.0F
        );
    }
}