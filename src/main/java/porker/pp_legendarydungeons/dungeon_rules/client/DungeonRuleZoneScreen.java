package porker.pp_legendarydungeons.dungeon_rules.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
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
    private Button linkStatusButton;

    public DungeonRuleZoneScreen(OpenDungeonRuleEditorPayload snapshot) {
        super(Component.literal("Dungeon Rule Zone"));
        this.snapshot = snapshot;
        this.decisions = DungeonRuleSet.fromPackedInt(snapshot.packedRuleOverrides());
    }

    @Override
    protected void init() {
        int center = width / 2;
        int top = Math.max(8, height / 2 - 170);

        presetBox = textBox(
                center - 240,
                top + 30,
                350,
                snapshot.presetId(),
                128
        );

        channelBox = textBox(
                center + 120,
                top + 30,
                120,
                Integer.toString(snapshot.linkChannel()),
                16
        );

        int firstColumn = center - 240;
        int fieldWidth = 70;
        int fieldGap = 10;

        offsetXBox = numericBox(firstColumn, top + 76, fieldWidth, snapshot.offsetX());
        offsetYBox = numericBox(
                firstColumn + fieldWidth + fieldGap,
                top + 76,
                fieldWidth,
                snapshot.offsetY()
        );
        offsetZBox = numericBox(
                firstColumn + (fieldWidth + fieldGap) * 2,
                top + 76,
                fieldWidth,
                snapshot.offsetZ()
        );

        int sizeStart = center + 10;
        sizeXBox = numericBox(sizeStart, top + 76, fieldWidth, snapshot.sizeX());
        sizeYBox = numericBox(
                sizeStart + fieldWidth + fieldGap,
                top + 76,
                fieldWidth,
                snapshot.sizeY()
        );
        sizeZBox = numericBox(
                sizeStart + (fieldWidth + fieldGap) * 2,
                top + 76,
                fieldWidth,
                snapshot.sizeZ()
        );

        priorityBox = numericBox(center - 240, top + 122, 110, snapshot.priority());
        parentDistanceBox = numericBox(
                center - 120,
                top + 122,
                140,
                snapshot.maximumParentDistance()
        );

        linkStatusButton = addRenderableWidget(
                Button.builder(linkStatusMessage(), button -> {
                    // Display-only link status button.
                }).bounds(center + 32, top + 122, 208, 20).build()
        );

        int startY = top + 190;
        DungeonRule[] rules = DungeonRule.values();

        for (int index = 0; index < rules.length; index++) {
            DungeonRule rule = rules[index];
            int column = index % 3;
            int row = index / 3;
            int x = center - 234 + column * 156;
            int y = startY + row * 22;

            addRenderableWidget(
                    Button.builder(
                            ruleMessage(rule),
                            pressed -> {
                                decisions.set(rule, decisions.get(rule).next());
                                pressed.setMessage(ruleMessage(rule));
                            }
                    ).bounds(x, y, 150, 20).build()
            );
        }

        int bottomY = startY + 72;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Save"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_SAVE)
                ).bounds(center - 116, bottomY, 110, 20).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Preview 20s"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_PREVIEW)
                ).bounds(center + 6, bottomY, 120, 20).build()
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

        /*
         * Set the maximum before the value. Otherwise long preset IDs are cut to
         * EditBox's default maximum before this screen ever displays them.
         */
        box.setMaxLength(maxLength);
        box.setValue(value);

        addRenderableWidget(box);
        return box;
    }

    private EditBox numericBox(int x, int y, int width, int value) {
        EditBox box = textBox(x, y, width, Integer.toString(value), 12);
        numericBoxes.add(box);
        return box;
    }

    private boolean isLinked() {
        String instanceId = snapshot.resolvedInstanceId();

        return instanceId != null
                && !instanceId.isBlank()
                && !"UNLINKED".equalsIgnoreCase(instanceId)
                && !"UNREGISTERED".equalsIgnoreCase(instanceId);
    }

    private Component linkStatusMessage() {
        boolean linked = isLinked();

        return Component.literal("Parent Link: ")
                .append(Component.literal(linked ? "CONNECTED" : "UNLINKED")
                        .withStyle(linked ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private Component ruleMessage(DungeonRule rule) {
        String label = rule.displayName();
        if (label.length() > 16) {
            label = label.substring(0, 16);
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

        // Render widgets first, then draw static labels above the widget layer.
        super.render(graphics, mouseX, mouseY, partialTick);

        int center = width / 2;
        int top = Math.max(8, height / 2 - 170);

        graphics.drawCenteredString(font, title, center, top, 0xFFFFFF);
        graphics.drawString(font, "Preset ID", center - 240, top + 19, 0xFFFFFF);
        graphics.drawString(font, "Link channel", center + 120, top + 19, 0xFFFFFF);

        int firstColumn = center - 240;
        int fieldWidth = 70;
        int fieldGap = 10;

        graphics.drawString(font, "Offset X", firstColumn, top + 64, 0xFFFFFF);
        graphics.drawString(font, "Offset Y", firstColumn + fieldWidth + fieldGap, top + 64, 0xFFFFFF);
        graphics.drawString(font, "Offset Z", firstColumn + (fieldWidth + fieldGap) * 2, top + 64, 0xFFFFFF);

        int sizeStart = center + 10;
        graphics.drawString(font, "Size X", sizeStart, top + 64, 0xFFFFFF);
        graphics.drawString(font, "Size Y", sizeStart + fieldWidth + fieldGap, top + 64, 0xFFFFFF);
        graphics.drawString(font, "Size Z", sizeStart + (fieldWidth + fieldGap) * 2, top + 64, 0xFFFFFF);

        graphics.drawString(font, "Priority", center - 240, top + 110, 0xFFFFFF);
        graphics.drawString(font, "Maximum parent distance", center - 120, top + 110, 0xFFFFFF);

        String linkedInstance = snapshot.resolvedInstanceId();
        if (linkedInstance == null || linkedInstance.isBlank()) {
            linkedInstance = "UNLINKED";
        }

        graphics.drawString(
                font,
                "Linked instance: " + linkedInstance,
                center - 240,
                top + 154,
                0xE0E0E0
        );
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
