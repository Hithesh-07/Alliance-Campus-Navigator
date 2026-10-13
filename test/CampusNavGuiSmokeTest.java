import ui.CampusNavFrame;

import javax.swing.*;

/**
 * Smoke test that boots the real Swing GUI on EDT and verifies
 * frame construction, initial layout, and safe shutdown.
 */
public class CampusNavGuiSmokeTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting CampusNav GUI Smoke Test...");

        SwingUtilities.invokeAndWait(() -> {
            try {
                CampusNavFrame frame = new CampusNavFrame();
                frame.pack();
                System.out.println("Frame initialized successfully. Title: " + frame.getTitle());
                System.out.println("Frame minimum size: " + frame.getMinimumSize());

                // Test switching views
                frame.showCampusMap();
                System.out.println("Switched to Campus Map view successfully.");

                frame.showSearch();
                System.out.println("Switched to Find a Location view successfully.");

                frame.showManagement();
                System.out.println("Switched to Manage Locations view successfully.");

                frame.showAbout();
                System.out.println("Switched to About view successfully.");

                frame.showDashboard();
                System.out.println("Returned to Dashboard view successfully.");

                frame.dispose();
                System.out.println("Frame disposed safely.");
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        });

        System.out.println("ALL GUI SMOKE TESTS PASSED CLEANLY!");
        System.exit(0);
    }
}
