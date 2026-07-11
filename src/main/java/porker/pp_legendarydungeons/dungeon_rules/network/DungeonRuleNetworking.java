
package porker.pp_legendarydungeons.dungeon_rules.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleParentBlockEntity;
import porker.pp_legendarydungeons.blocks.entity.DungeonRuleZoneBlockEntity;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleManager;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRulePermissions;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRulePreviewManager;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleSet;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonCompletionService;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonInstanceRecord;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonRuleSavedData;
import porker.pp_legendarydungeons.dungeon_rules.preset.DungeonRulePresetRegistry;

public final class DungeonRuleNetworking {
    private static final double MAXIMUM_EDIT_DISTANCE_SQUARED = 16.0D * 16.0D;

    private DungeonRuleNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(
                OpenDungeonRuleEditorPayload.TYPE,
                OpenDungeonRuleEditorPayload.CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                UpdateDungeonRuleBlockPayload.TYPE,
                UpdateDungeonRuleBlockPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateDungeonRuleBlockPayload.TYPE,
                (payload, context) -> handleUpdate(context.player(), payload)
        );
    }

    public static void openEditor(ServerPlayer player, BlockPos pos) {
        if (!DungeonRulePermissions.canBypass(player)) {
            return;
        }

        BlockEntity blockEntity = player.serverLevel().getBlockEntity(pos);

        if (blockEntity instanceof DungeonRuleParentBlockEntity parent) {
            String instanceId = parent.getInstanceId();
            String state = DungeonRuleSavedData
                    .get(player.serverLevel().getServer())
                    .get(instanceId)
                    .map(record -> record.state().name())
                    .orElse("UNREGISTERED");

            ServerPlayNetworking.send(
                    player,
                    new OpenDungeonRuleEditorPayload(
                            pos,
                            true,
                            parent.getPresetId(),
                            parent.getLinkChannel(),
                            parent.isEnabled(),
                            0,
                            0,
                            0,
                            1,
                            1,
                            1,
                            0,
                            (int) DungeonRuleManager.DEFAULT_PARENT_LINK_DISTANCE,
                            parent.getRuleOverrides().toPackedInt(),
                            instanceId,
                            state
                    )
            );
            return;
        }

        if (blockEntity instanceof DungeonRuleZoneBlockEntity zone) {
            String instanceId = DungeonRuleManager
                    .getResolvedInstance(player.serverLevel(), pos)
                    .orElse("UNLINKED");

            String state = DungeonRuleSavedData
                    .get(player.serverLevel().getServer())
                    .get(instanceId)
                    .map(record -> record.state().name())
                    .orElse("UNLINKED");

            ServerPlayNetworking.send(
                    player,
                    new OpenDungeonRuleEditorPayload(
                            pos,
                            false,
                            zone.getPresetId(),
                            zone.getLinkChannel(),
                            true,
                            zone.getOffsetX(),
                            zone.getOffsetY(),
                            zone.getOffsetZ(),
                            zone.getSizeX(),
                            zone.getSizeY(),
                            zone.getSizeZ(),
                            zone.getPriority(),
                            (int) zone.getMaximumParentDistance(),
                            zone.getRuleOverrides().toPackedInt(),
                            instanceId,
                            state
                    )
            );
        }
    }

    private static void handleUpdate(
            ServerPlayer player,
            UpdateDungeonRuleBlockPayload payload
    ) {
        if (!DungeonRulePermissions.canBypass(player)) {
            return;
        }

        if (player.distanceToSqr(
                payload.pos().getX() + 0.5D,
                payload.pos().getY() + 0.5D,
                payload.pos().getZ() + 0.5D
        ) > MAXIMUM_EDIT_DISTANCE_SQUARED) {
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(payload.pos());
        ResourceLocation presetId = parsePreset(payload.presetId());
        DungeonRuleSet overrides = DungeonRuleSet.fromPackedInt(
                payload.packedRuleOverrides()
        );

        if (payload.parent()
                && blockEntity instanceof DungeonRuleParentBlockEntity parent) {
            parent.updateConfiguration(
                    presetId.toString(),
                    payload.linkChannel(),
                    payload.enabled(),
                    overrides
            );

            String instanceId = parent.getInstanceId();

            switch (payload.action()) {
                case UpdateDungeonRuleBlockPayload.ACTION_COMPLETE ->
                        DungeonCompletionService.completeInstance(player.serverLevel().getServer(), instanceId);

                case UpdateDungeonRuleBlockPayload.ACTION_REACTIVATE ->
                        DungeonCompletionService.reactivateInstance(player.serverLevel().getServer(), instanceId);

                case UpdateDungeonRuleBlockPayload.ACTION_PREVIEW ->
                        DungeonRulePreviewManager.previewParent(player, payload.pos());

                default -> {
                }
            }

            return;
        }

        if (!payload.parent()
                && blockEntity instanceof DungeonRuleZoneBlockEntity zone) {
            zone.updateConfiguration(
                    presetId.toString(),
                    payload.linkChannel(),
                    payload.offsetX(),
                    payload.offsetY(),
                    payload.offsetZ(),
                    payload.sizeX(),
                    payload.sizeY(),
                    payload.sizeZ(),
                    payload.priority(),
                    payload.maximumParentDistance(),
                    overrides
            );

            if (UpdateDungeonRuleBlockPayload.ACTION_PREVIEW.equals(payload.action())) {
                DungeonRulePreviewManager.previewZone(
                        player,
                        zone.getWorldMinPos(),
                        zone.getWorldMaxPos()
                );
            }
        }
    }

    private static ResourceLocation parsePreset(String value) {
        try {
            return ResourceLocation.parse(value);
        } catch (Exception ignored) {
            return DungeonRulePresetRegistry.STANDARD_DUNGEON_ID;
        }
    }
}
