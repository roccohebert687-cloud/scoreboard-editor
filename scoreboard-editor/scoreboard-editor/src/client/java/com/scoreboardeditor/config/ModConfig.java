package com.scoreboardeditor.config;

/**
 * Root configuration object serialized to JSON.
 * Holds global settings and the active profile name.
 */
public class ModConfig {

    /** Name of the currently active profile */
    public String activeProfile = "Default";

    /** Whether the mod is enabled globally */
    public boolean enabled = true;

    /** X offset for the scoreboard (added to vanilla position) */
    public int offsetX = 0;

    /** Y offset for the scoreboard (added to vanilla position) */
    public int offsetY = 0;

    /** Scale multiplier for the entire scoreboard (1.0 = normal) */
    public float scale = 1.0f;

    /** Extra pixels between each scoreboard line */
    public int extraLineSpacing = 0;

    /** Background opacity 0–255; vanilla default is around 60 */
    public int backgroundOpacity = 60;

    /** Whether to render text shadow */
    public boolean textShadow = true;

    /** Whether to hide the numeric scores on the right side */
    public boolean hideScores = false;

    /** Whether to completely replace vanilla rendering or layer on top */
    public boolean replaceVanillaRendering = true;
}
