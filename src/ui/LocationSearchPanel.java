package ui;

import data.CampusDataManager;
import model.CampusLocation;
import model.Category;
import util.Theme;
import util.UiUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Search and exploration panel for locating Alliance University buildings,
 * facilities, and departments with real-time filtering and category chips.
 */
public class LocationSearchPanel extends JPanel implements CampusDataManager.DataChangeListener {

    private final NavigationController navigation;
    private final CampusDataManager dataManager;

    private JTextField searchField;
    private Category currentCategoryFilter = null; // null means 'All'
    private JPanel resultsContainer;
    private JLabel resultsCountLabel;

    public LocationSearchPanel(NavigationController navigation) {
        this.navigation = navigation;
        this.dataManager = CampusDataManager.getInstance();
        this.dataManager.addDataChangeListener(this);

        setLayout(new BorderLayout());
        setBackground(Theme.BG_LIGHT);

        // Top Filter Bar
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(24, 28, 16, 28));

        // Header Title
        JLabel title = new JLabel("Find a Location");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_MAIN);

        JLabel sub = new JLabel("Search buildings, classrooms, athletic grounds, and residential blocks");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_MUTED);

        topBar.add(title);
        topBar.add(Box.createVerticalStrut(4));
        topBar.add(sub);
        topBar.add(Box.createVerticalStrut(16));

        // Search Input Box
        JPanel searchBoxRow = new JPanel(new BorderLayout(10, 0));
        searchBoxRow.setOpaque(false);
        searchBoxRow.setMaximumSize(new Dimension(2000, 44));

        searchField = UiUtils.createTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Search by building name, category, or keyword (e.g. 'library', 'food', 'sports')...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                performSearch();
            }
        });

        JButton btnSearch = UiUtils.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> performSearch());

        JButton btnClear = UiUtils.createSecondaryButton("Clear");
        btnClear.addActionListener(e -> {
            searchField.setText("");
            currentCategoryFilter = null;
            performSearch();
        });

        searchBoxRow.add(searchField, BorderLayout.CENTER);

        JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchActions.setOpaque(false);
        searchActions.add(btnSearch);
        searchActions.add(btnClear);
        searchBoxRow.add(searchActions, BorderLayout.EAST);

        topBar.add(searchBoxRow);
        topBar.add(Box.createVerticalStrut(14));

        // Category Filter Chips
        topBar.add(createCategoryFilterBar());
        topBar.add(Box.createVerticalStrut(10));

        // Results summary line
        resultsCountLabel = new JLabel("Showing all locations");
        resultsCountLabel.setFont(Theme.FONT_SMALL_BOLD);
        resultsCountLabel.setForeground(Theme.TEXT_MUTED);
        topBar.add(resultsCountLabel);

        add(topBar, BorderLayout.NORTH);

        // Center: Scrollable Results
        resultsContainer = new JPanel();
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
        resultsContainer.setOpaque(false);
        resultsContainer.setBorder(new EmptyBorder(8, 28, 28, 28));

        JScrollPane scrollPane = new JScrollPane(resultsContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Theme.BG_LIGHT);
        scrollPane.getViewport().setBackground(Theme.BG_LIGHT);

        add(scrollPane, BorderLayout.CENTER);

        performSearch();
    }

    private JPanel createCategoryFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setOpaque(false);

        ButtonGroup group = new ButtonGroup();

        JToggleButton btnAll = createFilterChip("All Categories", null, true);
        group.add(btnAll);
        bar.add(btnAll);

        for (Category cat : Category.values()) {
            JToggleButton chip = createFilterChip(cat.getDisplayName(), cat, false);
            group.add(chip);
            bar.add(chip);
        }

        return bar;
    }

    private JToggleButton createFilterChip(String label, Category category, boolean selected) {
        JToggleButton chip = new JToggleButton(label, selected);
        chip.setFont(Theme.FONT_SMALL_BOLD);
        chip.setFocusPainted(false);
        chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chip.setBorder(new EmptyBorder(6, 14, 6, 14));

        chip.addActionListener(e -> {
            currentCategoryFilter = category;
            performSearch();
        });
        return chip;
    }

    public void setSearchQuery(String query) {
        if (searchField != null) {
            searchField.setText(query);
            performSearch();
        }
    }

    public void performSearch() {
        String query = searchField.getText().trim();
        List<CampusLocation> results = dataManager.search(query, currentCategoryFilter);

        resultsContainer.removeAll();

        if (results.isEmpty()) {
            resultsCountLabel.setText("No locations match your query: \"" + query + "\"");
            resultsContainer.add(createEmptyState(query));
        } else {
            resultsCountLabel.setText(String.format("Found %d %s",
                    results.size(), (results.size() == 1 ? "location" : "locations")));

            for (CampusLocation loc : results) {
                resultsContainer.add(createResultCard(loc));
                resultsContainer.add(Box.createVerticalStrut(10));
            }
        }

        resultsContainer.revalidate();
        resultsContainer.repaint();
    }

    private JPanel createResultCard(CampusLocation loc) {
        JPanel card = UiUtils.createCardPanel(12);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(2000, 95));

        // Left Icon & Details
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JLabel lblName = new JLabel(loc.getName());
        lblName.setFont(Theme.FONT_SUBHEADER);
        lblName.setForeground(Theme.TEXT_MAIN);

        JLabel lblId = new JLabel("[" + loc.getId() + "]");
        lblId.setFont(Theme.FONT_SMALL);
        lblId.setForeground(Theme.TEXT_LIGHT);

        titleRow.add(lblName);
        titleRow.add(lblId);
        titleRow.add(UiUtils.createCategoryBadge(loc.getCategory()));

        JLabel lblDesc = new JLabel(loc.getDescription());
        lblDesc.setFont(Theme.FONT_BODY);
        lblDesc.setForeground(Theme.TEXT_MUTED);

        textPanel.add(titleRow);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(lblDesc);

        // Right Action: View on Map
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);

        JButton btnView = UiUtils.createPrimaryButton("🗺 Show on Map");
        btnView.setFont(Theme.FONT_SMALL_BOLD);
        btnView.addActionListener(e -> navigation.showCampusMapWithLocation(loc.getId()));

        actions.add(btnView);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);

        return card;
    }

    private JPanel createEmptyState(String query) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(40, 20, 40, 20));

        JLabel icon = new JLabel("🔍");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("No matching campus locations found");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tip = new JLabel("Try searching with different keywords like 'Engineering', 'Food', 'Ground', or clear the category filter.");
        tip.setFont(Theme.FONT_BODY);
        tip.setForeground(Theme.TEXT_MUTED);
        tip.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnReset = UiUtils.createSecondaryButton("Show All Locations");
        btnReset.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnReset.addActionListener(e -> {
            searchField.setText("");
            currentCategoryFilter = null;
            performSearch();
        });

        panel.add(icon);
        panel.add(Box.createVerticalStrut(12));
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(tip);
        panel.add(Box.createVerticalStrut(18));
        panel.add(btnReset);

        return panel;
    }

    @Override
    public void onDataChanged() {
        performSearch();
    }
}
