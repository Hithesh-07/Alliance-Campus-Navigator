# CampusNav — Alliance University Campus Navigation System

**Institution:** Alliance University, Chandapura - Anekal Main Road, Bengaluru, Karnataka, India  
**Program:** B.Tech Computer Science & Engineering (Micro Project)  
**Platform:** Java 21+ / Modern Java SE Desktop Application (Java Swing & Java2D)  

---

## 🏛 Project Overview

**CampusNav** is an offline-first desktop navigation and campus discovery application designed specifically for **Alliance University**. The application provides students, faculty, and campus visitors with an interactive, searchable schematic map of key university departments, academic buildings, student residential hostels, sports facilities, and dining locations.

### Key Highlights
- **Zero External API or Database Dependencies:** Runs completely offline using pure Java SE without needing paid mapping keys, network access, or SQL servers.
- **Dynamic Java2D Vector Map:** All buildings, roads, pathways, sports fields, and landscaped lawns are drawn in real-time using Java2D vector primitives—not a static bitmap image.
- **Interactive Map Dragging & Layout Customization:** Buildings can be dragged across the campus canvas in real-time, saved to `data/map_layout.csv`, and restored across application restarts.
- **Robust CSV Persistence:** Automatically creates and maintains `data/locations.csv` and `data/map_layout.csv` with full RFC 4180 quoting and UTF-8 encoding support.
- **Modern Desktop UI:** Built with custom-painted Swing components, a dark navy sidebar, live metric summary cards, category badge chips, and smooth multi-criteria search.

---

## 🗺 Campus Layout & Architectural Landmarks

The schematic layout is modeled after the spatial relationships of Alliance University's Central Campus:
1. **Upper / Northern Campus:**
   - Administrative Block (Western campus entrance portico)
   - Faculty of Engineering and Technology
   - Engineering Block (Smart laboratories and lecture halls)
   - Alliance University Library (Distinctive turquoise/copper dome)
   - Learning Centre 1
2. **Central Campus:**
   - **Alliance School of Business:** Distinctive circular rotunda with internal amphitheater courtyard
   - Alliance School of Law
   - Alliance School of Liberal Arts
   - Central Academic Courtyard
   - Connecting boulevards, paved pedestrian walkways, and landscaped plazas
3. **Lower / Southern Campus:**
   - Alliance Food Court & Daana Paani Multicuisine Cafe
   - Water Treatment Plant
   - Regulation Football Ground (turf field with goal boxes and center circle)
   - Alliance Cricket Ground (oval boundary with center pitch strip)
   - Volleyball Court & Basketball Court
   - Hall of Residence 1 (HOR 1), Hall of Residence 2 (HOR 2), and Hall of Residence 5 (HOR 5)
   - Staff Quarters

---

## 🛠 Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **Language** | Java 21+ | Modern object-oriented Java SE |
| **GUI Framework** | Java Swing & AWT | Event Dispatch Thread (EDT), custom painting |
| **Map Rendering** | Java2D Graphics | Anti-aliased vector rendering (`Graphics2D`, `Path2D`, `AffineTransform`) |
| **Persistence** | CSV (RFC 4180) | Local UTF-8 files: `data/locations.csv`, `data/map_layout.csv` |
| **IDE** | VS Code / Terminal | Native PowerShell compilation with `javac` |

---

## 📁 Project Structure

```
CampusNav/
├── .gitignore
├── README.md
├── run.ps1                       # One-click PowerShell compilation & run script
├── data/
│   ├── locations.csv             # Campus location records (ID, name, category, description)
│   └── map_layout.csv            # 2D geometry (ID, X, Y, width, height, shapeType)
├── out/                          # Compiled bytecode (.class files)
├── test/
│   ├── CampusNavTest.java        # Automated persistence, search & geometry tests
│   └── CampusNavGuiSmokeTest.java# Automated EDT GUI smoke test
└── src/
    ├── Main.java                 # Entry point with antialiasing & look-and-feel
    ├── model/
    │   ├── Category.java         # Category enum with color palettes & icons
    │   ├── CampusLocation.java   # Location data model
    │   └── MapItem.java          # Spatial coordinates & shape type model
    ├── data/
    │   ├── CsvUtils.java         # RFC 4180 CSV parser & writer
    │   ├── DefaultCampusData.java# Static starter data & Reset Layout defaults
    │   └── CampusDataManager.java# Persistence manager & change event bus
    ├── ui/
    │   ├── NavigationController.java # Navigation interface
    │   ├── CampusNavFrame.java   # Main desktop window with dark navy sidebar & header
    │   ├── DashboardPanel.java   # Analytics cards, map preview & popular hubs
    │   ├── CampusMapPanel.java   # High-DPI interactive Java2D canvas with pan/zoom/drag
    │   ├── LocationSearchPanel.java # Keyword search & category filter chips
    │   ├── LocationManagementPanel.java # CRUD table management & filters
    │   ├── LocationDialog.java   # Modal dialog for Add / Edit locations
    │   └── AboutPanel.java       # Academic viva overview, objectives & notes
    └── util/
        ├── Theme.java            # Color tokens, fonts, and theme definitions
        └── UiUtils.java          # Modern rounded buttons, cards, and badges
```

---

## 🚀 How to Build and Run

### Prerequisites
- Java Development Kit (JDK 21 or later).
  - Verify your installation by opening PowerShell and running:
    ```powershell
    java -version
    javac -version
    ```
  - If Java is not installed, download the JDK from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium Temurin](https://adoptium.net/) and ensure Java is added to your system `PATH`.

### Option A: One-Click Launch with PowerShell Script
In your terminal, navigate to the `CampusNav` directory and execute:
```powershell
.\run.ps1
```

### Option B: Manual Command-Line Compilation & Launch

1. **Navigate to the project root:**
   ```powershell
   cd C:\Users\98858\.gemini\antigravity\scratch\CampusNav
   ```

2. **Compile all Java source files into the `out` directory:**
   ```powershell
   $files = Get-ChildItem -Recurse -Path src -Filter *.java | ForEach-Object { $_.FullName }
   javac -encoding UTF-8 -d out $files
   ```

3. **Launch the desktop application:**
   ```powershell
   java -cp out Main
   ```

---

## 🧪 Running the Automated Verification Suite

To verify CSV handling, default data integrity, location CRUD, search queries, and EDT GUI lifecycle:

```powershell
# Compile tests
javac -encoding UTF-8 -d out -cp out test/CampusNavTest.java test/CampusNavGuiSmokeTest.java

# Run backend logic tests
java -cp out CampusNavTest

# Run GUI smoke test
java -cp out CampusNavGuiSmokeTest
```

---

## 💡 User Guide & Features Walkthrough

1. **Dashboard:**
   - View live calculated statistics: Total Locations, Academic Blocks, Food & Dining, Sports Facilities, and Hostels.
   - Click any card to filter locations in the search panel.
   - Click the interactive mini-map preview to jump directly to the full campus map.
2. **Campus Map View:**
   - **Pan:** Click and drag any empty grass area (or right-click drag).
   - **Zoom:** Use the mouse wheel or the `➕ Zoom In` and `➖ Zoom Out` buttons.
   - **Fit Campus:** Click `⛶ Fit Campus` to recenter and fit the entire campus into view.
   - **Select Location:** Click on any building to view its detail card with title, category badge, coordinates, and description.
   - **Drag & Reorder Buildings:** Click and hold any building, then drag it to a new location on campus.
   - **Save Layout:** Click `💾 Save Layout` to write modified coordinates to `data/map_layout.csv`.
   - **Reset Layout:** Click `↺ Reset Layout` to restore the original schematic coordinates.
3. **Find a Location:**
   - Search by building title, category, or description keywords (e.g., `"library"`, `"food"`, `"sports"`).
   - Toggle category filter chips (`All Categories`, `Academic`, `Administrative`, `Food & Dining`, `Sports`, `Residential`, `Other`).
   - Click `🗺 Show on Map` on any card to immediately focus and highlight the building on the campus map.
4. **Manage Locations:**
   - Add new locations with assigned coordinates and architectural styles (`ROUNDED_HUB`, `DOMED`, `COURTYARD`, `ADMIN`, `FIELD`, `COURT`, `RECTANGLE`).
   - Edit names, categories, descriptions, or coordinates.
   - Delete locations with confirmation dialogs.
   - All changes immediately sync to `locations.csv`, `map_layout.csv`, and all view counters.
5. **Global Header Search:**
   - Enter a search term into the top header search bar at any time to immediately query campus locations from any view.

---

## ⚖️ Academic Disclaimer
*CampusNav is an illustrative schematic micro-project created for educational discovery and orientation. Map representations, proportions, and coordinates are schematic illustrations and not intended for satellite-accurate surveying, GPS navigation, or turn-by-turn routing.*
