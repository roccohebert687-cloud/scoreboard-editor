package com.scoreboardeditor.mixin;

import com.scoreboardeditor.ScoreboardEditorMod;
import com.scoreboardeditor.config.ModConfig;
import com.scoreboardeditor.hud.CustomScoreboardRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts the vanilla sidebar scoreboard rendering in {@link InGameHud}.
 *
 * <p>When the Scoreboard Editor mod is enabled, the vanilla rendering is
 * cancelled and our custom {@link CustomScoreboardRenderer} draws the
 * modified scoreboard instead.  When the mod is disabled the vanilla
 * rendering continues unchanged.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    /**
     * Injected at the very start of {@code renderScoreboardSidebar}.
     *
     * <p>If the mod is globally enabled we cancel the vanilla method and
     * delegate to {@link CustomScoreboardRenderer#renderFromMixin(DrawContext)}.
     * This keeps the rendering in the same call-site as vanilla so z-ordering
     * and HUD element stacking remain correct.
     */
    @Inject(
            method = "renderScoreboardSidebar",
            at = @At("HEAD"),
            cancellable = true
    )
    private void scoreboardEditor$renderScoreboardSidebar(
            DrawContext context,
            ScoreboardObjective objective,
            CallbackInfo ci) {

        ScoreboardEditorMod mod = ScoreboardEditorMod.getInstance();
        if (mod == null) return; // Safety: mod not yet initialized

        ModConfig config = mod.getConfigManager().getConfig();
        if (!config.enabled) return; // Mod disabled – let vanilla run

        // Cancel vanilla rendering and render our custom version
        ci.cancel();
        CustomScoreboardRenderer.renderFromMixin(context);
    }
}
