package com.scoreboardeditor.hud;

import com.scoreboardeditor.ScoreboardEditorMod;
import com.scoreboardeditor.config.ModConfig;
import com.scoreboardeditor.manager.RuleManager;
import com.scoreboardeditor.profile.Profile;
import com.scoreboardeditor.profile.ProfileManager;
import com.scoreboardeditor.render.ScoreboardLayout;
import com.scoreboardeditor.scoreboard.ScoreboardCapture;
import com.scoreboardeditor.scoreboard.ScoreboardState;
import com.scoreboardeditor.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Draws a fully customised scoreboard sidebar.
 *
 * <p>Called directly from {@link com.scoreboardeditor.mixin.InGameHudMixin}
 * every time the vanilla scoreboard would normally be rendered; the mixin
 * cancels the vanilla call and delegates here instead.
 *
 * <p>Each frame the renderer:
 * <ol>
 *   <li>Captures the live scoreboard via {@link ScoreboardCapture}.</li>
 *   <li>Applies the active profile's rules via {@link RuleManager}.</li>
 *   <li>Draws the result with the layout from {@link ScoreboardLayout}.</li>
 * </ol>
 */
public class CustomScoreboardRenderer {

    /** Vanilla scoreboard font height per line. */
    private static final int LINE_HEIGHT = 9;

    /** Pixels between the right screen edge and the scoreboard background. */
    private static final int RIGHT_MARGIN = 3;

    /** Maximum sidebar lines (vanilla cap). */
    private static final int MAX_LINES = 15;

    // Background colours (ARGB)
    private static final int ENTRY_BG_ALPHA = 0x4C; // ~30 % black
    private static final int TITLE_BG_ALPHA = 0x66; // ~40 % black

    // Score colour matches vanilla (dark red)
    private static final int SCORE_COLOR = 0xFF5555;

    private final ProfileManager profileManager;

    public CustomScoreboardRenderer(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    /**
     * Called once during mod init.  Nothing to register here — the mixin
     * drives rendering; this object is held as a reference by the mod main class.
     */
    public void register() {
        // Intentionally empty: the mixin calls renderFromMixin() directly.
    }

    // -----------------------------------------------------------------------
    // Static entry-point called by InGameHudMixin
    // -----------------------------------------------------------------------

    /**
     * Renders a custom sidebar scoreboard using the live scoreboard data and
     * the active profile's rules.  Called from the mixin in place of the
     * vanilla method.
     *
     * @param context the current draw context (never null)
     */
    public static void renderFromMixin(DrawContext context) {
        ScoreboardEditorMod mod = ScoreboardEditorMod.getInstance();
        if (mod == null) return;

        ModConfig config = mod.getConfigManager().getConfig();
        if (!config.enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.textRenderer == null) return;

        // Capture live scoreboard
        ScoreboardState state = ScoreboardCapture.capture();
        if (state.isEmpty()) return;

        // Resolve active profile + layout
        Profile profile = mod.getProfileManager().getActiveProfile();
        ScoreboardLayout layout = ScoreboardLayout.resolve(config, profile);

        // Apply replacement rules
        List<RuleManager.ResolvedLine> resolved = RuleManager.apply(state, profile);

        // Draw
        renderSidebar(
                context,
                client.textRenderer,
                state.getObjectiveTitle(),
                resolved,
                layout,
                client.getWindow().getScaledWidth(),
                client.getWindow().getScaledHeight()
        );
    }

    // -----------------------------------------------------------------------
    // Core rendering
    // -----------------------------------------------------------------------

    /**
     * Renders the customised scoreboard sidebar, mirroring vanilla positioning.
     *
     * <p>Vanilla positions the scoreboard on the right edge of the screen,
     * vertically centred.  We replicate that calculation and apply the
     * profile's offset + scale on top.
     */
    private static void renderSidebar(
            DrawContext context,
            TextRenderer tr,
            String title,
            List<RuleManager.ResolvedLine> lines,
            ScoreboardLayout layout,
            int screenW,
            int screenH) {

        int lineCount = Math.min(lines.size(), MAX_LINES);
        if (lineCount == 0 && title.isEmpty()) return;

        List<RuleManager.ResolvedLine> visible = lines.subList(0, lineCount);

        // ---- Measure content width -----------------------------------------
        int contentWidth = tr.getWidth(title);
        for (RuleManager.ResolvedLine line : visible) {
            int w = tr.getWidth(Text.literal(line.getDisplayText()));
            if (!layout.hideScores) {
                w += 6 + tr.getWidth(line.getScoreText()); // vanilla: 6 px gap
            }
            if (w > contentWidth) contentWidth = w;
        }
        contentWidth = Math.max(contentWidth, 20); // at least 20 px wide

        // ---- Compute position (vanilla formula) ----------------------------
        int lineH       = LINE_HEIGHT + layout.extraLineSpacing;
        int totalHeight = (lineCount + 1) * lineH; // lines + title

        // Vanilla Y: centred around screen midpoint
        int baseY = screenH / 2 + totalHeight / 3 + layout.offsetY;
        int baseX = screenW - contentWidth - RIGHT_MARGIN + layout.offsetX;

        // ---- Apply scale ---------------------------------------------------
        boolean scaled = layout.scale != 1.0f;
        if (scaled) {
            context.getMatrices().push();
            // Scale around the scoreboard's top-left origin
            float anchorX = baseX;
            float anchorY = baseY - totalHeight;
            context.getMatrices().translate(anchorX, anchorY, 0f);
            context.getMatrices().scale(layout.scale, layout.scale, 1f);
            context.getMatrices().translate(-anchorX, -anchorY, 0f);
        }

        // ---- Render entry rows (bottom → top) ------------------------------
        int entryBg = ColorUtil.withAlpha(0x000000,
                (int) (ColorUtil.clampByte(layout.backgroundOpacity) * 0.75f));
        int titleBg = ColorUtil.withAlpha(0x000000,
                ColorUtil.clampByte(layout.backgroundOpacity));

        for (int i = 0; i < lineCount; i++) {
            RuleManager.ResolvedLine line = visible.get(lineCount - 1 - i); // highest score first
            int y = baseY - (i + 1) * lineH;

            // Background strip
            context.fill(baseX - 2, y - 1, baseX + contentWidth + 2, y + LINE_HEIGHT, entryBg);

            // Display text (honours §-colour codes via Text.literal)
            context.drawText(tr,
                    Text.literal(line.getDisplayText()),
                    baseX, y,
                    0xFFFFFF,
                    layout.textShadow);

            // Score value on the right
            if (!layout.hideScores) {
                String scoreStr = line.getScoreText();
                int scoreX = baseX + contentWidth - tr.getWidth(scoreStr);
                context.drawText(tr, scoreStr, scoreX, y, SCORE_COLOR, layout.textShadow);
            }
        }

        // ---- Render title bar ---------------------------------------------
        int titleY = baseY - totalHeight;
        context.fill(baseX - 2, titleY - 1, baseX + contentWidth + 2, titleY + LINE_HEIGHT, titleBg);

        int titleX = baseX + (contentWidth - tr.getWidth(title)) / 2;
        context.drawText(tr, title, titleX, titleY, 0xFFFFFF, layout.textShadow);

        if (scaled) {
            context.getMatrices().pop();
        }
    }
}
