package com.scoreboardeditor.manager;

import com.scoreboardeditor.profile.Profile;
import com.scoreboardeditor.scoreboard.LineRule;
import com.scoreboardeditor.scoreboard.ScoreboardState;
import com.scoreboardeditor.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies the active {@link Profile}'s {@link LineRule}s to a raw
 * {@link ScoreboardState} and produces a list of {@link ResolvedLine}s
 * ready for rendering.
 *
 * <p>Rules are evaluated top-to-bottom; the first matching rule wins.
 * A line with an empty replacement is hidden entirely from the output.
 */
public class RuleManager {

    private RuleManager() {}

    /**
     * Applies rules from {@code profile} to every line in {@code state}.
     *
     * @param state   the raw scoreboard snapshot captured this frame
     * @param profile the active profile supplying rules + layout overrides
     * @return ordered list of resolved lines ready to be rendered (hidden lines excluded)
     */
    public static List<ResolvedLine> apply(ScoreboardState state, Profile profile) {
        List<ResolvedLine> result = new ArrayList<>();

        for (ScoreboardState.ScoreboardLine line : state.getLines()) {
            ResolvedLine resolved = applyRules(line, profile.rules);
            if (resolved != null) {
                result.add(resolved);
            }
        }

        return result;
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private static ResolvedLine applyRules(ScoreboardState.ScoreboardLine line, List<LineRule> rules) {
        String plain = line.getPlainText();

        for (LineRule rule : rules) {
            if (!rule.enabled) continue;
            if (rule.matches(plain)) {
                // Matched: apply this rule
                if (rule.isHideLine()) {
                    return null; // hide
                }
                String displayText = rule.getFormattedReplacement();
                String scoreText = rule.hasScoreOverride()
                        ? rule.scoreOverride
                        : String.valueOf(line.getScore());
                return new ResolvedLine(displayText, scoreText, line.getScore());
            }
        }

        // No rule matched – use the original line
        return new ResolvedLine(
                line.getFormattedText(),
                String.valueOf(line.getScore()),
                line.getScore()
        );
    }

    // ------------------------------------------------------------------
    // Value type: a single resolved scoreboard line ready for rendering
    // ------------------------------------------------------------------

    /**
     * A single scoreboard line after all rules have been applied.
     */
    public static final class ResolvedLine {

        /** Formatted display text (§-coded). */
        private final String displayText;

        /** Score string to render on the right (may be a custom override). */
        private final String scoreText;

        /** Original numeric score (used for sorting, not display). */
        private final int originalScore;

        public ResolvedLine(String displayText, String scoreText, int originalScore) {
            this.displayText = displayText;
            this.scoreText = scoreText;
            this.originalScore = originalScore;
        }

        public String getDisplayText() { return displayText; }
        public String getScoreText() { return scoreText; }
        public int getOriginalScore() { return originalScore; }

        /** Returns the plain-text display text (§-codes stripped). */
        public String getPlainDisplay() {
            return TextUtil.stripFormatting(displayText);
        }
    }
}
