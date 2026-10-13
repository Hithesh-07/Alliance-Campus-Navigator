import data.CampusDataManager;
import data.CsvUtils;
import data.DefaultCampusData;
import model.CampusLocation;
import model.Category;
import model.MapItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated test suite for CampusNav verifying CSV persistence, data manager,
 * search logic, geometry calculations, and lifecycle operations.
 */
public class CampusNavTest {

    public static void main(String[] args) {
        System.out.println("=== RUNNING CAMPUSNAV AUTOMATED VERIFICATION SUITE ===");
        int passed = 0;
        int total = 0;

        total++;
        if (testCsvEscaping()) {
            System.out.println("✔ Test 1: CSV Escaping & Parsing Passed");
            passed++;
        } else {
            System.err.println("✘ Test 1: CSV Escaping Failed");
        }

        total++;
        if (testDefaultDataIntegrity()) {
            System.out.println("✔ Test 2: Default Campus Data Integrity Passed");
            passed++;
        } else {
            System.err.println("✘ Test 2: Default Campus Data Integrity Failed");
        }

        total++;
        if (testDataManagerPersistenceAndLifecycle()) {
            System.out.println("✔ Test 3: Data Manager CRUD & CSV Persistence Passed");
            passed++;
        } else {
            System.err.println("✘ Test 3: Data Manager CRUD & CSV Persistence Failed");
        }

        total++;
        if (testSearchFiltering()) {
            System.out.println("✔ Test 4: Search & Category Filtering Passed");
            passed++;
        } else {
            System.err.println("✘ Test 4: Search & Category Filtering Failed");
        }

        total++;
        if (testMapGeometryAndBounds()) {
            System.out.println("✔ Test 5: Map Geometry & Hit Testing Passed");
            passed++;
        } else {
            System.err.println("✘ Test 5: Map Geometry & Hit Testing Failed");
        }

        System.out.println(String.format("\nVERIFICATION SUMMARY: %d/%d tests passed successfully.", passed, total));
        if (passed != total) {
            System.exit(1);
        }
    }

    private static boolean testCsvEscaping() {
        try {
            File temp = File.createTempFile("campusnav_test_", ".csv");
            temp.deleteOnExit();

            String[] headers = {"id", "name", "desc"};
            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{"LOC-01", "Building, with comma", "Description with \"quotes\" and newline"});
            rows.add(new String[]{"LOC-02", "Standard Name", "Simple desc"});

            CsvUtils.writeCsv(temp, headers, rows);

            List<List<String>> readRows = CsvUtils.readCsv(temp);
            if (readRows.size() != 3) { // 1 header + 2 rows
                System.err.println("Expected 3 rows, got: " + readRows.size());
                return false;
            }

            List<String> firstDataRow = readRows.get(1);
            if (!firstDataRow.get(0).equals("LOC-01") ||
                !firstDataRow.get(1).equals("Building, with comma") ||
                !firstDataRow.get(2).contains("\"quotes\"")) {
                System.err.println("Mismatch in escaped data: " + firstDataRow);
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testDefaultDataIntegrity() {
        List<CampusLocation> locs = DefaultCampusData.getDefaultLocations();
        List<MapItem> layout = DefaultCampusData.getDefaultMapLayout();

        if (locs.size() != 20) {
            System.err.println("Expected 20 default locations, found: " + locs.size());
            return false;
        }
        if (layout.size() != 20) {
            System.err.println("Expected 20 default layout items, found: " + layout.size());
            return false;
        }

        // Verify key locations
        boolean foundAdmin = false;
        boolean foundBusiness = false;
        boolean foundLibrary = false;
        boolean foundCricket = false;

        for (CampusLocation loc : locs) {
            if (loc.getName().contains("Administrative Block")) foundAdmin = true;
            if (loc.getName().contains("Alliance School of Business")) foundBusiness = true;
            if (loc.getName().contains("Alliance University Library")) foundLibrary = true;
            if (loc.getName().contains("Cricket Ground")) foundCricket = true;
        }

        return foundAdmin && foundBusiness && foundLibrary && foundCricket;
    }

    private static boolean testDataManagerPersistenceAndLifecycle() {
        try {
            CampusDataManager dm = CampusDataManager.getInstance();
            dm.loadData();

            int initialCount = dm.getTotalLocationsCount();
            if (initialCount < 20) {
                System.err.println("Initial location count unexpected: " + initialCount);
                return false;
            }

            // Test Add
            String testId = "TEST-999";
            CampusLocation testLoc = new CampusLocation(testId, "Test Robotics Studio", Category.ACADEMIC, "Special lab");
            MapItem testItem = new MapItem(testId, 300, 300, 120, 60, "RECTANGLE", "");
            dm.addLocation(testLoc, testItem);

            if (dm.getLocation(testId) == null || dm.getMapItem(testId) == null) {
                System.err.println("Failed to retrieve added location/map item");
                return false;
            }

            // Test Update
            testLoc.setName("Updated Robotics Studio");
            testItem.setX(350);
            dm.updateLocationAndMapItem(testLoc, testItem);

            if (!dm.getLocation(testId).getName().equals("Updated Robotics Studio")) {
                System.err.println("Update failed");
                return false;
            }

            // Test Delete
            dm.deleteLocation(testId);
            if (dm.getLocation(testId) != null) {
                System.err.println("Delete failed");
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testSearchFiltering() {
        CampusDataManager dm = CampusDataManager.getInstance();

        // 1. Keyword search "library"
        List<CampusLocation> libraryResults = dm.search("library", null);
        boolean foundLib = false;
        for (CampusLocation l : libraryResults) {
            if (l.getName().toLowerCase().contains("library")) {
                foundLib = true;
                break;
            }
        }
        if (!foundLib) return false;

        // 2. Category filter Sports
        List<CampusLocation> sportsResults = dm.search("", Category.SPORTS);
        if (sportsResults.isEmpty()) return false;
        for (CampusLocation l : sportsResults) {
            if (l.getCategory() != Category.SPORTS) return false;
        }

        // 3. Search non-existent
        List<CampusLocation> emptyResults = dm.search("NonExistentBuildingXYZ123", null);
        if (!emptyResults.isEmpty()) return false;

        return true;
    }

    private static boolean testMapGeometryAndBounds() {
        MapItem item = new MapItem("LOC-TEST", 100, 100, 150, 80, "RECTANGLE", "");

        if (!item.contains(100, 100)) return false;
        if (!item.contains(200, 150)) return false;
        if (!item.contains(250, 180)) return false;
        if (item.contains(99, 100)) return false;
        if (item.contains(251, 150)) return false;

        return true;
    }
}
