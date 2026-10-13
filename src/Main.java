import ui.CampusNavFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Main application entry point for CampusNav: Alliance University Campus Navigation System.
 */
public class Main {

    public static void main(String[] args) {
        // Enable subpixel text antialiasing and modern font scaling
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for native window decorations
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Windows".equalsIgnoreCase(info.getName()) || "Nimbus".equalsIgnoreCase(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // Default Swing look and feel is safe fallback
            }

            // Customize basic Swing font defaults for crisp presentation
            customizeGlobalFonts();

            CampusNavFrame frame = new CampusNavFrame();
            frame.setVisible(true);
        });
    }

    private static void customizeGlobalFonts() {
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("Button.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("TextArea.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("ComboBox.font", defaultFont);
    }
}
