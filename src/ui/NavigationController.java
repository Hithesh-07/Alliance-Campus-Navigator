package ui;

/**
 * Interface allowing views to trigger smooth navigation between sections of CampusNav.
 */
public interface NavigationController {
    void showDashboard();
    void showCampusMap();
    void showCampusMapWithLocation(String locationId);
    void showSearch();
    void showSearchWithQuery(String query);
    void showManagement();
    void showAbout();
}
