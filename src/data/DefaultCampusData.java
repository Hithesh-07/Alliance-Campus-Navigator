package data;

import model.CampusLocation;
import model.Category;
import model.MapItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides static default data for initial campus loading and Reset Layout operations.
 * Reflects the spatial arrangement of Alliance University, Anekal, Bengaluru.
 */
public final class DefaultCampusData {

    private DefaultCampusData() {}

    public static List<CampusLocation> getDefaultLocations() {
        List<CampusLocation> list = new ArrayList<>();

        list.add(new CampusLocation(
                "LOC-001",
                "Administrative Block",
                Category.ADMINISTRATIVE,
                "Main university administration, Vice Chancellor's office, admissions, registrar, and visitor reception."
        ));

        list.add(new CampusLocation(
                "LOC-002",
                "Faculty of Engineering and Technology",
                Category.ACADEMIC,
                "Dean's office, department faculty chambers, high-tech computing labs, and multidisciplinary engineering workshops."
        ));

        list.add(new CampusLocation(
                "LOC-003",
                "Engineering Block",
                Category.ACADEMIC,
                "Lecture theatres, smart classrooms, robotics and IoT laboratories, and student project studios."
        ));

        list.add(new CampusLocation(
                "LOC-004",
                "Alliance University Library",
                Category.ACADEMIC,
                "Iconic central learning repository with distinctive dome, digital archives, study pods, and extensive academic collections."
        ));

        list.add(new CampusLocation(
                "LOC-005",
                "Learning Centre 1",
                Category.ACADEMIC,
                "Specialized multimedia classrooms, case study presentation halls, and collaborative group study areas."
        ));

        list.add(new CampusLocation(
                "LOC-006",
                "Alliance School of Business",
                Category.ACADEMIC,
                "Flagship management institute housed in the iconic circular academic complex with executive auditoriums."
        ));

        list.add(new CampusLocation(
                "LOC-007",
                "Alliance School of Law",
                Category.ACADEMIC,
                "Moot court halls, legal aid clinic, jurisprudence library section, and faculty offices."
        ));

        list.add(new CampusLocation(
                "LOC-008",
                "Alliance School of Liberal Arts",
                Category.ACADEMIC,
                "Humanities, economics, languages, media research labs, and cultural study studios."
        ));

        list.add(new CampusLocation(
                "LOC-009",
                "Main Central Academic Landmark",
                Category.ACADEMIC,
                "Central rotunda courtyard connecting schools of business and law, student open forums, and art displays."
        ));

        list.add(new CampusLocation(
                "LOC-010",
                "Alliance Food Court",
                Category.FOOD,
                "Spacious multi-vendor dining facility serving student meals, international breakfast, and evening refreshments."
        ));

        list.add(new CampusLocation(
                "LOC-011",
                "Daana Paani Multicuisine Cafe",
                Category.FOOD,
                "Popular fast-casual cafe near western campus gateway offering snacks, cold beverages, and quick bites."
        ));

        list.add(new CampusLocation(
                "LOC-012",
                "Alliance Cricket Ground",
                Category.SPORTS,
                "Full-sized natural turf cricket ground with turf pitch, practice nets, and spectator boundaries."
        ));

        list.add(new CampusLocation(
                "LOC-013",
                "Football Ground",
                Category.SPORTS,
                "Spacious regulation football arena equipped with tournament touchlines and athletic jogging perimeter."
        ));

        list.add(new CampusLocation(
                "LOC-014",
                "Alliance Volleyball Court",
                Category.SPORTS,
                "Outdoor sand and clay volleyball court with competition net line and night floodlights."
        ));

        list.add(new CampusLocation(
                "LOC-015",
                "Basketball Court",
                Category.SPORTS,
                "Hardcourt all-weather basketball facility with perimeter fencing and scorekeeper stands."
        ));

        list.add(new CampusLocation(
                "LOC-016",
                "Hall of Residence 1 (HOR 1)",
                Category.RESIDENTIAL,
                "Student residential complex with furnished rooms, student lounge, Wi-Fi zones, and resident warden desk."
        ));

        list.add(new CampusLocation(
                "LOC-017",
                "Hall of Residence 2 (HOR 2)",
                Category.RESIDENTIAL,
                "Multistory student hostel block with dining connectivity, laundry facilities, and indoor game room."
        ));

        list.add(new CampusLocation(
                "LOC-018",
                "Hall of Residence 5 (HOR 5)",
                Category.RESIDENTIAL,
                "Modern quadrangle hostel building located adjacent to the cricket ground and sports zone."
        ));

        list.add(new CampusLocation(
                "LOC-019",
                "Staff Quarters",
                Category.RESIDENTIAL,
                "Peaceful residential towers accommodating university faculty, visiting professors, and administrative personnel."
        ));

        list.add(new CampusLocation(
                "LOC-020",
                "Water Treatment Plant",
                Category.OTHER,
                "Eco-friendly sustainable campus water recycling and reverse osmosis filtration utility facility."
        ));

        return Collections.unmodifiableList(list);
    }

    public static List<MapItem> getDefaultMapLayout() {
        List<MapItem> list = new ArrayList<>();

        // Upper-West
        list.add(new MapItem("LOC-001", 160, 200, 180, 100, "ADMIN", ""));
        list.add(new MapItem("LOC-011", 200, 340, 140, 70, "RECTANGLE", ""));
        list.add(new MapItem("LOC-020", 190, 450, 130, 70, "RECTANGLE", ""));

        // Upper-North & North-East
        list.add(new MapItem("LOC-002", 450, 80, 230, 85, "RECTANGLE", ""));
        list.add(new MapItem("LOC-003", 450, 190, 210, 80, "COURTYARD", ""));
        list.add(new MapItem("LOC-004", 710, 100, 180, 110, "DOMED", ""));
        list.add(new MapItem("LOC-005", 710, 235, 170, 75, "RECTANGLE", ""));
        list.add(new MapItem("LOC-018", 930, 160, 150, 120, "COURTYARD", ""));
        list.add(new MapItem("LOC-012", 1120, 110, 270, 210, "FIELD", ""));
        list.add(new MapItem("LOC-014", 940, 315, 160, 70, "COURT", ""));

        // Central
        list.add(new MapItem("LOC-007", 390, 340, 140, 85, "RECTANGLE", ""));
        list.add(new MapItem("LOC-006", 560, 330, 170, 140, "ROUNDED_HUB", ""));
        list.add(new MapItem("LOC-008", 760, 350, 150, 85, "RECTANGLE", ""));
        list.add(new MapItem("LOC-009", 585, 490, 120, 45, "ROUNDED_HUB", ""));

        // Central-South & South
        list.add(new MapItem("LOC-010", 540, 565, 210, 95, "RECTANGLE", ""));
        list.add(new MapItem("LOC-013", 180, 600, 290, 190, "FIELD", ""));
        list.add(new MapItem("LOC-015", 500, 710, 140, 85, "COURT", ""));
        list.add(new MapItem("LOC-016", 670, 710, 160, 95, "COURTYARD", ""));
        list.add(new MapItem("LOC-017", 860, 560, 160, 105, "COURTYARD", ""));
        list.add(new MapItem("LOC-019", 1060, 560, 180, 115, "RECTANGLE", ""));

        return Collections.unmodifiableList(list);
    }
}
