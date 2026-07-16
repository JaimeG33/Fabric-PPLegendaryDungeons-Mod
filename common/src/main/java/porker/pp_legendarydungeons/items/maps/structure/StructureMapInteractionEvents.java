package porker.pp_legendarydungeons.items.maps.structure;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.items.maps.profile.MapProfileService;

import java.util.Optional;

/**
 * Server-authoritative claim handling for structure armor stands that visibly
 * hold a placeholder map.
 *
 * <p>The armor stand opts in with the {@value #HOLDER_TAG} entity tag. The
 * blank map in its main hand supplies:</p>
 *
 * <ul>
 *     <li>{@code pp_gimmick = structure_map_placeholder}</li>
 *     <li>{@code pp_map_profile = namespace:profile_id}</li>
 * </ul>
 *
 * <p>No ticker is involved. The functional exploration map is generated only
 * when a player actually interacts with a valid holder.</p>
 */
public final class StructureMapInteractionEvents {
    public static final String HOLDER_TAG = "pp_grab_map";
    public static final String CLAIMED_TAG = "pp_map_claimed";

    private static final String GIMMICK_KEY = "pp_gimmick";
    private static final String PROFILE_KEY = "pp_map_profile";
    private static final String PLACEHOLDER_GIMMICK =
            "structure_map_placeholder";

    private static final double MAX_INTERACTION_DISTANCE_SQUARED = 36.0D;

    private static boolean registered = false;

    private StructureMapInteractionEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        InteractionEvent.INTERACT_ENTITY.register(
                StructureMapInteractionEvents::onInteractEntity
        );
    }

    private static EventResult onInteractEntity(
            Player player,
            Entity entity,
            InteractionHand hand
    ) {
        if (!(entity instanceof ArmorStand holder)) {
            return EventResult.pass();
        }

        ItemStack displayedStack =
                holder.getItemBySlot(EquipmentSlot.MAINHAND);

        /*
         * Entity command tags are authoritative on the server but are not a
         * dependable client-side marker. The synchronized held-item components
         * let the client suppress normal armor-stand swapping immediately.
         */
        if (player.level().isClientSide) {
            return readProfileId(displayedStack).isPresent()
                    ? EventResult.interruptTrue()
                    : EventResult.pass();
        }

        if (!(player instanceof ServerPlayer serverPlayer)
                || !(holder.level() instanceof ServerLevel serverLevel)) {
            return EventResult.pass();
        }

        if (!holder.getTags().contains(HOLDER_TAG)) {
            return EventResult.pass();
        }

        /*
         * A tagged holder remains protected after its map is claimed, even
         * though its main hand is then empty.
         */
        if (holder.getTags().contains(CLAIMED_TAG)) {
            return EventResult.interruptTrue();
        }

        if (hand != InteractionHand.MAIN_HAND
                || holder.distanceToSqr(serverPlayer)
                > MAX_INTERACTION_DISTANCE_SQUARED) {
            return EventResult.interruptTrue();
        }

        Optional<ResourceLocation> profileId =
                readProfileId(displayedStack);

        if (profileId.isEmpty()) {
            LegendaryDungeons.LOGGER.warn(
                    "[Structure Map] Tagged map holder at {} is missing a valid placeholder profile.",
                    holder.blockPosition()
            );
            return EventResult.interruptTrue();
        }

        Optional<ItemStack> generated = MapProfileService.generate(
                serverLevel,
                holder.position(),
                serverPlayer,
                profileId.get()
        );

        if (generated.isEmpty()) {
            /*
             * Do not consume the holder after a failed lookup or loot-table
             * execution. Corrected datapack data can be reloaded and retried.
             */
            return EventResult.interruptTrue();
        }

        ItemStack generatedMap = generated.get();

        if (!generatedMap.is(Items.FILLED_MAP)) {
            LegendaryDungeons.LOGGER.warn(
                    "[Structure Map] Profile {} generated {}, not a filled map, at {}.",
                    profileId.get(),
                    generatedMap.getItem(),
                    holder.blockPosition()
            );
            return EventResult.interruptTrue();
        }

        /*
         * Commit the one-time claim before handing over the stack. Server-side
         * interactions are sequential, so a second player will observe the
         * claimed tag and cannot receive a duplicate.
         */
        holder.addTag(CLAIMED_TAG);
        holder.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        if (!serverPlayer.getInventory().add(generatedMap)) {
            serverPlayer.drop(generatedMap, false);
        }

        return EventResult.interruptTrue();
    }

    private static Optional<ResourceLocation> readProfileId(ItemStack stack) {
        if (stack == null || !stack.is(Items.MAP)) {
            return Optional.empty();
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

        if (customData == null || customData.isEmpty()) {
            return Optional.empty();
        }

        CompoundTag tag = customData.copyTag();

        if (!PLACEHOLDER_GIMMICK.equals(tag.getString(GIMMICK_KEY))) {
            return Optional.empty();
        }

        String rawProfileId = tag.getString(PROFILE_KEY);

        if (rawProfileId == null || rawProfileId.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(ResourceLocation.parse(rawProfileId));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
