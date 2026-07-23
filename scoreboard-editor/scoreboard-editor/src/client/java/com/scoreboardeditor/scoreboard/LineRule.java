package com.scoreboardeditor.scoreboard;

import com.scoreboardeditor.util.TextUtil;

/**
 * A single replacement rule applied to one scoreboard line.
 *
 * <p>The {@code matchPattern} is compared against the stripped (plain-text)
 * version of the scoreboard line owner string. A {@code *} anywhere in the
 * pattern acts as a wildcard that matches any sequence of characters.
 *
 * <p>If {@code replacementText} is {@code null} or empty the line is hidden.
 * If {@code scoreOverride} is {@code null} the original numeric score is kept.
 */
public class LineRule {

    /** Pattern to match against a scoreboard line (plain text, * = wildcard). */
    public String matchPattern = "";

    /**
     * Replacement text for the line owner/display text.
     * Supports {@code &} colour/format codes (e.g. {@code &a}, {@code &l}).
     * Set to empty string to hide the line entirely.
     */
    public String replacementText = "";

    /**
     * Optional override for the numeric score shown on the right.
     * {@code null} or empty keeps the original score.
     */
    public String scoreOverride = null;

    /** Whether this rule is currently active. */
    public boolean enabled = true;

    public LineRule() {}

    public LineRule(String matchPattern, String replacementText) {
        this.matchPattern = matchPattern;
        this.replacementText = replacementText;
    }

    // -----------------------------------------------------------------------
    // Matching
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} when this rule matches the given plain-text line.
     * Comparison is case-insensitive. A {@code *} in the pattern matches any
     * substring (including empty).
     */
    public boolean matches(String plainLine) {
        if (!enabled || matchPattern == null || matchPattern.isEmpty()) return false;
        return wildcardMatch(matchPattern.toLowerCase(), plainLine.toLowerCase());
    }

    private static boolean wildcardMatch(String pattern, String text) {
        // Split on * and match greedily
        String[] parts = pattern.split("\\*", -1);
        if (parts.length == 1) {
            // No wildcard – exact match
            return pattern.equals(text);
        }
        int pos = 0;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0) {
                // Must match at the start
                if (!text.startsWith(part)) return false;
                pos = part.length();
            } else if (i == parts.length - 1) {
                // Must match at the end
                if (!text.endsWith(part)) return false;
            } else {
                int idx = text.indexOf(part, pos);
                if (idx < 0) return false;
                pos = idx + part.length();
            }
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /** Returns true when this rule hides the line (empty replacement). */
    public boolean isHideLine() {
        return replacementText == null || replacementText.isEmpty();
    }

    /**
     * Returns the formatted replacement text with {@code &} codes resolved
     * to section-sign ({@code §}) codes.
     */
    public String getFormattedReplacement() {
        return TextUtil.ampersandToSection(replacementText);
    }

    /** Returns true when a numeric score override is set. */
    public boolean hasScoreOverride() {
        return scoreOverride != null && !scoreOverride.isBlank();
    }

    @Override
    public String toString() {
        return "LineRule{match='" + matchPattern + "', replace='" + replacementText + "', score=" + scoreOverride + "}";
    }
}
