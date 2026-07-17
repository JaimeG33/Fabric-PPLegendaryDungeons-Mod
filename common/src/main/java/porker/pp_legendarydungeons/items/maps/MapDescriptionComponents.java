package porker.pp_legendarydungeons.items.maps;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import porker.pp_legendarydungeons.features.wtraders.json.MapDescriptionLineJson;
import porker.pp_legendarydungeons.features.wtraders.json.WTraderJsonValues;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts validated map-description JSON lines into Minecraft components.
 */
public final class MapDescriptionComponents {
    private MapDescriptionComponents() {
    }

    public static List<Component> fromJson(List<MapDescriptionLineJson> lines) {
        List<Component> output = new ArrayList<>();

        for (MapDescriptionLineJson line : lines) {
            if (line == null || WTraderJsonValues.isBlank(line.text)) {
                continue;
            }

            ChatFormatting color = WTraderJsonValues.isBlank(line.color)
                    ? null
                    : ChatFormatting.getByName(line.color);

            MutableComponent component = Component.literal(line.text);
            component.withStyle(style -> applyStyle(style, line, color));
            output.add(component);
        }

        return List.copyOf(output);
    }

    private static Style applyStyle(
            Style style,
            MapDescriptionLineJson line,
            ChatFormatting color
    ) {
        Style updated = style;

        if (color != null) {
            updated = updated.applyFormat(color);
        }

        if (line.bold != null) {
            updated = updated.withBold(line.bold);
        }

        if (line.italic != null) {
            updated = updated.withItalic(line.italic);
        }

        return updated;
    }
}
