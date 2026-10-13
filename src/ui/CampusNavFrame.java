package ui;

import data.CampusDataManager;
import model.CampusLocation;
import util.Theme;
import util.UiUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main application window for CampusNav.
 * Houses the dark navy sidebar, persistent top header with global search,
 * and CardLayout workspace managing primary views.
 */
public class CampusNavFrame extends JFrame implements NavigationController {

    private static final String VIEW_DASHBOARD = "DASHBOARD";
    private static final String VIEW_MAP = "MAP";
    private static final String VIEW_SEARCH = "SEARCH";
    private static final String VIEW_MANAGEMENT = "MANAGEMENT";
    private static final String VIEW_ABOUT = "ABOUT";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);

    private DashboardPanel dashboardPanel;
    private CampusMapPanel campusMapPanel;
    private LocationSearchPanel searchPanel;
    private LocationManagementPanel managementPanel;
    private AboutPanel aboutPanel;

    private JLabel headerPageTitle;
    private JTextField headerSearchField;

    private final Map<String, JButton> sidebarButtons = new HashMap<>();
    private String currentView = VIEW_DASHBOARD;

    public CampusNavFrame() {
        super("CampusNav — Alliance University Campus Navigation System");

        // Initialize data first
        CampusDataManager.getInstance().loadData();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1240, 800);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Theme.BG_WORKSPACE);

        // 1. Sidebar (Dark Charcoal Navy)
        rootPanel.add(createSidebar(), BorderLayout.WEST);

        // 2. Main Workspace (Header + View Cards)
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setOpaque(false);

        mainContainer.add(createHeader(), BorderLayout.NORTH);
        initViews();
        mainContainer.add(contentCards, BorderLayout.CENTER);

        rootPanel.add(mainContainer, BorderLayout.CENTER);
        setContentPane(rootPanel);

        // Default initial view
        showDashboard();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(Theme.SIDEBAR_BG);

        // Sidebar Brand Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(22, 20, 24, 20));

        JLabel brandLogo = new JLabel("CAMPUSNAV");
        brandLogo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brandLogo.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Alliance University");
        brandSub.setFont(Theme.FONT_SMALL_BOLD);
        brandSub.setForeground(Theme.SIDEBAR_ACCENT);

        JLabel brandLoc = new JLabel("Anekal, Bengaluru");
        brandLoc.setFont(Theme.FONT_SMALL);
        brandLoc.setForeground(Theme.SIDEBAR_MUTED);

        brandPanel.add(brandLogo);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(brandSub);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(brandLoc);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Sidebar Navigation Links
        JPanel navMenu = new JPanel();
        navMenu.setLayout(new BoxLayout(navMenu, BoxLayout.Y_AXIS));
        navMenu.setOpaque(false);
        navMenu.setBorder(new EmptyBorder(0, 12, 0, 12));

        navMenu.add(createNavButton("📊  Dashboard", VIEW_DASHBOARD, this::showDashboard));
        navMenu.add(Box.createVerticalStrut(6));
        navMenu.add(createNavButton("🗺️  Campus Map", VIEW_MAP, this::showCampusMap));
        navMenu.add(Box.createVerticalStrut(6));
        navMenu.add(createNavButton("🔍  Find a Location", VIEW_SEARCH, this::showSearch));
        navMenu.add(Box.createVerticalStrut(6));
        navMenu.add(createNavButton("⚙️  Manage Locations", VIEW_MANAGEMENT, this::showManagement));
        navMenu.add(Box.createVerticalStrut(6));
        navMenu.add(createNavButton("ℹ️  About Project", VIEW_ABOUT, this::showAbout));

        sidebar.add(navMenu, BorderLayout.CENTER);

        // Sidebar Footer Info
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel version = new JLabel("CampusNav v1.0 • Modern Java");
        version.setFont(Theme.FONT_SMALL);
        version.setForeground(Theme.SIDEBAR_MUTED);

        JLabel offlineBadge = new JLabel("● Offline Ready • CSV Backed");
        offlineBadge.setFont(Theme.FONT_SMALL);
        offlineBadge.setForeground(Theme.SUCCESS);

        footer.add(version);
        footer.add(Box.createVerticalStrut(4));
        footer.add(offlineBadge);

        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String label, String viewKey, Runnable action) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.applyQualityHints(g2);

                boolean isSelected = viewKey.equals(currentView);

                if (isSelected) {
                    g2.setColor(Theme.SIDEBAR_SELECTED);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                } else if (getModel().isRollover()) {
                    g2.setColor(Theme.SIDEBAR_HOVER);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setForeground(Theme.SIDEBAR_TEXT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> action.run());

        sidebarButtons.put(viewKey, btn);
        return btn;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.CARD_BORDER),
                new EmptyBorder(14, 28, 14, 28)
        ));

        // Left Title
        headerPageTitle = new JLabel("Dashboard Overview");
        headerPageTitle.setFont(Theme.FONT_HEADER);
        headerPageTitle.setForeground(Theme.TEXT_MAIN);
        header.add(headerPageTitle, BorderLayout.WEST);

        // Center / Right: Global Search & Campus Badge
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightHeader.setOpaque(false);

        // Header Quick Search Box
        JPanel searchBox = new JPanel(new BorderLayout(6, 0));
        searchBox.setOpaque(false);

        headerSearchField = UiUtils.createTextField(20);
        headerSearchField.putClientProperty("JTextField.placeholderText", "Quick search campus locations...");
        headerSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleHeaderSearch();
                }
            }
        });

        JButton btnHeaderSearch = UiUtils.createPrimaryButton("Search");
        btnHeaderSearch.setFont(Theme.FONT_SMALL_BOLD);
        btnHeaderSearch.addActionListener(e -> handleHeaderSearch());

        searchBox.add(headerSearchField, BorderLayout.CENTER);
        searchBox.add(btnHeaderSearch, BorderLayout.EAST);

        // Institutional Badge
        JPanel instBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        instBadge.setBackground(new Color(241, 245, 249));
        instBadge.setBorder(new EmptyBorder(2, 10, 2, 10));

        JLabel instIcon = new JLabel("🏛️");
        JLabel instName = new JLabel("Alliance University");
        instName.setFont(Theme.FONT_SMALL_BOLD);
        instName.setForeground(Theme.PRIMARY);

        instBadge.add(instIcon);
        instBadge.add(instName);

        rightHeader.add(searchBox);
        rightHeader.add(instBadge);

        header.add(rightHeader, BorderLayout.EAST);
        return header;
    }

    private void handleHeaderSearch() {
        String query = headerSearchField.getText().trim();
        if (query.isEmpty()) return;

        // Check if there is an exact or single strong match
        List<CampusLocation> matches = CampusDataManager.getInstance().search(query, null);
        if (matches.size() == 1) {
            // Jump directly to map and highlight
            showCampusMapWithLocation(matches.get(0).getId());
        } else {
            // Navigate to search panel with query
            showSearchWithQuery(query);
        }
    }

    private void initViews() {
        dashboardPanel = new DashboardPanel(this);
        campusMapPanel = new CampusMapPanel();
        searchPanel = new LocationSearchPanel(this);
        managementPanel = new LocationManagementPanel(this);
        aboutPanel = new AboutPanel();

        contentCards.add(dashboardPanel, VIEW_DASHBOARD);
        contentCards.add(campusMapPanel, VIEW_MAP);
        contentCards.add(searchPanel, VIEW_SEARCH);
        contentCards.add(managementPanel, VIEW_MANAGEMENT);
        contentCards.add(aboutPanel, VIEW_ABOUT);
    }

    private void switchView(String viewKey, String title) {
        currentView = viewKey;
        headerPageTitle.setText(title);
        cardLayout.show(contentCards, viewKey);

        // Update sidebar button states
        for (JButton btn : sidebarButtons.values()) {
            btn.repaint();
        }
    }

    // --- NavigationController Implementation ---

    @Override
    public void showDashboard() {
        switchView(VIEW_DASHBOARD, "Campus Dashboard Overview");
    }

    @Override
    public void showCampusMap() {
        switchView(VIEW_MAP, "Alliance University — Interactive Schematic Campus Map");
        SwingUtilities.invokeLater(() -> campusMapPanel.resetView());
    }

    @Override
    public void showCampusMapWithLocation(String locationId) {
        switchView(VIEW_MAP, "Campus Map — Selected Location");
        SwingUtilities.invokeLater(() -> campusMapPanel.centerOnLocation(locationId));
    }

    @Override
    public void showSearch() {
        switchView(VIEW_SEARCH, "Find a Campus Location");
    }

    @Override
    public void showSearchWithQuery(String query) {
        switchView(VIEW_SEARCH, "Location Search Results");
        if (searchPanel != null) {
            searchPanel.setSearchQuery(query);
        }
    }

    @Override
    public void showManagement() {
        switchView(VIEW_MANAGEMENT, "Manage Campus Locations & Coordinates");
    }

    @Override
    public void showAbout() {
        switchView(VIEW_ABOUT, "About Project — CampusNav Alliance University");
    }
}
