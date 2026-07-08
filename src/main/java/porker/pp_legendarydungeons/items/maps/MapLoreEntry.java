package porker.pp_legendarydungeons.items.maps;

import net.minecraft.network.chat.Component;

import java.util.List;

public record MapLoreEntry(
        String targetId,
        List<Component> descriptionLines
) {
}
