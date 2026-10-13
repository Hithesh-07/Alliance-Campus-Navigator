package data;

import model.CampusLocation;
import model.Category;
import model.MapItem;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Data Access and Persistence Manager for CampusNav.
 * Synchronizes in-memory models with CSV persistence in data/ directory.
 */
public class CampusDataManager {

    private static CampusDataManager instance;

    public interface DataChangeListener {
        void onDataChanged();
        default void onLocationSelected(String locationId) {}
    }

    private final List<DataChangeListener> listeners = new CopyOnWriteArrayList<>();

    private final Map<String, CampusLocation> locationMap = new LinkedHashMap<>();
    private final Map<String, MapItem> mapItemMap = new LinkedHashMap<>();

    private File dataDir;
    private File locationsFile;
    private File mapLayoutFile;

    private CampusDataManager() {
        resolveDataFiles();
    }

    public static synchronized CampusDataManager getInstance() {
        if (instance == null) {
            instance = new CampusDataManager();
        }
        return instance;
    }

    private void resolveDataFiles() {
        // Search for data directory in likely locations (working dir, parent, etc.)
        File directData = new File("data");
        File parentData = new File("../data");
        File scratchData = new File("CampusNav/data");

        if (directData.exists() && directData.isDirectory()) {
            dataDir = directData;
        } else if (scratchData.exists() && scratchData.isDirectory()) {
            dataDir = scratchData;
        } else if (parentData.exists() && parentData.isDirectory()) {
            dataDir = parentData;
        } else {
            // Default to local "data"
            dataDir = directData;
            dataDir.mkdirs();
        }

        locationsFile = new File(dataDir, "locations.csv");
        mapLayoutFile = new File(dataDir, "map_layout.csv");
    }

    /**
     * Initializes data from CSV files. If files do not exist or are empty,
     * seeds initial default campus data.
     */
    public synchronized void loadData() {
        locationMap.clear();
        mapItemMap.clear();

        boolean needsLocationsInit = !locationsFile.exists() || locationsFile.length() == 0;
        boolean needsLayoutInit = !mapLayoutFile.exists() || mapLayoutFile.length() == 0;

        if (needsLocationsInit || needsLayoutInit) {
            seedDefaultData(needsLocationsInit, needsLayoutInit);
        }

        // Read Locations CSV
        try {
            List<List<String>> rows = CsvUtils.readCsv(locationsFile);
            boolean first = true;
            for (List<String> row : rows) {
                if (first) {
                    first = false;
                    // Check if it's the header row
                    if (!row.isEmpty() && row.get(0).equalsIgnoreCase("id")) {
                        continue;
                    }
                }
                if (row.size() >= 3) {
                    String id = row.get(0);
                    String name = row.get(1);
                    Category cat = Category.fromString(row.get(2));
                    String desc = row.size() >= 4 ? row.get(3) : "";
                    locationMap.put(id, new CampusLocation(id, name, cat, desc));
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Error reading locations.csv: " + e.getMessage());
            // Fallback to default if corrupted
            for (CampusLocation loc : DefaultCampusData.getDefaultLocations()) {
                locationMap.put(loc.getId(), loc);
            }
        }

        // Read Map Layout CSV
        try {
            List<List<String>> rows = CsvUtils.readCsv(mapLayoutFile);
            boolean first = true;
            for (List<String> row : rows) {
                if (first) {
                    first = false;
                    if (!row.isEmpty() && row.get(0).equalsIgnoreCase("id")) {
                        continue;
                    }
                }
                if (row.size() >= 5) {
                    String id = row.get(0);
                    double x = parseDoubleSafe(row.get(1), 100);
                    double y = parseDoubleSafe(row.get(2), 100);
                    double w = parseDoubleSafe(row.get(3), 120);
                    double h = parseDoubleSafe(row.get(4), 80);
                    String shape = row.size() >= 6 ? row.get(5) : "RECTANGLE";
                    String color = row.size() >= 7 ? row.get(6) : "";
                    mapItemMap.put(id, new MapItem(id, x, y, w, h, shape, color));
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Error reading map_layout.csv: " + e.getMessage());
            for (MapItem item : DefaultCampusData.getDefaultMapLayout()) {
                mapItemMap.put(item.getId(), item);
            }
        }

        // Ensure consistency: every location must have a map item
        int offsetX = 100;
        int offsetY = 850;
        for (String locId : locationMap.keySet()) {
            if (!mapItemMap.containsKey(locId)) {
                mapItemMap.put(locId, new MapItem(locId, offsetX, offsetY, 130, 70, "RECTANGLE", ""));
                offsetX += 150;
                if (offsetX > 1200) {
                    offsetX = 100;
                    offsetY += 90;
                }
            }
        }

        notifyDataChanged();
    }

    private double parseDoubleSafe(String val, double def) {
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private void seedDefaultData(boolean seedLocations, boolean seedLayout) {
        if (seedLocations) {
            List<String[]> locRows = new ArrayList<>();
            for (CampusLocation loc : DefaultCampusData.getDefaultLocations()) {
                locRows.add(new String[]{loc.getId(), loc.getName(), loc.getCategory().name(), loc.getDescription()});
            }
            try {
                CsvUtils.writeCsv(locationsFile, new String[]{"id", "name", "category", "description"}, locRows);
            } catch (IOException e) {
                System.err.println("Could not seed locations.csv: " + e.getMessage());
            }
        }

        if (seedLayout) {
            List<String[]> layoutRows = new ArrayList<>();
            for (MapItem item : DefaultCampusData.getDefaultMapLayout()) {
                layoutRows.add(new String[]{
                        item.getId(),
                        String.valueOf((int)item.getX()),
                        String.valueOf((int)item.getY()),
                        String.valueOf((int)item.getWidth()),
                        String.valueOf((int)item.getHeight()),
                        item.getShapeType(),
                        item.getCustomColorHex()
                });
            }
            try {
                CsvUtils.writeCsv(mapLayoutFile, new String[]{"id", "x", "y", "width", "height", "shapeType", "customColorHex"}, layoutRows);
            } catch (IOException e) {
                System.err.println("Could not seed map_layout.csv: " + e.getMessage());
            }
        }
    }

    public synchronized void saveLocations() throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (CampusLocation loc : locationMap.values()) {
            rows.add(new String[]{
                    loc.getId(),
                    loc.getName(),
                    loc.getCategory().name(),
                    loc.getDescription()
            });
        }
        CsvUtils.writeCsv(locationsFile, new String[]{"id", "name", "category", "description"}, rows);
    }

    public synchronized void saveMapLayout() throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (MapItem item : mapItemMap.values()) {
            rows.add(new String[]{
                    item.getId(),
                    String.valueOf(Math.round(item.getX())),
                    String.valueOf(Math.round(item.getY())),
                    String.valueOf(Math.round(item.getWidth())),
                    String.valueOf(Math.round(item.getHeight())),
                    item.getShapeType(),
                    item.getCustomColorHex()
            });
        }
        CsvUtils.writeCsv(mapLayoutFile, new String[]{"id", "x", "y", "width", "height", "shapeType", "customColorHex"}, rows);
    }

    public synchronized void resetToDefaultLayout() throws IOException {
        List<MapItem> defaults = DefaultCampusData.getDefaultMapLayout();
        for (MapItem d : defaults) {
            mapItemMap.put(d.getId(), new MapItem(d));
        }
        saveMapLayout();
        notifyDataChanged();
    }

    public synchronized void resetAllDataToDefault() throws IOException {
        locationMap.clear();
        for (CampusLocation loc : DefaultCampusData.getDefaultLocations()) {
            locationMap.put(loc.getId(), loc);
        }
        mapItemMap.clear();
        for (MapItem item : DefaultCampusData.getDefaultMapLayout()) {
            mapItemMap.put(item.getId(), new MapItem(item));
        }
        saveLocations();
        saveMapLayout();
        notifyDataChanged();
    }

    public synchronized List<CampusLocation> getAllLocations() {
        return new ArrayList<>(locationMap.values());
    }

    public synchronized CampusLocation getLocation(String id) {
        return locationMap.get(id);
    }

    public synchronized MapItem getMapItem(String id) {
        return mapItemMap.get(id);
    }

    public synchronized List<MapItem> getAllMapItems() {
        return new ArrayList<>(mapItemMap.values());
    }

    public synchronized boolean existsId(String id) {
        return locationMap.containsKey(id);
    }

    public synchronized String generateNextId() {
        int max = 0;
        for (String id : locationMap.keySet()) {
            if (id.startsWith("LOC-")) {
                try {
                    int num = Integer.parseInt(id.substring(4));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("LOC-%03d", max + 1);
    }

    public synchronized void addLocation(CampusLocation loc, MapItem item) throws IOException {
        if (loc == null || loc.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Location ID cannot be empty.");
        }
        if (locationMap.containsKey(loc.getId())) {
            throw new IllegalArgumentException("A location with ID '" + loc.getId() + "' already exists.");
        }
        locationMap.put(loc.getId(), loc);
        if (item == null) {
            item = new MapItem(loc.getId(), 500, 500, 140, 80, "RECTANGLE", "");
        } else {
            item.setId(loc.getId());
        }
        mapItemMap.put(loc.getId(), item);
        saveLocations();
        saveMapLayout();
        notifyDataChanged();
    }

    public synchronized void updateLocation(CampusLocation loc) throws IOException {
        if (loc == null || !locationMap.containsKey(loc.getId())) {
            throw new IllegalArgumentException("Location does not exist.");
        }
        locationMap.put(loc.getId(), loc);
        saveLocations();
        notifyDataChanged();
    }

    public synchronized void updateMapItem(MapItem item) throws IOException {
        if (item == null || !mapItemMap.containsKey(item.getId())) {
            throw new IllegalArgumentException("Map item does not exist.");
        }
        mapItemMap.put(item.getId(), item);
        saveMapLayout();
        notifyDataChanged();
    }

    public synchronized void updateLocationAndMapItem(CampusLocation loc, MapItem item) throws IOException {
        if (loc == null || !locationMap.containsKey(loc.getId())) {
            throw new IllegalArgumentException("Location does not exist.");
        }
        locationMap.put(loc.getId(), loc);
        if (item != null) {
            item.setId(loc.getId());
            mapItemMap.put(loc.getId(), item);
        }
        saveLocations();
        saveMapLayout();
        notifyDataChanged();
    }

    public synchronized void deleteLocation(String id) throws IOException {
        if (id != null) {
            locationMap.remove(id);
            mapItemMap.remove(id);
            saveLocations();
            saveMapLayout();
            notifyDataChanged();
        }
    }

    public synchronized List<CampusLocation> search(String query, Category categoryFilter) {
        List<CampusLocation> results = new ArrayList<>();
        String q = query != null ? query.trim().toLowerCase() : "";

        for (CampusLocation loc : locationMap.values()) {
            if (categoryFilter != null && loc.getCategory() != categoryFilter) {
                continue;
            }
            if (q.isEmpty()) {
                results.add(loc);
                continue;
            }

            boolean match = loc.getName().toLowerCase().contains(q)
                    || loc.getId().toLowerCase().contains(q)
                    || loc.getCategory().getDisplayName().toLowerCase().contains(q)
                    || loc.getDescription().toLowerCase().contains(q);

            if (match) {
                results.add(loc);
            }
        }
        return results;
    }

    // Counts for Dashboard
    public synchronized int getTotalLocationsCount() {
        return locationMap.size();
    }

    public synchronized int getCountByCategory(Category category) {
        int count = 0;
        for (CampusLocation loc : locationMap.values()) {
            if (loc.getCategory() == category) {
                count++;
            }
        }
        return count;
    }

    // Listener management
    public void addDataChangeListener(DataChangeListener listener) {
        listeners.add(listener);
    }

    public void removeDataChangeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    public void notifyDataChanged() {
        for (DataChangeListener l : listeners) {
            try {
                l.onDataChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyLocationSelected(String id) {
        for (DataChangeListener l : listeners) {
            try {
                l.onLocationSelected(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public File getDataDir() {
        return dataDir;
    }
}
