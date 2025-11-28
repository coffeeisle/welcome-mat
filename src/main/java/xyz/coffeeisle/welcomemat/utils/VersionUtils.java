package xyz.coffeeisle.welcomemat.utils;

/**
 * Simple semantic-like version comparison helper. Supports dot-separated
 * numeric segments such as 1.3 or 1.3.1 and ignores trailing zeros.
 */
public final class VersionUtils {
    private VersionUtils() {
    }

    public static int compare(String current, String target) {
        if (current == null) {
            current = "0";
        }
        if (target == null) {
            target = "0";
        }

        String[] currentParts = current.split("\\.");
        String[] targetParts = target.split("\\.");
        int length = Math.max(currentParts.length, targetParts.length);
        for (int i = 0; i < length; i++) {
            int currentValue = i < currentParts.length ? parseIntSafe(currentParts[i]) : 0;
            int targetValue = i < targetParts.length ? parseIntSafe(targetParts[i]) : 0;
            if (currentValue != targetValue) {
                return Integer.compare(currentValue, targetValue);
            }
        }
        return 0;
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
