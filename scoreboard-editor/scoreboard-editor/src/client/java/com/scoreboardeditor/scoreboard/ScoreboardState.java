package com.scoreboardeditor.scoreboard;

import java.util.ArrayList;
import java.util.List;

/**
 * An immutable snapshot of the sidebar scoreboard at a single point in time.
 * Captured from the live Minecraft world each render frame.
 */
public class ScoreboardState {

    /** The plain-text display name of the sidebar objective (title row). */
    private final String objectiveTitle;

    /** Ordered list of scoreboard lines (highest score first). */
    private final List<ScoreboardLine> lines;

    public ScoreboardState(String objectiveTitle, List<ScoreboardLine> lines) {
        this.objectiveTitle = objectiveTitle;
        this.lines = List.copyOf(lines);
    }

    public String getObjectiveTitle() {
        return objectiveTitle;
    }

    public List<ScoreboardLine> getLines() {
        return lines;
    }

    /** Returns true when there is no sidebar scoreboard active. */
    public boolean isEmpty() {
        return lines.isEmpty() && objectiveTitle.isEmpty();
    }

    // -----------------------------------------------------------------------
    // Inner value type
    // -----------------------------------------------------------------------

    /**
     * One displayed row in the scoreboard sidebar.
     *
     * <p>{@code ownerRaw} is the string stored as the score holder – on most
     * servers this is a formatted team-prefix+suffix string that encodes the
     * visible text. {@code plainText} is the same string with all §-codes
     * stripped and is used for rule matching.
     */
    public static final class ScoreboardLine {

        /** Raw owner string as stored in the scoreboard (may contain §-codes). */
        private final String ownerRaw;

        /** Fully formatted display text (team prefix + owner + suffix, §-coded). */
        private final String formattedText;

        /** Plain-text version (§-codes stripped) used for rule matching. */
        private final String plainText;

        /** Numeric score value. */
        private final int score;

        public ScoreboardLine(String ownerRaw, String formattedText, String plainText, int score) {
            this.ownerRaw = ownerRaw;
            this.formattedText = formattedText;
            this.plainText = plainText;
            this.score = score;
        }

        public String getOwnerRaw() { return ownerRaw; }
        public String getFormattedText() { return formattedText; }
        public String getPlainText() { return plainText; }
        public int getScore() { return score; }

        @Override
        public String toString() {
            return "ScoreboardLine{plain='" + plainText + "', score=" + score + "}";
        }
    }

    // -----------------------------------------------------------------------
    // Factory helper – empty state
    // -----------------------------------------------------------------------

    public static ScoreboardState empty() {
        return new ScoreboardState("", new ArrayList<>());
    }
}
