package porker.pp_legendarydungeons.dungeon_rules.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

import java.util.Optional;

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
        boolean previewActive = DungeonRulePreviewManager.isPreviewing(player, pos);

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
                            state,
                            previewActive
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
                            state,
                            previewActive
                    )
            );
        }
    }

    private static void handleUpdate(ServerPlayer player, UpdateDungeonRuleBlockPayload payload) {
        if (!DungeonRulePermissions.canBypass(player)) {
            return;
        }

        if (player.distanceToSqr(
                payload.pos().getX() + 0.5D,
                payload.pos().getY() + 0.5D,
                payload.pos().getZ() + 0.5D
        ) > MAXIMUM_EDIT_DISTANCE_SQUARED) {
            actionBar(
                    player,
                    Component.literal("You are too far away from that dungeon-rule controller.")
                            .withStyle(ChatFormatting.RED)
            );
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
            handleParentUpdate(player, parent, presetId, overrides, payload);
            return;
        }

        if (!payload.parent()
                && blockEntity instanceof DungeonRuleZoneBlockEntity zone) {
            handleZoneUpdate(player, level, zone, presetId, overrides, payload);
            return;
        }

        actionBar(
                player,
                Component.literal("That dungeon-rule controller is no longer available.")
                        .withStyle(ChatFormatting.RED)
        );
    }

    private static void handleParentUpdate(
            ServerPlayer player,
            DungeonRuleParentBlockEntity parent,
            ResourceLocation presetId,
            DungeonRuleSet overrides,
            UpdateDungeonRuleBlockPayload payload
    ) {
        parent.updateConfiguration(
                presetId.toString(),
                payload.linkChannel(),
                payload.enabled(),
                overrides
        );

        String instanceId = parent.getInstanceId();

        switch (payload.action()) {
            case UpdateDungeonRuleBlockPayload.ACTION_COMPLETE -> {
                boolean completed = DungeonCompletionService.completeInstance(
                        player.serverLevel().getServer(),
                        instanceId
                );

                actionBar(
                        player,
                        completed
                                ? Component.literal(
                                        "Dungeon completed. Dungeon rules are now DISABLED."
                                ).withStyle(ChatFormatting.GREEN)
                                : Component.literal(
                                        "Could not complete that dungeon instance."
                                ).withStyle(ChatFormatting.RED)
                );
            }

            case UpdateDungeonRuleBlockPayload.ACTION_REACTIVATE -> {
                boolean reactivated = DungeonCompletionService.reactivateInstance(
                        player.serverLevel().getServer(),
                        instanceId
                );

                actionBar(
                        player,
                        reactivated
                                ? Component.literal(
                                        "Dungeon reactivated. Dungeon rules are now ENABLED."
                                ).withStyle(ChatFormatting.GREEN)
                                : Component.literal(
                                        "Could not reactivate that dungeon instance."
                                ).withStyle(ChatFormatting.RED)
                );
            }

            case UpdateDungeonRuleBlockPayload.ACTION_PREVIEW -> {
                boolean previewEnabled = DungeonRulePreviewManager.toggleParent(
                        player,
                        payload.pos()
                );

                actionBar(
                        player,
                        Component.literal(
                                "Parent preview: " + (previewEnabled ? "ON" : "OFF")
                        ).withStyle(
                                previewEnabled
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.RED
                        )
                );
            }

            case UpdateDungeonRuleBlockPayload.ACTION_SAVE -> {
                boolean active = DungeonRuleSavedData
                        .get(player.serverLevel().getServer())
                        .get(instanceId)
                        .map(DungeonInstanceRecord::rulesAreActive)
                        .orElse(false);

                actionBar(
                        player,
                        Component.literal(
                                "Dungeon parent saved. Dungeon Rule Status: "
                                        + (active ? "ENABLED" : "DISABLED")
                        ).withStyle(
                                active
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.RED
                        )
                );
            }

            default -> actionBar(
                    player,
                    Component.literal("Dungeon parent updated.")
                            .withStyle(ChatFormatting.GREEN)
            );
        }
    }

    private static void handleZoneUpdate(
            ServerPlayer player,
            ServerLevel level,
            DungeonRuleZoneBlockEntity zone,
            ResourceLocation presetId,
            DungeonRuleSet overrides,
            UpdateDungeonRuleBlockPayload payload
    ) {
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

        Optional<String> resolvedInstance = DungeonRuleManager.getResolvedInstance(
                level,
                payload.pos()
        );

        if (UpdateDungeonRuleBlockPayload.ACTION_PREVIEW.equals(payload.action())) {
            boolean previewEnabled = DungeonRulePreviewManager.toggleZone(
                    player,
                    payload.pos(),
                    zone.getWorldMinPos(),
                    zone.getWorldMaxPos()
            );

            actionBar(
                    player,
                    Component.literal(
                            "Zone preview: " + (previewEnabled ? "ON" : "OFF")
                    ).withStyle(
                            previewEnabled
                                    ? ChatFormatting.GREEN
                                    : ChatFormatting.RED
                    )
            );
            return;
        }

        if (resolvedInstance.isPresent()) {
            actionBar(
                    player,
                    Component.literal(
                            "Dungeon zone saved and linked to parent: "
                                    + resolvedInstance.get()
                    ).withStyle(ChatFormatting.GREEN)
            );
        } else {
            actionBar(
                    player,
                    Component.literal(
                            "Dungeon zone saved, but no matching parent was found."
                    ).withStyle(ChatFormatting.RED)
            );
        }
    }

    private static void actionBar(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }

    private static ResourceLocation parsePreset(String value) {
        try {
            return ResourceLocation.parse(value);
        } catch (Exception ignored) {
            return DungeonRulePresetRegistry.STANDARD_DUNGEON_ID;
        }
    }
}
