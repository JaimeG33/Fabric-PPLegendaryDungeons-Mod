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

import java.util.EnumMap;
import java.util.Map;

public final class DungeonRuleParentScreen extends Screen {
    private final OpenDungeonRuleEditorPayload snapshot;
    private final DungeonRuleSet decisions;
    private final Map<DungeonRule, Button> ruleButtons = new EnumMap<>(DungeonRule.class);

    private EditBox presetBox;
    private EditBox channelBox;
    private Button enabledButton;
    private Button ruleStatusButton;
    private boolean enabled;
    private String instanceState;

    public DungeonRuleParentScreen(OpenDungeonRuleEditorPayload snapshot) {
        super(Component.literal("Dungeon Rule Parent"));
        this.snapshot = snapshot;
        this.decisions = DungeonRuleSet.fromPackedInt(snapshot.packedRuleOverrides());
        this.enabled = snapshot.enabled();
        this.instanceState = snapshot.instanceState();
    }

    @Override
    protected void init() {
        int center = width / 2;
        int top = Math.max(10, height / 2 - 145);

        presetBox = new EditBox(
                font,
                center - 234,
                top + 30,
                350,
                20,
                Component.literal("Preset ID")
        );

        /*
         * EditBox defaults to a shorter maximum length. The maximum must be raised
         * before setValue(...) or long resource IDs are permanently truncated.
         */
        presetBox.setMaxLength(128);
        presetBox.setValue(snapshot.presetId());
        addRenderableWidget(presetBox);

        channelBox = new EditBox(
                font,
                center + 126,
                top + 30,
                108,
                20,
                Component.literal("Link Channel")
        );
        channelBox.setMaxLength(16);
        channelBox.setValue(Integer.toString(snapshot.linkChannel()));
        addRenderableWidget(channelBox);

        enabledButton = addRenderableWidget(
                Button.builder(enabledMessage(), button -> {
                    enabled = !enabled;
                    button.setMessage(enabledMessage());
                    updateRuleStatusButton();
                }).bounds(center - 234, top + 56, 210, 20).build()
        );

        ruleStatusButton = addRenderableWidget(
                Button.builder(ruleStatusMessage(), button -> {
                    // Display-only status button.
                }).bounds(center - 14, top + 56, 248, 20).build()
        );

        int startY = top + 110;
        DungeonRule[] rules = DungeonRule.values();

        for (int index = 0; index < rules.length; index++) {
            DungeonRule rule = rules[index];
            int column = index % 3;
            int row = index / 3;
            int x = center - 234 + column * 156;
            int y = startY + row * 22;

            Button button = Button.builder(
                    ruleMessage(rule),
                    pressed -> {
                        decisions.set(rule, decisions.get(rule).next());
                        pressed.setMessage(ruleMessage(rule));
                    }
            ).bounds(x, y, 150, 20).build();

            ruleButtons.put(rule, button);
            addRenderableWidget(button);
        }

        int bottomY = startY + 72;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Save"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_SAVE)
                ).bounds(center - 234, bottomY, 110, 20).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Preview"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_PREVIEW)
                ).bounds(center - 118, bottomY, 110, 20).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Complete"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_COMPLETE)
                ).bounds(center - 2, bottomY, 110, 20).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Reactivate"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_REACTIVATE)
                ).bounds(center + 114, bottomY, 120, 20).build()
        );
    }

    private Component enabledMessage() {
        return Component.literal("Controller: " + (enabled ? "ENABLED" : "DISABLED"));
    }

    private boolean rulesAreActive() {
        return enabled && "ACTIVE".equalsIgnoreCase(instanceState);
    }

    private Component ruleStatusMessage() {
        boolean active = rulesAreActive();

        return Component.literal("Dungeon Rule Status: ")
                .append(Component.literal(active ? "ENABLED" : "DISABLED")
                        .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private void updateRuleStatusButton() {
        if (ruleStatusButton != null) {
            ruleStatusButton.setMessage(ruleStatusMessage());
        }
    }

    private Component ruleMessage(DungeonRule rule) {
        return Component.literal(
                abbreviate(rule.displayName()) + ": " + decisions.get(rule).name()
        );
    }

    private static String abbreviate(String value) {
        return value.length() <= 16 ? value : value.substring(0, 16);
    }

    private void send(String action) {
        if (UpdateDungeonRuleBlockPayload.ACTION_COMPLETE.equals(action)) {
            instanceState = "COMPLETED";
        } else if (UpdateDungeonRuleBlockPayload.ACTION_REACTIVATE.equals(action)) {
            instanceState = "ACTIVE";
        }

        updateRuleStatusButton();

        ClientPlayNetworking.send(new UpdateDungeonRuleBlockPayload(
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
        int top = Math.max(10, height / 2 - 145);

        graphics.drawCenteredString(font, title, center, top, 0xFFFFFF);
        graphics.drawString(font, "Preset ID", center - 234, top + 19, 0xFFFFFF);
        graphics.drawString(font, "Link channel", center + 126, top + 19, 0xFFFFFF);

        graphics.drawString(
                font,
                "Instance: " + snapshot.resolvedInstanceId(),
                center - 234,
                top + 82,
                0xE0E0E0
        );

        graphics.drawString(
                font,
                "Saved state: " + instanceState,
                center + 64,
                top + 82,
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
