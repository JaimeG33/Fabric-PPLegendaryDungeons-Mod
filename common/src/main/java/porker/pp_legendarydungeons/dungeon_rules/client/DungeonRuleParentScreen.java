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

import java.util.EnumMap;
import java.util.Map;

public final class DungeonRuleParentScreen extends Screen {
    private static final int OUTER_MARGIN = 8;
    private static final int WIDGET_HEIGHT = 20;
    private static final int SMALL_GAP = 4;
    private static final int RULE_COLUMNS = 3;

    private final OpenDungeonRuleEditorPayload snapshot;
    private final DungeonRuleSet decisions;
    private final Map<DungeonRule, Button> ruleButtons =
            new EnumMap<>(DungeonRule.class);

    private EditBox presetBox;
    private EditBox channelBox;
    private Button enabledButton;
    private Button ruleStatusButton;
    private Button previewButton;

    private boolean enabled;
    private boolean previewActive;
    private String instanceState;

    private int panelLeft;
    private int panelWidth;
    private int contentTop;
    private int ruleButtonWidth;

    public DungeonRuleParentScreen(OpenDungeonRuleEditorPayload snapshot) {
        super(Component.literal("Dungeon Rule Parent"));
        this.snapshot = snapshot;
        this.decisions =
                DungeonRuleSet.fromPackedInt(snapshot.packedRuleOverrides());
        this.enabled = snapshot.enabled();
        this.previewActive = snapshot.previewActive();
        this.instanceState = snapshot.instanceState();
    }

    @Override
    protected void init() {
        panelWidth = Math.max(
                280,
                Math.min(480, width - OUTER_MARGIN * 2)
        );
        panelLeft = (width - panelWidth) / 2;

        /*
         * The panel needs 174 scaled pixels from title to action-row bottom.
         * Positioning it from its measured height keeps GUI scales 3 and 4
         * usable instead of relying on a fixed screen-center offset.
         */
        contentTop = Math.max(
                4,
                Math.min(40, (height - 174) / 2)
        );

        int channelWidth = channelWidth();
        int presetWidth = panelWidth - channelWidth - 8;
        int channelX = panelLeft + presetWidth + 8;

        presetBox = new EditBox(
                font,
                panelLeft,
                contentTop + 24,
                presetWidth,
                WIDGET_HEIGHT,
                Component.literal("Preset ID")
        );
        presetBox.setMaxLength(128);
        presetBox.setValue(snapshot.presetId());
        addRenderableWidget(presetBox);

        channelBox = new EditBox(
                font,
                channelX,
                contentTop + 24,
                channelWidth,
                WIDGET_HEIGHT,
                Component.literal("Link Channel")
        );
        channelBox.setMaxLength(16);
        channelBox.setValue(Integer.toString(snapshot.linkChannel()));
        addRenderableWidget(channelBox);

        int statusWidth = (panelWidth - SMALL_GAP) / 2;

        enabledButton = addRenderableWidget(
                Button.builder(enabledMessage(), button -> {
                    enabled = !enabled;
                    button.setMessage(enabledMessage());
                    updateRuleStatusButton();
                }).bounds(
                        panelLeft,
                        contentTop + 48,
                        statusWidth,
                        WIDGET_HEIGHT
                ).build()
        );

        ruleStatusButton = addRenderableWidget(
                Button.builder(ruleStatusMessage(), button -> {
                    // Display-only status button.
                }).bounds(
                        panelLeft + statusWidth + SMALL_GAP,
                        contentTop + 48,
                        panelWidth - statusWidth - SMALL_GAP,
                        WIDGET_HEIGHT
                ).build()
        );

        int ruleStartY = contentTop + 86;
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
            int y = ruleStartY + row * 21;

            Button button = Button.builder(
                    ruleMessage(rule),
                    pressed -> {
                        decisions.set(rule, decisions.get(rule).next());
                        pressed.setMessage(ruleMessage(rule));
                    }
            ).bounds(x, y, ruleButtonWidth, WIDGET_HEIGHT).build();

            ruleButtons.put(rule, button);
            addRenderableWidget(button);
        }

        int footerY = contentTop + 154;
        int footerButtonWidth =
                (panelWidth - SMALL_GAP * 3) / 4;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Save"),
                        button -> send(
                                UpdateDungeonRuleBlockPayload.ACTION_SAVE
                        )
                ).bounds(
                        panelLeft,
                        footerY,
                        footerButtonWidth,
                        WIDGET_HEIGHT
                ).build()
        );

        previewButton = addRenderableWidget(
                Button.builder(
                        previewMessage(),
                        button -> togglePreview()
                ).bounds(
                        panelLeft + footerButtonWidth + SMALL_GAP,
                        footerY,
                        footerButtonWidth,
                        WIDGET_HEIGHT
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Complete"),
                        button -> send(
                                UpdateDungeonRuleBlockPayload.ACTION_COMPLETE
                        )
                ).bounds(
                        panelLeft + (footerButtonWidth + SMALL_GAP) * 2,
                        footerY,
                        footerButtonWidth,
                        WIDGET_HEIGHT
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Reactivate"),
                        button -> send(
                                UpdateDungeonRuleBlockPayload.ACTION_REACTIVATE
                        )
                ).bounds(
                        panelLeft + (footerButtonWidth + SMALL_GAP) * 3,
                        footerY,
                        panelWidth - (footerButtonWidth + SMALL_GAP) * 3,
                        WIDGET_HEIGHT
                ).build()
        );
    }

    private int channelWidth() {
        return panelWidth < 380 ? 92 : 120;
    }

    private Component enabledMessage() {
        return Component.literal(
                "Controller: " + (enabled ? "ENABLED" : "DISABLED")
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

    private boolean rulesAreActive() {
        return enabled && "ACTIVE".equalsIgnoreCase(instanceState);
    }

    private Component ruleStatusMessage() {
        boolean active = rulesAreActive();

        return Component.literal("Dungeon Rule Status: ")
                .append(
                        Component.literal(
                                active ? "ENABLED" : "DISABLED"
                        ).withStyle(
                                active
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.RED
                        )
                );
    }

    private void updateRuleStatusButton() {
        if (ruleStatusButton != null) {
            ruleStatusButton.setMessage(ruleStatusMessage());
        }
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

    private static String abbreviate(String value, int maximumLength) {
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
        if (UpdateDungeonRuleBlockPayload.ACTION_COMPLETE.equals(action)) {
            instanceState = "COMPLETED";
        } else if (UpdateDungeonRuleBlockPayload.ACTION_REACTIVATE.equals(
                action
        )) {
            instanceState = "ACTIVE";
        }

        updateRuleStatusButton();

        DungeonRuleClientNetworking.sendUpdate(
                new UpdateDungeonRuleBlockPayload(
                        snapshot.pos(),
                        true,
                        presetBox.getValue(),
                        parseInt(channelBox.getValue(), 0),
                        enabled,
                        0,
                        0,
                        0,
                        1,
                        1,
                        1,
                        0,
                        512,
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
        int infoGap = 8;
        int infoWidth = (panelWidth - infoGap) / 2;

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
                contentTop + 13,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                "Link channel",
                channelX,
                contentTop + 13,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                fitText(
                        "Instance: " + snapshot.resolvedInstanceId(),
                        infoWidth
                ),
                panelLeft,
                contentTop + 73,
                0xE0E0E0
        );

        graphics.drawString(
                font,
                fitText(
                        "Saved state: " + instanceState,
                        panelWidth - infoWidth - infoGap
                ),
                panelLeft + infoWidth + infoGap,
                contentTop + 73,
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
