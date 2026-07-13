package porker.pp_legendarydungeons.dungeon_rules.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.LegendaryDungeons;

/**
 * Server-to-client snapshot used to open either the parent or child editor.
 *
 * <p>The payload keeps its vanilla payload type and stream codec while
 * Architectury owns the cross-loader transport.</p>
 */
public record OpenDungeonRuleEditorPayload(
        BlockPos pos,
        boolean parent,
        String presetId,
        int linkChannel,
        boolean enabled,
        int offsetX,
        int offsetY,
        int offsetZ,
        int sizeX,
        int sizeY,
        int sizeZ,
        int priority,
        int maximumParentDistance,
        int packedRuleOverrides,
        String resolvedInstanceId,
        String instanceState,
        boolean previewActive
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(
                    LegendaryDungeons.MOD_ID,
                    "open_dungeon_rule_editor"
            );

    public static final Type<OpenDungeonRuleEditorPayload> TYPE =
            new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OpenDungeonRuleEditorPayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> payload.write(buffer),
                    OpenDungeonRuleEditorPayload::read
            );

    private void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(parent);
        buffer.writeUtf(presetId);
        buffer.writeVarInt(linkChannel);
        buffer.writeBoolean(enabled);
        buffer.writeVarInt(offsetX);
        buffer.writeVarInt(offsetY);
        buffer.writeVarInt(offsetZ);
        buffer.writeVarInt(sizeX);
        buffer.writeVarInt(sizeY);
        buffer.writeVarInt(sizeZ);
        buffer.writeVarInt(priority);
        buffer.writeVarInt(maximumParentDistance);
        buffer.writeInt(packedRuleOverrides);
        buffer.writeUtf(resolvedInstanceId);
        buffer.writeUtf(instanceState);
        buffer.writeBoolean(previewActive);
    }

    private static OpenDungeonRuleEditorPayload read(
            FriendlyByteBuf buffer
    ) {
        return new OpenDungeonRuleEditorPayload(
                buffer.readBlockPos(),
                buffer.readBoolean(),
                buffer.readUtf(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readInt(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readBoolean()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
