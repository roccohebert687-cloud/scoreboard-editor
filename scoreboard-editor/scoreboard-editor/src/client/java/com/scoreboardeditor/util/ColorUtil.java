package com.scoreboardeditor.util;

/**
 * Helpers for Minecraft colour codes and ARGB integer colours.
 */
public final class ColorUtil {

    private ColorUtil() {}

    /** Returns the ARGB int for a given Minecraft colour-code character ('0'–'f'). */
    public static int colorCodeToArgb(char code, int alpha) {
        int rgb = switch (Character.toLowerCase(code)) {
            case '0' -> 0x000000;
            case '1' -> 0x0000AA;
            case '2' -> 0x00AA00;
            case '3' -> 0x00AAAA;
            case '4' -> 0xAA0000;
            case '5' -> 0xAA00AA;
            case '6' -> 0xFFAA00;
            case '7' -> 0xAAAAAA;
            case '8' -> 0x555555;
            case '9' -> 0x5555FF;
            case 'a' -> 0x55FF55;
            case 'b' -> 0x55FFFF;
            case 'c' -> 0xFF5555;
            case 'd' -> 0xFF55FF;
            case 'e' -> 0xFFFF55;
            case 'f' -> 0xFFFFFF;
            default  -> 0xFFFFFF;
        };
        return (alpha << 24) | rgb;
    }

    /** Packs RGBA components into an ARGB integer. */
    public static int toArgb(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Returns only the alpha component (0–255) of an ARGB integer. */
    public static int alpha(int argb) {
        return (argb >> 24) & 0xFF;
    }

    /** Clamps an integer to [0, 255]. */
    public static int clampByte(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /** Blends the given opaque colour with a background opacity (0–255). */
    public static int withAlpha(int rgb, int alpha) {
        return (clampByte(alpha) << 24) | (rgb & 0x00FFFFFF);
    }

    /** Standard Minecraft scoreboard background colour (~30 % black). */
    public static int scoreboardBackground(int opacity) {
        return withAlpha(0x000000, clampByte(opacity));
    }
}
