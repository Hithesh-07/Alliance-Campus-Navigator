# Alliance Campus Navigator — Presentation Deliverables

**Institution:** Alliance University, Bengaluru, Karnataka  
**Course:** B.Tech in Computer Science and Engineering  
**Student:** B. Hithesh Reddy  
**Project:** Alliance Campus Navigator (Java Micro Project, 2026–2027)  
**GitHub Repository:** [https://github.com/Hithesh-07/Alliance-Campus-Navigator](https://github.com/Hithesh-07/Alliance-Campus-Navigator)

---

## 📁 Presentation Files

```
presentation/
├── Alliance_Campus_Navigator_Presentation.pptx  # The 12-slide PowerPoint presentation
├── generate_presentation.py                     # Python script to build/regenerate the PPTX
├── README.md                                    # This documentation guide
└── screenshots/                                 # Real high-resolution app screenshots
    ├── 1_dashboard_view.png                     # Dashboard with live metric cards & popular hubs
    ├── 2_campus_map_overview.png                # Full Java2D schematic campus map overview
    ├── 3_campus_map_selected.png                # Campus map with selected School of Business
    ├── 4_search_view.png                        # Multi-criteria search panel & category chips
    ├── 5_management_view.png                    # CRUD location & coordinates management table
    └── 6_about_view.png                         # About project & technical specification
```

---

## 📊 Slide Structure (12 Slides)

| Slide | Title | Focus & Core Content |
| :---: | :--- | :--- |
| **1** | **Title Slide** | Project title, student name (B. Hithesh Reddy), B.Tech CSE, Alliance University, academic year (2026–2027), and app screenshot. |
| **2** | **Introduction** | University campus scale challenges, facility discovery issues, and project overview. |
| **3** | **Problem Statement & Motivation** | Official problem statement, user personas (students, visitors, admins), and offline motivations. |
| **4** | **Objectives & Scope** | 6 core project goals, schematic boundaries, and explicit limitations (no GPS / turn-by-turn routing). |
| **5** | **Technology Stack** | Java 21+, Java Swing, Java2D Graphics, RFC 4180 CSV, VS Code, PowerShell, and Git/GitHub. |
| **6** | **System Architecture** | 3-tier architecture diagram: Presentation (`ui`), Business Logic (`data`), Domain (`model`), and CSV Storage. |
| **7** | **Campus Map & Facility Categories** | Actual Java2D map screenshot with Alliance landmark layout and 6 color-coded categories. |
| **8** | **User Interface & Features** | 3 screenshot cards: Dashboard analytics, multi-criteria search, and CRUD management. |
| **9** | **Java Implementation & OOP** | Inheritance, polymorphism, encapsulation, event-driven Swing, and actual `toWorld()` drag code. |
| **10** | **File Handling & Data Persistence** | CSV schemas for `locations.csv` and `map_layout.csv`, data lifecycles, and RFC 4180 escaping. |
| **11** | **Testing, Results & Limitations** | Test results table (`CampusNavTest` & `CampusNavGuiSmokeTest` all passed), plus documented limitations. |
| **12** | **Conclusion & Future Scope** | Summary of achievements, future enhancement roadmap, GitHub link, and Thank You note. |

---

## 🛠 How to Regenerate or Customize the Presentation

If you edit the Python script `generate_presentation.py` or capture new screenshots:

1. Ensure `python-pptx` is installed:
   ```powershell
   pip install python-pptx pillow
   ```

2. Run the generator script:
   ```powershell
   python presentation/generate_presentation.py
   ```

3. Open `Alliance_Campus_Navigator_Presentation.pptx` in Microsoft PowerPoint or Google Slides to edit any text, shape, or table.
