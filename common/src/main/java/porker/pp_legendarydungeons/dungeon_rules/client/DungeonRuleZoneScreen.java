package porker.pp_legendarydungeons.dungeon_rules.client;

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
    private static final int OUTER_MARGIN = 8;
    private static final int WIDGET_HEIGHT = 20;
    private static final int SMALL_GAP = 4;
    private static final int RULE_COLUMNS = 3;

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
    private Button previewButton;

    private boolean previewActive;

    private int panelLeft;
    private int panelWidth;
    private int contentTop;
    private int ruleButtonWidth;

    public DungeonRuleZoneScreen(OpenDungeonRuleEditorPayload snapshot) {
        super(Component.literal("Dungeon Rule Zone"));
        this.snapshot = snapshot;
        this.decisions =
                DungeonRuleSet.fromPackedInt(snapshot.packedRuleOverrides());
        this.previewActive = snapshot.previewActive();
    }

    @Override
    protected void init() {
        numericBoxes.clear();

        panelWidth = Math.max(
                280,
                Math.min(480, width - OUTER_MARGIN * 2)
        );
        panelLeft = (width - panelWidth) / 2;

        /*
         * The compact measured panel is 210 scaled pixels tall. Positioning it
         * from its own measured height keeps the action row visible at GUI
         * scales 3 and 4.
         */
        contentTop = Math.max(
                4,
                Math.min(32, (height - 210) / 2)
        );

        int channelWidth = channelWidth();
        int presetWidth = panelWidth - channelWidth - 8;
        int channelX = panelLeft + presetWidth + 8;

        presetBox = textBox(
                panelLeft,
                contentTop + 21,
                presetWidth,
                snapshot.presetId(),
                128
        );

        channelBox = textBox(
                channelX,
                contentTop + 21,
                channelWidth,
                Integer.toString(snapshot.linkChannel()),
                16
        );

        int groupGap = 12;
        int groupWidth = (panelWidth - groupGap) / 2;
        int fieldGap = 4;
        int fieldWidth = (groupWidth - fieldGap * 2) / 3;
        int sizeStart = panelLeft + groupWidth + groupGap;

        offsetXBox = numericBox(
                panelLeft,
                contentTop + 55,
                fieldWidth,
                snapshot.offsetX()
        );
        offsetYBox = numericBox(
                panelLeft + fieldWidth + fieldGap,
                contentTop + 55,
                fieldWidth,
                snapshot.offsetY()
        );
        offsetZBox = numericBox(
                panelLeft + (fieldWidth + fieldGap) * 2,
                contentTop + 55,
                fieldWidth,
                snapshot.offsetZ()
        );

        sizeXBox = numericBox(
                sizeStart,
                contentTop + 55,
                fieldWidth,
                snapshot.sizeX()
        );
        sizeYBox = numericBox(
                sizeStart + fieldWidth + fieldGap,
                contentTop + 55,
                fieldWidth,
                snapshot.sizeY()
        );
        sizeZBox = numericBox(
                sizeStart + (fieldWidth + fieldGap) * 2,
                contentTop + 55,
                fieldWidth,
                snapshot.sizeZ()
        );

        int priorityWidth = panelWidth < 380 ? 72 : 110;
        int distanceWidth = panelWidth < 380 ? 104 : 140;
        int linkWidth = panelWidth
                - priorityWidth
                - distanceWidth
                - SMALL_GAP * 2;

        priorityBox = numericBox(
                panelLeft,
                contentTop + 89,
                priorityWidth,
                snapshot.priority()
        );

        parentDistanceBox = numericBox(
                panelLeft + priorityWidth + SMALL_GAP,
                contentTop + 89,
                distanceWidth,
                snapshot.maximumParentDistance()
        );

        linkStatusButton = addRenderableWidget(
                Button.builder(linkStatusMessage(), button -> {
                    // Display-only link status button.
                }).bounds(
                        panelLeft
                                + priorityWidth
                                + distanceWidth
                                + SMALL_GAP * 2,
                        contentTop + 89,
                        linkWidth,
                        WIDGET_HEIGHT
                ).build()
        );

        int ruleStartY = contentTop + 125;
        ruleButtonWidth =
                (panelWidth - SMALL_GAP * (RULE_COLUMNS - 1))
                        / RULE_COLUMNS;

        DungeonRule[] rules = DungeonRule.values();

        for (int index = 0; index < rules.length; index++) {
            DungeonRule rule = rules[index];
            int column = index % RULE_COLUMNS;
            int row = index / RULE_COLUMNS;
            int x = panelLeft
                    + column * (ruleButtonWidth + SMALL_GAP);
            int y = ruleStartY + row * 20;

            addRenderableWidget(
                    Button.builder(
                            ruleMessage(rule),
                            pressed -> {
                                decisions.set(
                                        rule,
                                        decisions.get(rule).next()
                                );
                                pressed.setMessage(ruleMessage(rule));
                            }
                    ).bounds(
                            x,
                            y,
                            ruleButtonWidth,
                            WIDGET_HEIGHT
                    ).build()
            );
        }

        int footerY = contentTop + 190;
        int actionWidth = Math.min(
                140,
                (panelWidth - 8) / 2
        );
        int actionLeft = panelLeft
                + (panelWidth - actionWidth * 2 - 8) / 2;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Save"),
                        button -> send(
                                UpdateDungeonRuleBlockPayload.ACTION_SAVE
                        )
                ).bounds(
                        actionLeft,
                        footerY,
                        actionWidth,
                        WIDGET_HEIGHT
                ).build()
        );

        previewButton = addRenderableWidget(
                Button.builder(
                        previewMessage(),
                        button -> togglePreview()
                ).bounds(
                        actionLeft + actionWidth + 8,
                        footerY,
                        actionWidth,
                        WIDGET_HEIGHT
                ).build()
        );
    }

    private int channelWidth() {
        return panelWidth < 380 ? 92 : 120;
    }

    private EditBox textBox(
            int x,
            int y,
            int width,
            String value,
            int maxLength
    ) {
        EditBox box = new EditBox(
                font,
                x,
                y,
                width,
                WIDGET_HEIGHT,
                Component.empty()
        );
        box.setMaxLength(maxLength);
        box.setValue(value);
        addRenderableWidget(box);
        return box;
    }

    private EditBox numericBox(
            int x,
            int y,
            int width,
            int value
    ) {
        EditBox box = textBox(
                x,
                y,
                width,
                Integer.toString(value),
                12
        );
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
        String prefix = panelWidth < 400
                ? "Link: "
                : "Parent Link: ";

        return Component.literal(prefix)
                .append(
                        Component.literal(
                                linked ? "CONNECTED" : "UNLINKED"
                        ).withStyle(
                                linked
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.RED
                        )
                );
    }

    private Component previewMessage() {
        return Component.literal("Preview: ")
                .append(
                        Component.literal(
                                previewActive ? "ON" : "OFF"
                        ).withStyle(
                                previewActive
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.RED
                        )
                );
    }

    private void togglePreview() {
        previewActive = !previewActive;

        if (previewButton != null) {
            previewButton.setMessage(previewMessage());
        }

        send(UpdateDungeonRuleBlockPayload.ACTION_PREVIEW);
    }

    private Component ruleMessage(DungeonRule rule) {
        String ruleName = ruleButtonWidth < 135
                ? compactRuleName(rule)
                : abbreviate(rule.displayName(), 16);

        String decisionName = decisions.get(rule).name();

        if (ruleButtonWidth < 112) {
            decisionName = decisionName.substring(0, 1);
        }

        return Component.literal(ruleName + ": " + decisionName);
    }

    private static String compactRuleName(DungeonRule rule) {
        return switch (rule) {
            case BLOCK_BREAKING -> "Break";
            case BLOCK_PLACEMENT -> "Place";
            case MINING_FATIGUE -> "Fatigue";
            case EXPLOSIONS -> "Explode";
            case BED_USE -> "Beds";
            case PC_USE -> "PC";
            case HEALER_USE -> "Healer";
            case PORTABLE_PC_USE -> "Port PC";
            case PORTABLE_HEALER_USE -> "Port Heal";
        };
    }

    private static String abbreviate(
            String value,
            int maximumLength
    ) {
        return value.length() <= maximumLength
                ? value
                : value.substring(0, maximumLength);
    }

    private String fitText(String value, int maximumWidth) {
        if (value == null) {
            return "";
        }

        if (font.width(value) <= maximumWidth) {
            return value;
        }

        String suffix = "...";
        int suffixWidth = font.width(suffix);
        int end = value.length();

        while (end > 0
                && font.width(value.substring(0, end))
                + suffixWidth > maximumWidth) {
            end--;
        }

        return end <= 0
                ? suffix
                : value.substring(0, end) + suffix;
    }

    private void send(String action) {
        DungeonRuleClientNetworking.sendUpdate(
                new UpdateDungeonRuleBlockPayload(
                        snapshot.pos(),
                        false,
                        presetBox.getValue(),
                        parseInt(channelBox.getValue(), 0),
                        true,
                        parseInt(
                                offsetXBox.getValue(),
                                snapshot.offsetX()
                        ),
                        parseInt(
                                offsetYBox.getValue(),
                                snapshot.offsetY()
                        ),
                        parseInt(
                                offsetZBox.getValue(),
                                snapshot.offsetZ()
                        ),
                        parseInt(
                                sizeXBox.getValue(),
                                snapshot.sizeX()
                        ),
                        parseInt(
                                sizeYBox.getValue(),
                                snapshot.sizeY()
                        ),
                        parseInt(
                                sizeZBox.getValue(),
                                snapshot.sizeZ()
                        ),
                        parseInt(
                                priorityBox.getValue(),
                                snapshot.priority()
                        ),
                        parseInt(
                                parentDistanceBox.getValue(),
                                snapshot.maximumParentDistance()
                        ),
                        decisions.toPackedInt(),
                        action
                )
        );

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
        super.render(graphics, mouseX, mouseY, partialTick);

        int channelWidth = channelWidth();
        int presetWidth = panelWidth - channelWidth - 8;
        int channelX = panelLeft + presetWidth + 8;

        int groupGap = 12;
        int groupWidth = (panelWidth - groupGap) / 2;
        int fieldGap = 4;
        int fieldWidth = (groupWidth - fieldGap * 2) / 3;
        int sizeStart = panelLeft + groupWidth + groupGap;

        int priorityWidth = panelWidth < 380 ? 72 : 110;

        graphics.drawCenteredString(
                font,
                title,
                width / 2,
                contentTop,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                "Preset ID",
                panelLeft,
                contentTop + 11,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                "Link channel",
                channelX,
                contentTop + 11,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                "Offset X",
                panelLeft,
                contentTop + 45,
                0xFFFFFF
        );
        graphics.drawString(
                font,
                "Offset Y",
                panelLeft + fieldWidth + fieldGap,
                contentTop + 45,
                0xFFFFFF
        );
        graphics.drawString(
                font,
                "Offset Z",
                panelLeft + (fieldWidth + fieldGap) * 2,
                contentTop + 45,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                "Size X",
                sizeStart,
                contentTop + 45,
                0xFFFFFF
        );
        graphics.drawString(
                font,
                "Size Y",
                sizeStart + fieldWidth + fieldGap,
                contentTop + 45,
                0xFFFFFF
        );
        graphics.drawString(
                font,
                "Size Z",
                sizeStart + (fieldWidth + fieldGap) * 2,
                contentTop + 45,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                "Priority",
                panelLeft,
                contentTop + 79,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                panelWidth < 400
                        ? "Max parent distance"
                        : "Maximum parent distance",
                panelLeft + priorityWidth + SMALL_GAP,
                contentTop + 79,
                0xFFFFFF
        );

        String linkedInstance = snapshot.resolvedInstanceId();

        if (linkedInstance == null || linkedInstance.isBlank()) {
            linkedInstance = "UNLINKED";
        }

        graphics.drawString(
                font,
                fitText(
                        "Linked instance: " + linkedInstance,
                        panelWidth
                ),
                panelLeft,
                contentTop + 113,
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
