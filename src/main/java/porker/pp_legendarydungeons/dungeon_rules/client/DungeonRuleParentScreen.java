
package porker.pp_legendarydungeons.dungeon_rules.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRule;
import porker.pp_legendarydungeons.dungeon_rules.DungeonRuleSet;
import porker.pp_legendarydungeons.dungeon_rules.RuleDecision;
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
    private boolean enabled;

    public DungeonRuleParentScreen(OpenDungeonRuleEditorPayload snapshot) {
        super(Component.literal("Dungeon Rule Parent"));
        this.snapshot = snapshot;
        this.decisions = DungeonRuleSet.fromPackedInt(snapshot.packedRuleOverrides());
        this.enabled = snapshot.enabled();
    }

    @Override
    protected void init() {
        int center = width / 2;
        int top = Math.max(12, height / 2 - 105);

        presetBox = new EditBox(
                font,
                center - 150,
                top + 18,
                220,
                20,
                Component.literal("Preset ID")
        );
        presetBox.setValue(snapshot.presetId());
        presetBox.setMaxLength(128);
        addRenderableWidget(presetBox);

        channelBox = new EditBox(
                font,
                center + 80,
                top + 18,
                70,
                20,
                Component.literal("Channel")
        );
        channelBox.setValue(Integer.toString(snapshot.linkChannel()));
        addRenderableWidget(channelBox);

        enabledButton = addRenderableWidget(
                Button.builder(enabledMessage(), button -> {
                    enabled = !enabled;
                    button.setMessage(enabledMessage());
                }).bounds(center - 150, top + 44, 145, 20).build()
        );

        int startY = top + 70;
        DungeonRule[] rules = DungeonRule.values();

        for (int index = 0; index < rules.length; index++) {
            DungeonRule rule = rules[index];
            int column = index % 3;
            int row = index / 3;
            int x = center - 174 + column * 116;
            int y = startY + row * 22;

            Button button = Button.builder(
                    ruleMessage(rule),
                    pressed -> {
                        decisions.set(rule, decisions.get(rule).next());
                        pressed.setMessage(ruleMessage(rule));
                    }
            ).bounds(x, y, 112, 20).build();

            ruleButtons.put(rule, button);
            addRenderableWidget(button);
        }

        int bottomY = startY + 72;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Save"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_SAVE)
                ).bounds(center - 174, bottomY, 82, 20).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Preview"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_PREVIEW)
                ).bounds(center - 88, bottomY, 82, 20).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Complete"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_COMPLETE)
                ).bounds(center - 2, bottomY, 82, 20).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Reactivate"),
                        button -> send(UpdateDungeonRuleBlockPayload.ACTION_REACTIVATE)
                ).bounds(center + 84, bottomY, 90, 20).build()
        );
    }

    private Component enabledMessage() {
        return Component.literal("Controller: " + (enabled ? "ENABLED" : "DISABLED"));
    }

    private Component ruleMessage(DungeonRule rule) {
        return Component.literal(
                abbreviate(rule.displayName()) + ": " + decisions.get(rule).name()
        );
    }

    private static String abbreviate(String value) {
        return value.length() <= 13 ? value : value.substring(0, 13);
    }

    private void send(String action) {
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
        int center = width / 2;
        int top = Math.max(12, height / 2 - 105);

        graphics.drawCenteredString(font, title, center, top, 0xFFFFFF);
        graphics.drawString(font, "Preset", center - 150, top + 7, 0xA0A0A0);
        graphics.drawString(font, "Channel", center + 80, top + 7, 0xA0A0A0);
        graphics.drawString(
                font,
                "Instance: " + snapshot.resolvedInstanceId(),
                center - 150,
                top + 47,
                0xA0A0A0
        );
        graphics.drawString(
                font,
                "State: " + snapshot.instanceState(),
                center + 45,
                top + 47,
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
