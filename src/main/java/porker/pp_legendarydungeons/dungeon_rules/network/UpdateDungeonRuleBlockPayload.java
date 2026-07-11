
package porker.pp_legendarydungeons.dungeon_rules.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.ProfessorPorkersLegendaryDungeons;

/**
 * Client-to-server edit/action request.
 *
 * The server re-validates permission, distance, block type, and value ranges.
 */
public record UpdateDungeonRuleBlockPayload(
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
        String action
) implements CustomPacketPayload {
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_PREVIEW = "preview";
    public static final String ACTION_COMPLETE = "complete";
    public static final String ACTION_REACTIVATE = "reactivate";

    public static final Type<UpdateDungeonRuleBlockPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    ProfessorPorkersLegendaryDungeons.MOD_ID,
                    "update_dungeon_rule_block"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDungeonRuleBlockPayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> payload.write(buffer),
                    UpdateDungeonRuleBlockPayload::read
            );

    private void write(RegistryFriendlyByteBuf buffer) {
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
        buffer.writeUtf(action);
    }

    private static UpdateDungeonRuleBlockPayload read(RegistryFriendlyByteBuf buffer) {
        return new UpdateDungeonRuleBlockPayload(
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
                buffer.readUtf()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
