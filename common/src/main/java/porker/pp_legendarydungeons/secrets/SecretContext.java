package porker.pp_legendarydungeons.secrets;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

public record SecretContext(
        ServerLevel level,
        ServerPlayer player,
        ArmorStand starter
) {
}