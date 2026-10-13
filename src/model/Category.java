package model;

import java.awt.Color;

/**
 * Enumeration of campus location categories with associated visual styling.
 */
public enum Category {
    ACADEMIC("Academic", new Color(37, 99, 235), new Color(239, 246, 255), "🎓"),
    ADMINISTRATIVE("Administrative", new Color(124, 58, 237), new Color(245, 243, 255), "🏛️"),
    FOOD("Food & Dining", new Color(234, 88, 12), new Color(255, 247, 237), "🍽️"),
    SPORTS("Sports & Recreation", new Color(22, 163, 74), new Color(240, 253, 244), "⚽"),
    RESIDENTIAL("Residential", new Color(217, 119, 6), new Color(254, 243, 199), "🏢"),
    OTHER("Other Services", new Color(100, 116, 139), new Color(241, 245, 249), "📍");

    private final String displayName;
    private final Color primaryColor;
    private final Color badgeBgColor;
    private final String icon;

    Category(String displayName, Color primaryColor, Color badgeBgColor, String icon) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.badgeBgColor = badgeBgColor;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public Color getBadgeBgColor() {
        return badgeBgColor;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Category fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        String cleaned = text.trim();
        for (Category c : values()) {
            if (c.name().equalsIgnoreCase(cleaned) || c.displayName.equalsIgnoreCase(cleaned)) {
                return c;
            }
        }
        // Partial match fallback
        String lower = cleaned.toLowerCase();
        if (lower.contains("academic") || lower.contains("engineering") || lower.contains("law") || lower.contains("business")) {
            return ACADEMIC;
        }
        if (lower.contains("admin")) {
            return ADMINISTRATIVE;
        }
        if (lower.contains("food") || lower.contains("cafe") || lower.contains("dine")) {
            return FOOD;
        }
        if (lower.contains("sport") || lower.contains("ground") || lower.contains("court")) {
            return SPORTS;
        }
        if (lower.contains("residence") || lower.contains("hostel") || lower.contains("hor") || lower.contains("quarter")) {
            return RESIDENTIAL;
        }
        return OTHER;
    }
}
