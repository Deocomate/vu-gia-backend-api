package vn.springboot.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Turns a human title into a URL-safe slug. Handles Vietnamese diacritics
 * (via Unicode NFD decomposition) and the đ/Đ special case.
 */
public final class SlugUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\p{Alnum}-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern DASHES = Pattern.compile("-{2,}");
    private static final Pattern EDGE_DASH = Pattern.compile("(^-|-$)");
    private static final Pattern COMBINING = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private SlugUtils() {
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = input.trim().replace('đ', 'd').replace('Đ', 'D');
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = COMBINING.matcher(normalized).replaceAll("");
        String slug = WHITESPACE.matcher(normalized).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        slug = DASHES.matcher(slug).replaceAll("-");
        slug = EDGE_DASH.matcher(slug).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
