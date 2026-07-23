package com.scoreboardeditor.gui.components;

import com.scoreboardeditor.profile.Profile;
import com.scoreboardeditor.scoreboard.LineRule;
import com.scoreboardeditor.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * A single row inside the {@link RuleListWidget} that lets the user edit
 * one {@link LineRule}: match pattern, replacement text, score override,
 * enabled toggle, delete, and move-up / move-down controls.
 */
public class LineEditorEntry extends net.minecraft.client.gui.widget.EntryListWidget.Entry<LineEditorEntry> {

    // Layout constants
    private static final int PADDING        = 4;
    private static final int LABEL_W        = 52;
    private static final int FIELD_H        = 14;
    private static final int BUTTON_W       = 18;
    private static final int BUTTON_H       = 14;
    private static final int SCORE_FIELD_W  = 60;

    private final RuleListWidget parent;
    private final Profile profile;
    private final int ruleIndex;
    private final MinecraftClient client;

    // Widgets
    private final TextFieldWidget matchField;
    private final TextFieldWidget replaceField;
    private final TextFieldWidget scoreField;
    private final ButtonWidget enabledBtn;
    private final ButtonWidget deleteBtn;
    private final ButtonWidget upBtn;
    private final ButtonWidget downBtn;

    public LineEditorEntry(RuleListWidget parent, Profile profile, int ruleIndex, MinecraftClient client) {
        this.parent = parent;
        this.profile = profile;
        this.ruleIndex = ruleIndex;
        this.client = client;

        LineRule rule = getRule();

        // -- Match field -------------------------------------------------------
        matchField = new TextFieldWidget(client.textRenderer, 0, 0, 100, FIELD_H, Text.empty());
        matchField.setMaxLength(256);
        matchField.setText(rule.matchPattern);
        matchField.setChangedListener(text -> {
            getRule().matchPattern = text;
        });
        matchField.setPlaceholder(Text.literal("Pattern (text or * wildcard)").formatted(net.minecraft.util.Formatting.DARK_GRAY));

        // -- Replace field -----------------------------------------------------
        replaceField = new TextFieldWidget(client.textRenderer, 0, 0, 100, FIELD_H, Text.empty());
        replaceField.setMaxLength(256);
        replaceField.setText(TextUtil.sectionToAmpersand(rule.replacementText));
        replaceField.setChangedListener(text -> {
            getRule().replacementText = TextUtil.ampersandToSection(text);
        });
        replaceField.setPlaceholder(Text.literal("Replacement (&a codes) or blank to hide").formatted(net.minecraft.util.Formatting.DARK_GRAY));

        // -- Score override field ----------------------------------------------
        scoreField = new TextFieldWidget(client.textRenderer, 0, 0, SCORE_FIELD_W, FIELD_H, Text.empty());
        scoreField.setMaxLength(32);
        scoreField.setText(rule.scoreOverride != null ? rule.scoreOverride : "");
        scoreField.setChangedListener(text -> {
            getRule().scoreOverride = text.isBlank() ? null : text;
        });
        scoreField.setPlaceholder(Text.literal("Score").formatted(net.minecraft.util.Formatting.DARK_GRAY));

        // -- Enabled toggle ---------------------------------------------------
        enabledBtn = ButtonWidget.builder(enabledText(rule.enabled), btn -> {
            LineRule r = getRule();
            r.enabled = !r.enabled;
            btn.setMessage(enabledText(r.enabled));
        }).dimensions(0, 0, 44, BUTTON_H).build();

        // -- Delete button -----------------------------------------------------
        deleteBtn = ButtonWidget.builder(Text.literal("✗"), btn -> parent.deleteRule(ruleIndex))
                .dimensions(0, 0, BUTTON_W, BUTTON_H).build();

        // -- Move up button ----------------------------------------------------
        upBtn = ButtonWidget.builder(Text.literal("▲"), btn -> parent.moveRuleUp(ruleIndex))
                .dimensions(0, 0, BUTTON_W, BUTTON_H).build();

        // -- Move down button --------------------------------------------------
        downBtn = ButtonWidget.builder(Text.literal("▼"), btn -> parent.moveRuleDown(ruleIndex))
                .dimensions(0, 0, BUTTON_W, BUTTON_H).build();
    }

    // -----------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float tickDelta) {

        int availW   = entryWidth - PADDING * 2;
        // Score field + move buttons + enable + delete consume the right side
        int rightW   = SCORE_FIELD_W + BUTTON_W * 3 + PADDING * 4 + 44;
        int fieldW   = (availW - LABEL_W - rightW - PADDING * 2) / 2;

        int cx       = x + PADDING;
        int row1     = y + PADDING;
        int row2     = y + PADDING + FIELD_H + PADDING;
        int row3     = y + PADDING + (FIELD_H + PADDING) * 2;

        // ---- Row 1: Match label + match field -------------------------------
        context.drawText(client.textRenderer, Text.literal("Match:"), cx, row1 + 2, 0xAAAAAA, false);
        matchField.setPosition(cx + LABEL_W, row1);
        matchField.setWidth(availW - LABEL_W);
        matchField.render(context, mouseX, mouseY, tickDelta);

        // ---- Row 2: Replace label + replace field ---------------------------
        context.drawText(client.textRenderer, Text.literal("Replace:"), cx, row2 + 2, 0xAAAAAA, false);
        replaceField.setPosition(cx + LABEL_W, row2);
        replaceField.setWidth(availW - LABEL_W - SCORE_FIELD_W - PADDING * 3 - BUTTON_W * 3 - 44);
        replaceField.render(context, mouseX, mouseY, tickDelta);

        // Score field (right of replace field)
        int scoreX = cx + LABEL_W + replaceField.getWidth() + PADDING;
        scoreField.setPosition(scoreX, row2);
        scoreField.setWidth(SCORE_FIELD_W);
        scoreField.render(context, mouseX, mouseY, tickDelta);

        // ---- Row 3: Enabled + ordering + delete buttons ---------------------
        context.drawText(client.textRenderer, Text.literal("Score override:").formatted(net.minecraft.util.Formatting.DARK_GRAY),
                cx, row3 + 2, 0x888888, false);

        int btnX = x + entryWidth - PADDING - BUTTON_W * 3 - PADDING * 2 - 44;
        enabledBtn.setPosition(btnX, row3);
        enabledBtn.render(context, mouseX, mouseY, tickDelta);

        upBtn.setPosition(btnX + 44 + PADDING, row3);
        upBtn.render(context, mouseX, mouseY, tickDelta);

        downBtn.setPosition(btnX + 44 + PADDING + BUTTON_W + PADDING, row3);
        downBtn.render(context, mouseX, mouseY, tickDelta);

        deleteBtn.setPosition(btnX + 44 + PADDING + BUTTON_W * 2 + PADDING * 2, row3);
        deleteBtn.render(context, mouseX, mouseY, tickDelta);

        // ---- Separator line -------------------------------------------------
        context.fill(x, y + entryHeight - 1, x + entryWidth, y + entryHeight, 0x33FFFFFF);
    }

    // -----------------------------------------------------------------------
    // Input forwarding
    // -----------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return matchField.mouseClicked(mouseX, mouseY, button)
                || replaceField.mouseClicked(mouseX, mouseY, button)
                || scoreField.mouseClicked(mouseX, mouseY, button)
                || enabledBtn.mouseClicked(mouseX, mouseY, button)
                || deleteBtn.mouseClicked(mouseX, mouseY, button)
                || upBtn.mouseClicked(mouseX, mouseY, button)
                || downBtn.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return matchField.keyPressed(keyCode, scanCode, modifiers)
                || replaceField.keyPressed(keyCode, scanCode, modifiers)
                || scoreField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return matchField.charTyped(chr, modifiers)
                || replaceField.charTyped(chr, modifiers)
                || scoreField.charTyped(chr, modifiers);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private LineRule getRule() {
        return profile.rules.get(ruleIndex);
    }

    private static Text enabledText(boolean enabled) {
        return enabled
                ? Text.literal("ON").formatted(net.minecraft.util.Formatting.GREEN)
                : Text.literal("OFF").formatted(net.minecraft.util.Formatting.RED);
    }
}
