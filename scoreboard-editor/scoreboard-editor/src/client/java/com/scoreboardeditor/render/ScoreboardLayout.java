package com.scoreboardeditor.render;

import com.scoreboardeditor.config.ModConfig;
import com.scoreboardeditor.profile.Profile;

/**
 * Resolved layout parameters for one render frame, merging the global
 * {@link ModConfig} defaults with any per-profile overrides.
 */
public class ScoreboardLayout {

    public final int offsetX;
    public final int offsetY;
    public final float scale;
    public final int extraLineSpacing;
    public final int backgroundOpacity;
    public final boolean textShadow;
    public final boolean hideScores;

    private ScoreboardLayout(Builder b) {
        this.offsetX = b.offsetX;
        this.offsetY = b.offsetY;
        this.scale = b.scale;
        this.extraLineSpacing = b.extraLineSpacing;
        this.backgroundOpacity = b.backgroundOpacity;
        this.textShadow = b.textShadow;
        this.hideScores = b.hideScores;
    }

    /**
     * Resolves layout by applying profile overrides on top of global config.
     */
    public static ScoreboardLayout resolve(ModConfig config, Profile profile) {
        return new Builder()
                .offsetX(profile.offsetX != null ? profile.offsetX : config.offsetX)
                .offsetY(profile.offsetY != null ? profile.offsetY : config.offsetY)
                .scale(profile.scale != null ? profile.scale : config.scale)
                .extraLineSpacing(profile.extraLineSpacing != null ? profile.extraLineSpacing : config.extraLineSpacing)
                .backgroundOpacity(profile.backgroundOpacity != null ? profile.backgroundOpacity : config.backgroundOpacity)
                .textShadow(profile.textShadow != null ? profile.textShadow : config.textShadow)
                .hideScores(profile.hideScores != null ? profile.hideScores : config.hideScores)
                .build();
    }

    // -----------------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------------

    private static final class Builder {
        int offsetX = 0;
        int offsetY = 0;
        float scale = 1.0f;
        int extraLineSpacing = 0;
        int backgroundOpacity = 60;
        boolean textShadow = true;
        boolean hideScores = false;

        Builder offsetX(int v)             { this.offsetX = v; return this; }
        Builder offsetY(int v)             { this.offsetY = v; return this; }
        Builder scale(float v)             { this.scale = v; return this; }
        Builder extraLineSpacing(int v)    { this.extraLineSpacing = v; return this; }
        Builder backgroundOpacity(int v)   { this.backgroundOpacity = v; return this; }
        Builder textShadow(boolean v)      { this.textShadow = v; return this; }
        Builder hideScores(boolean v)      { this.hideScores = v; return this; }
        ScoreboardLayout build()           { return new ScoreboardLayout(this); }
    }
}
