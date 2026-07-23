package com.scoreboardeditor.scoreboard;

import com.scoreboardeditor.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Reads the current sidebar scoreboard from the live Minecraft world and
 * produces an immutable {@link ScoreboardState} snapshot.
 *
 * <p>Call {@link #capture()} every frame / tick to stay up to date with live
 * server scoreboards that may update rapidly (e.g. DonutSMP).
 */
public class ScoreboardCapture {

    /** Maximum lines the vanilla sidebar ever displays. */
    private static final int MAX_SIDEBAR_LINES = 15;

    private ScoreboardCapture() {}

    /**
     * Captures the current sidebar scoreboard state.
     * Returns {@link ScoreboardState#empty()} if no sidebar objective is active.
     */
    public static ScoreboardState capture() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return ScoreboardState.empty();
        }

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) {
            return ScoreboardState.empty();
        }

        String title = objective.getDisplayName().getString();

        Collection<ScoreboardEntry> rawEntries = scoreboard.getScoreboardEntries(objective);
        List<ScoreboardEntry> sorted = rawEntries.stream()
                .sorted(Comparator.comparingInt(ScoreboardEntry::value).reversed())
                .limit(MAX_SIDEBAR_LINES)
                .toList();

        List<ScoreboardState.ScoreboardLine> lines = new ArrayList<>(sorted.size());
        for (ScoreboardEntry entry : sorted) {
            String owner = entry.owner();
            int score = entry.value();

            // Resolve full display text including team prefix/suffix
            Text displayText = resolveDisplayText(scoreboard, owner);
            String formatted = displayText.getString();
            String plain = TextUtil.stripFormatting(formatted);

            lines.add(new ScoreboardState.ScoreboardLine(owner, formatted, plain, score));
        }

        return new ScoreboardState(title, lines);
    }

    /**
     * Builds the full display text for a score holder, applying the team's
     * prefix and suffix when the holder is a member of a team.
     */
    private static Text resolveDisplayText(Scoreboard scoreboard, String owner) {
        Team team = scoreboard.getPlayerTeam(owner);
        if (team != null) {
            return Team.decorateName(team, Text.literal(owner));
        }
        return Text.literal(owner);
    }

    /**
     * Returns true when a sidebar objective is currently displayed.
     */
    public static boolean isSidebarActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;
        Scoreboard sb = client.world.getScoreboard();
        return sb.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) != null;
    }
}
