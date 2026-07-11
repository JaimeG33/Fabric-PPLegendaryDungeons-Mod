
package porker.pp_legendarydungeons.dungeon_rules.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRule;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleSet;
import porker.pp_legendarydungeons.dungeon_rules.network.OpenDungeonRuleEditorPayload;
import porker.pp_legendarydungeons.dungeon_rules.network.UpdateDungeonRuleBlockPayload;

import java.util.ArrayList;
import java.util.List;

public final class DungeonRuleZoneScreen extends Screen {
    private final OpenDungeonRuleEditorPayload snapshot;
    private final DungeonRuleSet decisions;
    private final List<EditBox> numericBoxes = new ArrayList<>();

    private EditBox presetBox;
    private EditBox channelBox;
    private EditBox offsetXBox;
    private EditBox offsetYBox;
    private EditBox offsetZBox;
    private EditBox sizeXBox;
    private EditBox sizeYBox;
    private EditBox sizeZBox;
    private EditBox priorityBox;
    private EditBox parentDistanceBox;

    public DungeonRuleZoneScreen(OpenDungeonRuleEditorPayload snapshot) {
        super(Component.literal("Dungeon Rule Zone"));
        this.snapshot = snapshot;
        this.decisions = DungeonRuleSet.fromPackedInt(snapshot.packedRuleOverrides());
    }

    @Override
    protected void init() {
        int center = width / 2;
        int top = Math.max(8, height / 2 - 116);

        presetBox = textBox(center - 180, top + 17, 245, snapshot.presetId(), 128);
        channelBox = textBox(
                center + 75,
                top + 17,
                105,
                Integer.toString(snapshot.linkChannel()),
                16
        );

        offsetXBox = numericBox(center - 180, top + 48, snapshot.offsetX());
        offsetYBox = numericBox(center - 118, top + 48, snapshot.offsetY());
        offsetZBox = numericBox(center - 56, top + 48, snapshot.offsetZ());

        sizeXBox = numericBox(center + 18, top + 48, snapshot.sizeX());
        sizeYBox = numericBox(center + 80, top + 48, snapshot.sizeY());
        sizeZBox = numericBox(center + 142, top + 48, snapshot.sizeZ());

        priorityBox = numericBox(center - 180, top + 79, snapshot.priority());
        parentDistanceBox = textBox(
                center - 86,
                top + 79,
                86,
                Integer.toString(snapshot.maximumParentDistance()),
                16
        );

        int startY = top + 108;
        DungeonRule[] rules = DungeonRule.values();

        for (int index = 0; index < rules.length; index++) {
            DungeonRule rule = rules[index];
            int column = index % 3;
            int row = index / 3;
            int x = center - 174 + column * 116;
            int y = startY + row * 22;

            addRenderableWidget(
                    Button.builder(
                            ruleMessage(rule),
                            pressed -> {
                                decisions.set(rule, decisions.get(rule).next());
                                pressed.setMessage(ruleMessage(rule));
                            }
                    ).bounds(x, y, 112, 20).build()
            );
        }

        int bottomY = startY + 72;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Save"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_SAVE)
                ).bounds(center - 86, bottomY, 82, 20).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Preview 20s"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_PREVIEW)
                ).bounds(center + 4, bottomY, 100, 20).build()
        );
    }

    private EditBox textBox(
            int x,
            int y,
            int width,
            String value,
            int maxLength
    ) {
        EditBox box = new EditBox(font, x, y, width, 20, Component.empty());
        box.setValue(value);
        box.setMaxLength(maxLength);
        addRenderableWidget(box);
        return box;
    }

    private EditBox numericBox(int x, int y, int value) {
        EditBox box = textBox(x, y, 56, Integer.toString(value), 12);
        numericBoxes.add(box);
        return box;
    }

    private Component ruleMessage(DungeonRule rule) {
        String label = rule.displayName();
        if (label.length() > 13) {
            label = label.substring(0, 13);
        }

        return Component.literal(label + ": " + decisions.get(rule).name());
    }

    private void send(String action) {
        ClientPlayNetworking.send(new UpdateDungeonRuleBlockPayload(
                snapshot.pos(),
                false,
                presetBox.getValue(),
                parseInt(channelBox.getValue(), 0),
                true,
                parseInt(offsetXBox.getValue(), snapshot.offsetX()),
                parseInt(offsetYBox.getValue(), snapshot.offsetY()),
                parseInt(offsetZBox.getValue(), snapshot.offsetZ()),
                parseInt(sizeXBox.getValue(), snapshot.sizeX()),
                parseInt(sizeYBox.getValue(), snapshot.sizeY()),
                parseInt(sizeZBox.getValue(), snapshot.sizeZ()),
                parseInt(priorityBox.getValue(), snapshot.priority()),
                parseInt(
                        parentDistanceBox.getValue(),
                        snapshot.maximumParentDistance()
                ),
                decisions.toPackedInt(),
                action
        ));

        if (UpdateDungeonRuleBlockPayload.ACTION_SAVE.equals(action)) {
            onClose();
        }
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int center = width / 2;
        int top = Math.max(8, height / 2 - 116);

        graphics.drawCenteredString(font, title, center, top, 0xFFFFFF);
        graphics.drawString(font, "Preset", center - 180, top + 6, 0xA0A0A0);
        graphics.drawString(font, "Channel", center + 75, top + 6, 0xA0A0A0);

        graphics.drawString(font, "Offset X / Y / Z", center - 180, top + 37, 0xA0A0A0);
        graphics.drawString(font, "Size X / Y / Z", center + 18, top + 37, 0xA0A0A0);

        graphics.drawString(font, "Priority", center - 180, top + 68, 0xA0A0A0);
        graphics.drawString(font, "Parent range", center - 86, top + 68, 0xA0A0A0);

        graphics.drawString(
                font,
                "Linked instance: " + snapshot.resolvedInstanceId(),
                center + 8,
                top + 82,
                0xA0A0A0
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
