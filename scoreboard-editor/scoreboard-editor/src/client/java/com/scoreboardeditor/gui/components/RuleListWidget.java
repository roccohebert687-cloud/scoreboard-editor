package com.scoreboardeditor.gui.components;

import com.scoreboardeditor.profile.Profile;
import com.scoreboardeditor.scoreboard.LineRule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;

import java.util.function.Consumer;

/**
 * Scrollable list of {@link LineEditorEntry} widgets, one per
 * {@link LineRule} in the active {@link Profile}.
 *
 * <p>Each entry exposes text fields and controls for the pattern,
 * replacement text, and score override of the corresponding rule.
 */
public class RuleListWidget extends EntryListWidget<LineEditorEntry> {

    /** Height in pixels of a single list entry. */
    public static final int ENTRY_HEIGHT = 72;

    private Profile profile;
    private final Consumer<Profile> onChanged;

    public RuleListWidget(
            MinecraftClient client,
            int width,
            int height,
            int top,
            Profile profile,
            Consumer<Profile> onChanged) {
        super(client, width, height, top, ENTRY_HEIGHT);
        this.profile   = profile;
        this.onChanged = onChanged;
        refreshEntries();
    }

    // -----------------------------------------------------------------------
    // Entry management
    // -----------------------------------------------------------------------

    /**
     * Rebuilds the entry list from the profile's current rule list.
     * Call whenever a rule is added or deleted.
     */
    public void refreshEntries() {
        clearEntries();
        for (int i = 0; i < profile.rules.size(); i++) {
            addEntry(new LineEditorEntry(this, profile, i, client));
        }
    }

    /** Called by an entry when it wants to delete its own rule. */
    void deleteRule(int ruleIndex) {
        profile.removeRule(ruleIndex);
        onChanged.accept(profile);
        refreshEntries();
    }

    /** Called by an entry when it wants to move its rule up. */
    void moveRuleUp(int ruleIndex) {
        profile.moveRuleUp(ruleIndex);
        onChanged.accept(profile);
        refreshEntries();
    }

    /** Called by an entry when it wants to move its rule down. */
    void moveRuleDown(int ruleIndex) {
        profile.moveRuleDown(ruleIndex);
        onChanged.accept(profile);
        refreshEntries();
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        refreshEntries();
    }

    // -----------------------------------------------------------------------
    // EntryListWidget overrides
    // -----------------------------------------------------------------------

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    // Input must be explicitly forwarded so text fields inside entries work.

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers);
    }
}
