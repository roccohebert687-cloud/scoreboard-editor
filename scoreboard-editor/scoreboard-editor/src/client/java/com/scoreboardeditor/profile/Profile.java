package com.scoreboardeditor.profile;

import com.scoreboardeditor.scoreboard.LineRule;

import java.util.ArrayList;
import java.util.List;

/**
 * A named profile that stores a set of scoreboard replacement rules together
 * with layout/visual overrides.  Profiles are serialized to JSON by Gson.
 */
public class Profile {

    /** Human-readable name shown in the UI. */
    public String name;

    /** Ordered list of replacement rules.  Rules are evaluated top-to-bottom;
     *  the first matching rule wins. */
    public List<LineRule> rules = new ArrayList<>();

    // ------------------------------------------------------------------
    // Layout overrides (all nullable – null means "use global config value")
    // ------------------------------------------------------------------

    /** X-offset added to the vanilla scoreboard X position. */
    public Integer offsetX = null;

    /** Y-offset added to the vanilla scoreboard Y position. */
    public Integer offsetY = null;

    /** Scale multiplier (1.0 = normal). */
    public Float scale = null;

    /** Extra pixels between lines. */
    public Integer extraLineSpacing = null;

    /** Background rectangle opacity 0-255. */
    public Integer backgroundOpacity = null;

    /** Whether to render text shadow. */
    public Boolean textShadow = null;

    /** Whether to hide the numeric score column. */
    public Boolean hideScores = null;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public Profile() {
        this.name = "New Profile";
    }

    public Profile(String name) {
        this.name = name;
    }

    // ------------------------------------------------------------------
    // Rule helpers
    // ------------------------------------------------------------------

    /** Appends a new rule to the end of the list and returns it. */
    public LineRule addRule(String pattern, String replacement) {
        LineRule rule = new LineRule(pattern, replacement);
        rules.add(rule);
        return rule;
    }

    /** Removes the rule at the given index (no-op if out of range). */
    public void removeRule(int index) {
        if (index >= 0 && index < rules.size()) {
            rules.remove(index);
        }
    }

    /** Moves a rule up by one position (no-op at top). */
    public void moveRuleUp(int index) {
        if (index > 0 && index < rules.size()) {
            LineRule r = rules.remove(index);
            rules.add(index - 1, r);
        }
    }

    /** Moves a rule down by one position (no-op at bottom). */
    public void moveRuleDown(int index) {
        if (index >= 0 && index < rules.size() - 1) {
            LineRule r = rules.remove(index);
            rules.add(index + 1, r);
        }
    }

    // ------------------------------------------------------------------
    // Factory helpers for bundled presets
    // ------------------------------------------------------------------

    /** Creates the built-in DonutSMP preset profile. */
    public static Profile createDonutSmpPreset() {
        Profile p = new Profile("DonutSMP");
        // Balance line – replace raw number with human-readable suffix
        p.addRule("balance: *", "Balance: &a$%s");
        // Common stat lines
        p.addRule("kills: *", "Kills: &c%s");
        p.addRule("deaths: *", "Deaths: &7%s");
        p.addRule("kdr: *", "KDR: &e%s");
        return p;
    }

    /** Creates the built-in Hypixel preset profile. */
    public static Profile createHypixelPreset() {
        Profile p = new Profile("Hypixel");
        p.addRule("www.hypixel.net", "&bhypixel.net");
        return p;
    }

    /** Creates the default profile. */
    public static Profile createDefault() {
        return new Profile("Default");
    }

    @Override
    public String toString() {
        return "Profile{name='" + name + "', rules=" + rules.size() + "}";
    }
}
