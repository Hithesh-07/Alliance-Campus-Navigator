package util;

import java.awt.Color;
import java.awt.Font;

/**
 * Design system tokens, color palettes, and fonts for CampusNav.
 */
public final class Theme {

    private Theme() {}

    // Sidebar & Shell Colors (Dark Navy / Charcoal)
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);          // Slate 900
    public static final Color SIDEBAR_CARD = new Color(30, 41, 59);        // Slate 800
    public static final Color SIDEBAR_HOVER = new Color(51, 65, 85);       // Slate 700
    public static final Color SIDEBAR_SELECTED = new Color(14, 116, 144);   // Cyan 700
    public static final Color SIDEBAR_ACCENT = new Color(45, 212, 191);    // Teal 400
    public static final Color SIDEBAR_TEXT = new Color(241, 245, 249);      // Slate 100
    public static final Color SIDEBAR_MUTED = new Color(148, 163, 184);    // Slate 400

    // Main Workspace Colors (Light Clean Slate)
    public static final Color BG_LIGHT = new Color(248, 250, 252);         // Slate 50
    public static final Color BG_WORKSPACE = new Color(241, 245, 249);     // Slate 100
    public static final Color CARD_BG = Color.WHITE;
    public static final Color CARD_BORDER = new Color(226, 232, 240);      // Slate 200
    public static final Color CARD_BORDER_HOVER = new Color(148, 163, 184);

    // Accent & Action Colors
    public static final Color PRIMARY = new Color(14, 116, 144);          // Teal/Cyan 700
    public static final Color PRIMARY_HOVER = new Color(15, 138, 172);
    public static final Color PRIMARY_LIGHT = new Color(207, 250, 254);    // Cyan 100
    public static final Color ACCENT_BLUE = new Color(37, 99, 235);        // Blue 600
    public static final Color SUCCESS = new Color(16, 185, 129);           // Emerald 500
    public static final Color WARNING = new Color(245, 158, 11);           // Amber 500
    public static final Color DANGER = new Color(239, 68, 68);             // Red 500
    public static final Color DANGER_LIGHT = new Color(254, 226, 226);

    // Typography Colors
    public static final Color TEXT_MAIN = new Color(15, 23, 42);           // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);       // Slate 500
    public static final Color TEXT_LIGHT = new Color(148, 163, 184);

    // Map Specific Canvas Colors
    public static final Color MAP_TERRAIN = new Color(238, 244, 235);      // Soft campus turf green
    public static final Color MAP_ROAD = new Color(203, 213, 225);         // Light asphalt
    public static final Color MAP_ROAD_MARK = new Color(255, 255, 255, 180);
    public static final Color MAP_PATH = new Color(226, 232, 240);         // Walkway
    public static final Color MAP_LAWN = new Color(209, 235, 198);         // Bright lawn patch
    public static final Color MAP_GRID = new Color(220, 228, 218, 120);
    public static final Color MAP_WATER = new Color(186, 230, 253);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_MAP_LABEL = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_MAP_SUB = new Font("Segoe UI", Font.PLAIN, 9);
}
