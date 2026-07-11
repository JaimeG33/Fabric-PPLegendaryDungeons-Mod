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
        int top = Math.max(10, height / 2 - 135);

        presetBox = new EditBox(
                font,
                center - 210,
                top + 30,
                320,
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
                center + 120,
                top + 30,
                90,
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
                }).bounds(center - 210, top + 56, 210, 20).build()
        );

        int startY = top + 102;
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

    private Component ruleMessage(DungeonRule rule) {
        return Component.literal(
                abbreviate(rule.displayName()) + ": " + decisions.get(rule).name()
        );
    }

    private static String abbreviate(String value) {
        return value.length() <= 16 ? value : value.substring(0, 16);
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
        int top = Math.max(10, height / 2 - 135);

        graphics.drawCenteredString(font, title, center, top, 0xFFFFFF);
        graphics.drawString(font, "Preset ID", center - 210, top + 19, 0xA0A0A0);
        graphics.drawString(font, "Link channel", center + 120, top + 19, 0xA0A0A0);

        graphics.drawString(
                font,
                "Instance: " + snapshot.resolvedInstanceId(),
                center - 210,
                top + 80,
                0xA0A0A0
        );

        graphics.drawString(
                font,
                "State: " + snapshot.instanceState(),
                center + 60,
                top + 80,
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
