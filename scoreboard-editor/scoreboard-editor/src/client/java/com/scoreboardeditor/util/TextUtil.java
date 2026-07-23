package com.scoreboardeditor.util;

import java.util.regex.Pattern;

/**
 * Utility methods for Minecraft text / formatting-code manipulation.
 */
public final class TextUtil {

    private static final Pattern SECTION_PATTERN = Pattern.compile("§[0-9a-fk-or]", Pattern.CASE_INSENSITIVE);
    private static final char SECTION = '§';
    private static final char AMPERSAND = '&';

    private TextUtil() {}

    /**
     * Strips all Minecraft §-colour/format codes from the string.
     */
    public static String stripFormatting(String text) {
        if (text == null) return "";
        return SECTION_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * Converts {@code &}-prefixed colour/format codes to §-codes.
     * Unrecognised codes are left unchanged.
     */
    public static String ampersandToSection(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == AMPERSAND && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (isValidCode(next)) {
                    sb.append(SECTION);
                    sb.append(next);
                    i++; // skip next char
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Converts §-codes back to &-codes for display in text fields.
     */
    public static String sectionToAmpersand(String text) {
        if (text == null) return "";
        return text.replace(SECTION, AMPERSAND);
    }

    private static boolean isValidCode(char c) {
        return (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'f')
                || (c >= 'k' && c <= 'o')
                || c == 'r'
                || (c >= 'A' && c <= 'F')
                || (c >= 'K' && c <= 'O')
                || c == 'R';
    }

    /**
     * Truncates text so it fits within {@code maxWidth} pixels when rendered
     * with the standard Minecraft font (approximately 6 px/char).
     * Appends "…" if truncated.
     */
    public static String truncate(String text, int maxChars) {
        if (text == null) return "";
        if (text.length() <= maxChars) return text;
        return text.substring(0, Math.max(0, maxChars - 1)) + "…";
    }

    /**
     * Returns a human-readable preview of a line rule:
     * shows the plain match pattern → plain replacement.
     */
    public static String rulePreview(String matchPattern, String replacementText) {
        String match = truncate(matchPattern, 20);
        String replace = replacementText == null || replacementText.isEmpty()
                ? "(hide)"
                : truncate(replacementText, 20);
        return match + " → " + replace;
    }
}
