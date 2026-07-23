package com.scoreboardeditor.gui;

import com.scoreboardeditor.ScoreboardEditorMod;
import com.scoreboardeditor.config.ConfigManager;
import com.scoreboardeditor.config.ModConfig;
import com.scoreboardeditor.gui.components.RuleListWidget;
import com.scoreboardeditor.profile.Profile;
import com.scoreboardeditor.profile.ProfileManager;
import com.scoreboardeditor.scoreboard.LineRule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Main editor screen opened by the configured keybind.
 *
 * <p>Three tabs:
 * <ul>
 *   <li><b>Rules</b>    – add / edit / remove replacement rules for the active profile.</li>
 *   <li><b>Layout</b>   – position, scale, spacing, background, shadow, score visibility.</li>
 *   <li><b>Profiles</b> – create, delete, activate named profiles.</li>
 * </ul>
 *
 * All changes are applied immediately (live preview) and persisted when the
 * screen closes.  The game continues running behind the screen so the
 * scoreboard stays visible as a live preview.
 */
public class ScoreboardEditorScreen extends Screen {

    // -----------------------------------------------------------------------
    // Tab constants
    // -----------------------------------------------------------------------

    private static final int TAB_RULES    = 0;
    private static final int TAB_LAYOUT   = 1;
    private static final int TAB_PROFILES = 2;

    private static final int HEADER_H  = 52;  // space reserved for title + tab bar
    private static final int FOOTER_H  = 30;  // space reserved for Done button
    private static final int TAB_W     = 80;
    private static final int TAB_H     = 20;
    private static final int PAD       = 8;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private int activeTab = TAB_RULES;

    private ConfigManager  configManager;
    private ModConfig      config;
    private ProfileManager profileManager;
    private Profile        activeProfile;

    // Rules tab
    private RuleListWidget ruleList;

    // Profiles tab
    private int selectedProfileIndex = 0;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public ScoreboardEditorScreen() {
        super(Text.literal("Scoreboard Editor"));
    }

    // -----------------------------------------------------------------------
    // Screen lifecycle
    // -----------------------------------------------------------------------

    @Override
    protected void init() {
        ScoreboardEditorMod mod = ScoreboardEditorMod.getInstance();
        configManager  = mod.getConfigManager();
        config         = configManager.getConfig();
        profileManager = mod.getProfileManager();
        activeProfile  = profileManager.getActiveProfile();
        selectedProfileIndex = indexOfActive();
        buildAll();
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer, this.title, width / 2, 8, 0xFFFFFF);

        // Tab underline
        int lineY = HEADER_H - 2;
        ctx.fill(PAD, lineY, width - PAD, lineY + 1, 0x55FFFFFF);

        // Tab-specific extra rendering
        switch (activeTab) {
            case TAB_RULES    -> renderRulesExtra(ctx);
            case TAB_PROFILES -> renderProfilesList(ctx, mx, my);
        }

        super.render(ctx, mx, my, delta);
    }

    @Override
    public void removed() {
        persistAll();
    }

    /** Allow the game to keep running so scoreboard is visible as a live preview. */
    @Override
    public boolean shouldPause() {
        return false;
    }

    // -----------------------------------------------------------------------
    // Full rebuild
    // -----------------------------------------------------------------------

    private void buildAll() {
        clearChildren();
        addTabBar();
        addDoneButton();
        switch (activeTab) {
            case TAB_RULES    -> buildRulesTab();
            case TAB_LAYOUT   -> buildLayoutTab();
            case TAB_PROFILES -> buildProfilesTab();
        }
    }

    private void switchTab(int tab) {
        activeTab = tab;
        ruleList = null;
        buildAll();
    }

    // -----------------------------------------------------------------------
    // Persistent tab bar + Done button
    // -----------------------------------------------------------------------

    private void addTabBar() {
        int total = TAB_W * 3 + 4;
        int sx    = (width - total) / 2;
        int ty    = HEADER_H - TAB_H - 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("Rules"),
                b -> switchTab(TAB_RULES)).dimensions(sx, ty, TAB_W, TAB_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Layout"),
                b -> switchTab(TAB_LAYOUT)).dimensions(sx + TAB_W + 2, ty, TAB_W, TAB_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Profiles"),
                b -> switchTab(TAB_PROFILES)).dimensions(sx + TAB_W * 2 + 4, ty, TAB_W, TAB_H).build());
    }

    private void addDoneButton() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 - 50, height - FOOTER_H + 5, 100, 20).build());
    }

    // -----------------------------------------------------------------------
    // Rules tab
    // -----------------------------------------------------------------------

    private void buildRulesTab() {
        int top     = HEADER_H + 2;
        int bottom  = height - FOOTER_H - 26;
        int listH   = bottom - top;

        ruleList = new RuleListWidget(client, width, listH, top, activeProfile, p -> persistAll());
        // addDrawableChild makes it render automatically via super.render()
        addDrawableChild(ruleList);

        // "Add Rule" button just above the Done bar
        addDrawableChild(ButtonWidget.builder(Text.literal("+ Add Rule"), b -> {
            activeProfile.rules.add(new LineRule("", ""));
            ruleList.refreshEntries();
            persistAll();
        }).dimensions(width / 2 - 60, bottom + 2, 120, 20).build());
    }

    private void renderRulesExtra(DrawContext ctx) {
        // Active profile indicator
        ctx.drawText(textRenderer,
                Text.literal("Profile: ").append(
                        Text.literal(activeProfile.name).formatted(Formatting.YELLOW)),
                PAD, HEADER_H + 2, 0x888888, false);

        // Empty-state hint
        if (activeProfile.rules.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("No rules yet – press \"+ Add Rule\" to start."),
                    width / 2, height / 2, 0x888888);
        }
    }

    // -----------------------------------------------------------------------
    // Layout tab
    // -----------------------------------------------------------------------

    private void buildLayoutTab() {
        int cx   = width / 2 - 100;
        int y    = HEADER_H + 6;
        int step = 26;

        // Mod enabled
        addDrawableChild(ButtonWidget.builder(onOffLabel("Mod Enabled", config.enabled), b -> {
            config.enabled = !config.enabled;
            b.setMessage(onOffLabel("Mod Enabled", config.enabled));
            persistAll();
        }).dimensions(cx, y, 200, 20).build());

        // Offset X
        addDrawableChild(new IntSlider(cx, y + step, 200, 20, "Offset X",
                config.offsetX, -500, 500) {
            @Override void onChanged(int v) { config.offsetX = v; persistAll(); }
        });

        // Offset Y
        addDrawableChild(new IntSlider(cx, y + step * 2, 200, 20, "Offset Y",
                config.offsetY, -500, 500) {
            @Override void onChanged(int v) { config.offsetY = v; persistAll(); }
        });

        // Scale 0.5 – 3.0
        addDrawableChild(new FloatSlider(cx, y + step * 3, 200, 20, "Scale",
                config.scale, 0.5f, 3.0f) {
            @Override void onChanged(float v) { config.scale = v; persistAll(); }
        });

        // Extra line spacing 0 – 20
        addDrawableChild(new IntSlider(cx, y + step * 4, 200, 20, "Line Spacing",
                config.extraLineSpacing, 0, 20) {
            @Override void onChanged(int v) { config.extraLineSpacing = v; persistAll(); }
        });

        // Background opacity 0 – 255
        addDrawableChild(new IntSlider(cx, y + step * 5, 200, 20, "BG Opacity",
                config.backgroundOpacity, 0, 255) {
            @Override void onChanged(int v) { config.backgroundOpacity = v; persistAll(); }
        });

        // Shadow toggle
        addDrawableChild(ButtonWidget.builder(onOffLabel("Shadow", config.textShadow), b -> {
            config.textShadow = !config.textShadow;
            b.setMessage(onOffLabel("Shadow", config.textShadow));
            persistAll();
        }).dimensions(cx, y + step * 6, 97, 20).build());

        // Hide scores toggle
        addDrawableChild(ButtonWidget.builder(onOffLabel("Hide Scores", config.hideScores), b -> {
            config.hideScores = !config.hideScores;
            b.setMessage(onOffLabel("Hide Scores", config.hideScores));
            persistAll();
        }).dimensions(cx + 103, y + step * 6, 97, 20).build());

        // Reset button
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset to Defaults"), b -> resetLayout())
                .dimensions(cx, y + step * 7, 200, 20).build());
    }

    private void resetLayout() {
        config.offsetX           = 0;
        config.offsetY           = 0;
        config.scale             = 1.0f;
        config.extraLineSpacing  = 0;
        config.backgroundOpacity = 60;
        config.textShadow        = true;
        config.hideScores        = false;
        persistAll();
        buildAll(); // rebuild sliders at new positions
    }

    // -----------------------------------------------------------------------
    // Profiles tab
    // -----------------------------------------------------------------------

    private void buildProfilesTab() {
        int rightX = width / 2 + PAD;
        int baseY  = HEADER_H + 4;

        // Activate selected profile
        addDrawableChild(ButtonWidget.builder(Text.literal("Activate Profile"), b -> activateSelected())
                .dimensions(rightX, baseY, 140, 20).build());

        // New profile input + button
        TextFieldWidget nameField = new TextFieldWidget(
                textRenderer, rightX, baseY + 28, 140, 18, Text.empty());
        nameField.setMaxLength(32);
        nameField.setPlaceholder(Text.literal("New profile name…").formatted(Formatting.DARK_GRAY));
        addDrawableChild(nameField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Create Profile"), b -> {
            String n = nameField.getText().trim();
            if (!n.isEmpty() && profileManager.getProfile(n) == null) {
                profileManager.createProfile(n);
                nameField.setText("");
                selectedProfileIndex = profileManager.getProfiles().size() - 1;
                persistAll();
            }
        }).dimensions(rightX, baseY + 50, 140, 20).build());

        // Delete
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Delete Profile").formatted(Formatting.RED), b -> deleteSelected())
                .dimensions(rightX, baseY + 76, 140, 20).build());
    }

    private void renderProfilesList(DrawContext ctx, int mx, int my) {
        List<Profile> profiles = profileManager.getProfiles();
        int listX   = PAD;
        int listY   = HEADER_H + 4;
        int rowH    = 22;
        int listW   = width / 2 - PAD * 2;
        String active = config.activeProfile;

        ctx.drawText(textRenderer,
                Text.literal("Profiles").formatted(Formatting.YELLOW),
                listX, listY - 12, 0xFFFFFF, true);

        for (int i = 0; i < profiles.size(); i++) {
            Profile p   = profiles.get(i);
            int ry      = listY + i * rowH;
            boolean sel = (i == selectedProfileIndex);
            boolean act = p.name.equals(active);

            ctx.fill(listX, ry, listX + listW, ry + rowH - 2,
                    sel ? 0x66FFFFFF : 0x33000000);

            int color = act ? 0xFFFF55 : 0xFFFFFF;
            String label = act ? p.name + " ✓" : p.name;
            ctx.drawText(textRenderer, Text.literal(label), listX + 4, ry + 6, color, false);
        }

        // Active label bottom-right
        ctx.drawText(textRenderer,
                Text.literal("Active: ").append(
                        Text.literal(active).formatted(Formatting.AQUA)),
                width / 2 + PAD, height - FOOTER_H - 14, 0xAAAAAA, false);
    }

    private void activateSelected() {
        List<Profile> profiles = profileManager.getProfiles();
        if (selectedProfileIndex >= 0 && selectedProfileIndex < profiles.size()) {
            Profile p = profiles.get(selectedProfileIndex);
            profileManager.setActiveProfile(p.name);
            activeProfile = p;
            persistAll();
        }
    }

    private void deleteSelected() {
        List<Profile> profiles = profileManager.getProfiles();
        if (profiles.size() <= 1) return;
        if (selectedProfileIndex >= 0 && selectedProfileIndex < profiles.size()) {
            profileManager.deleteProfile(profiles.get(selectedProfileIndex).name);
            selectedProfileIndex = Math.max(0, selectedProfileIndex - 1);
            activeProfile = profileManager.getActiveProfile();
            persistAll();
        }
    }

    private int indexOfActive() {
        List<Profile> profiles = profileManager.getProfiles();
        String active = config.activeProfile;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).name.equals(active)) return i;
        }
        return 0;
    }

    // -----------------------------------------------------------------------
    // Input
    // -----------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Profile list row clicks
        if (activeTab == TAB_PROFILES) {
            int listX  = PAD;
            int listY  = HEADER_H + 4;
            int rowH   = 22;
            int listW  = width / 2 - PAD * 2;
            List<Profile> profiles = profileManager.getProfiles();
            for (int i = 0; i < profiles.size(); i++) {
                int ry = listY + i * rowH;
                if (mx >= listX && mx <= listX + listW && my >= ry && my <= ry + rowH - 2) {
                    selectedProfileIndex = i;
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (ruleList != null && ruleList.isMouseOver(mx, my)) {
            return ruleList.mouseScrolled(mx, my, dx, dy);
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    // -----------------------------------------------------------------------
    // Persistence
    // -----------------------------------------------------------------------

    private void persistAll() {
        configManager.save();
        profileManager.save();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Text onOffLabel(String label, boolean on) {
        return Text.literal(label + ": ").append(
                on ? Text.literal("ON").formatted(Formatting.GREEN)
                   : Text.literal("OFF").formatted(Formatting.RED));
    }

    // -----------------------------------------------------------------------
    // Inline slider implementations
    // -----------------------------------------------------------------------

    /** Integer slider with configurable min/max. */
    private abstract static class IntSlider extends SliderWidget {
        private final String label;
        private final int    min;
        private final int    range;

        IntSlider(int x, int y, int w, int h, String label, int current, int min, int max) {
            super(x, y, w, h, Text.empty(), (double) (current - min) / (max - min));
            this.label = label;
            this.min   = min;
            this.range = max - min;
            updateMessage();
        }

        private int intValue() {
            return min + (int) Math.round(value * range);
        }

        @Override protected void updateMessage() {
            setMessage(Text.literal(label + ": " + intValue()));
        }

        @Override protected void applyValue() { onChanged(intValue()); }

        abstract void onChanged(int v);
    }

    /** Float slider with configurable min/max. */
    private abstract static class FloatSlider extends SliderWidget {
        private final String label;
        private final float  min;
        private final float  range;

        FloatSlider(int x, int y, int w, int h, String label, float current, float min, float max) {
            super(x, y, w, h, Text.empty(), (current - min) / (max - min));
            this.label = label;
            this.min   = min;
            this.range = max - min;
            updateMessage();
        }

        private float floatValue() {
            return min + (float) value * range;
        }

        @Override protected void updateMessage() {
            setMessage(Text.literal(String.format("%s: %.2f×", label, floatValue())));
        }

        @Override protected void applyValue() { onChanged(floatValue()); }

        abstract void onChanged(float v);
    }
}
