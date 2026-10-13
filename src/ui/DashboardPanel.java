package ui;

import data.CampusDataManager;
import model.CampusLocation;
import model.Category;
import util.Theme;
import util.UiUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Modern CampusNav Dashboard presenting summary analytics,
 * campus map preview, and quick-access popular university landmarks.
 */
public class DashboardPanel extends JPanel implements CampusDataManager.DataChangeListener {

    private final NavigationController navigation;
    private final CampusDataManager dataManager;

    private JLabel lblTotalCount;
    private JLabel lblAcademicCount;
    private JLabel lblFoodCount;
    private JLabel lblSportsCount;
    private JLabel lblResidentialCount;

    private JPanel popularSpotsContainer;

    public DashboardPanel(NavigationController navigation) {
        this.navigation = navigation;
        this.dataManager = CampusDataManager.getInstance();
        this.dataManager.addDataChangeListener(this);

        setLayout(new BorderLayout());
        setBackground(Theme.BG_LIGHT);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Theme.BG_LIGHT);
        contentPanel.setBorder(new EmptyBorder(24, 28, 28, 28));

        // 1. Welcome Banner
        contentPanel.add(createWelcomeBanner());
        contentPanel.add(Box.createVerticalStrut(20));

        // 2. Metrics Analytics Cards
        contentPanel.add(createMetricsSection());
        contentPanel.add(Box.createVerticalStrut(24));

        // 3. Lower Section: Map Preview (Left) + Quick Access (Right)
        contentPanel.add(createLowerSection());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Theme.BG_LIGHT);
        scrollPane.getViewport().setBackground(Theme.BG_LIGHT);

        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createWelcomeBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.applyQualityHints(g2);
                // Gradient banner
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 23, 42),
                        getWidth(), 0, new Color(14, 116, 144)
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setLayout(new BorderLayout());
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(20, 24, 20, 24));
        banner.setMaximumSize(new Dimension(2000, 110));

        JPanel textPane = new JPanel();
        textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));
        textPane.setOpaque(false);

        JLabel title = new JLabel("Welcome to Alliance University CampusNav");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Central Campus, Chandapura - Anekal Main Road, Bengaluru, Karnataka 562106");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(new Color(203, 213, 225));

        textPane.add(title);
        textPane.add(Box.createVerticalStrut(6));
        textPane.add(sub);

        banner.add(textPane, BorderLayout.CENTER);

        JButton btnExplore = UiUtils.createButton("Open Interactive Map ➔", Theme.SIDEBAR_ACCENT, Theme.SIDEBAR_BG, 10);
        btnExplore.setFont(Theme.FONT_BODY_BOLD);
        btnExplore.addActionListener(e -> navigation.showCampusMap());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnExplore);
        banner.add(btnWrapper, BorderLayout.EAST);

        return banner;
    }

    private JPanel createMetricsSection() {
        JPanel grid = new JPanel(new GridLayout(1, 5, 14, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(2000, 110));

        lblTotalCount = new JLabel("0");
        lblAcademicCount = new JLabel("0");
        lblFoodCount = new JLabel("0");
        lblSportsCount = new JLabel("0");
        lblResidentialCount = new JLabel("0");

        grid.add(createMetricCard("Total Locations", lblTotalCount, "📍", Theme.PRIMARY, () -> navigation.showSearch()));
        grid.add(createMetricCard("Academic Blocks", lblAcademicCount, "🎓", Category.ACADEMIC.getPrimaryColor(), () -> navigation.showSearchWithQuery("Academic")));
        grid.add(createMetricCard("Food & Dining", lblFoodCount, "🍽️", Category.FOOD.getPrimaryColor(), () -> navigation.showSearchWithQuery("Food")));
        grid.add(createMetricCard("Sports Facilities", lblSportsCount, "⚽", Category.SPORTS.getPrimaryColor(), () -> navigation.showSearchWithQuery("Sports")));
        grid.add(createMetricCard("Hostels & Living", lblResidentialCount, "🏢", Category.RESIDENTIAL.getPrimaryColor(), () -> navigation.showSearchWithQuery("Residential")));

        return grid;
    }

    private JPanel createMetricCard(String title, JLabel countLabel, String icon, Color accent, Runnable onClick) {
        JPanel card = UiUtils.createCardPanel(12);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.FONT_SMALL_BOLD);
        lblTitle.setForeground(Theme.TEXT_MUTED);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        top.add(lblTitle, BorderLayout.WEST);
        top.add(lblIcon, BorderLayout.EAST);

        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        countLabel.setForeground(accent);

        card.add(top, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onClick != null) onClick.run();
            }
        });

        return card;
    }

    private JPanel createLowerSection() {
        JPanel split = new JPanel(new GridLayout(1, 2, 18, 0));
        split.setOpaque(false);
        split.setMaximumSize(new Dimension(2000, 480));

        // Left: Interactive Map Preview Card
        JPanel mapCard = UiUtils.createCardPanel(14);
        mapCard.setLayout(new BorderLayout());
        mapCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel mapHeader = new JPanel(new BorderLayout());
        mapHeader.setOpaque(false);
        JLabel mapTitle = new JLabel("Campus Schematic Map Preview");
        mapTitle.setFont(Theme.FONT_HEADER);
        mapTitle.setForeground(Theme.TEXT_MAIN);

        JLabel mapSub = new JLabel("Click to open full interactive navigation with drag & zoom");
        mapSub.setFont(Theme.FONT_SMALL);
        mapSub.setForeground(Theme.TEXT_MUTED);

        mapHeader.add(mapTitle, BorderLayout.NORTH);
        mapHeader.add(mapSub, BorderLayout.SOUTH);
        mapCard.add(mapHeader, BorderLayout.NORTH);

        // Clickable Mini Preview Banner
        JPanel previewCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.applyQualityHints(g2);

                // Mini terrain
                g2.setColor(Theme.MAP_TERRAIN);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // Roads schematic preview
                g2.setColor(Theme.MAP_ROAD);
                g2.setStroke(new BasicStroke(16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(20, getHeight() / 2, getWidth() / 3, getHeight() / 2);
                g2.drawOval(getWidth() / 3, getHeight() / 4, getWidth() / 3, getHeight() / 2);
                g2.drawLine(getWidth() * 2 / 3, getHeight() / 2, getWidth() - 20, getHeight() / 2);

                // Central Business School rotunda preview
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(new Color(254, 243, 199));
                g2.fillOval(cx - 35, cy - 35, 70, 70);
                g2.setColor(new Color(180, 83, 9));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(cx - 35, cy - 35, 70, 70);

                // Library preview
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(cx + 60, cy - 80, 70, 45, 6, 6);
                g2.setColor(new Color(45, 212, 191));
                g2.fillOval(cx + 80, cy - 70, 25, 25);

                // Football ground preview
                g2.setColor(new Color(110, 180, 95));
                g2.fillRoundRect(cx - 140, cy + 30, 90, 60, 6, 6);

                // Overlay message
                g2.setColor(new Color(15, 23, 42, 160));
                g2.fillRoundRect(cx - 110, cy - 18, 220, 36, 18, 18);

                g2.setColor(Color.WHITE);
                g2.setFont(Theme.FONT_BODY_BOLD);
                String prompt = "🔍 Click to Launch Full Map";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(prompt, cx - fm.stringWidth(prompt) / 2, cy + 5);

                g2.dispose();
            }
        };
        previewCanvas.setCursor(new Cursor(Cursor.HAND_CURSOR));
        previewCanvas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                navigation.showCampusMap();
            }
        });

        JPanel previewWrapper = new JPanel(new BorderLayout());
        previewWrapper.setOpaque(false);
        previewWrapper.setBorder(new EmptyBorder(14, 0, 0, 0));
        previewWrapper.add(previewCanvas, BorderLayout.CENTER);
        mapCard.add(previewWrapper, BorderLayout.CENTER);

        // Right: Popular Locations Quick-Access List
        JPanel popularCard = UiUtils.createCardPanel(14);
        popularCard.setLayout(new BorderLayout());
        popularCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel popularHeader = new JPanel(new BorderLayout());
        popularHeader.setOpaque(false);
        JLabel popTitle = new JLabel("Popular Campus Hubs");
        popTitle.setFont(Theme.FONT_HEADER);
        popTitle.setForeground(Theme.TEXT_MAIN);

        JLabel popSub = new JLabel("Frequently visited departments, academic buildings, and dining");
        popSub.setFont(Theme.FONT_SMALL);
        popSub.setForeground(Theme.TEXT_MUTED);

        popularHeader.add(popTitle, BorderLayout.NORTH);
        popularHeader.add(popSub, BorderLayout.SOUTH);
        popularCard.add(popularHeader, BorderLayout.NORTH);

        popularSpotsContainer = new JPanel();
        popularSpotsContainer.setLayout(new BoxLayout(popularSpotsContainer, BoxLayout.Y_AXIS));
        popularSpotsContainer.setOpaque(false);
        popularSpotsContainer.setBorder(new EmptyBorder(12, 0, 0, 0));

        JScrollPane popularScroll = new JScrollPane(popularSpotsContainer);
        popularScroll.setBorder(null);
        popularScroll.setOpaque(false);
        popularScroll.getViewport().setOpaque(false);
        popularCard.add(popularScroll, BorderLayout.CENTER);

        split.add(mapCard);
        split.add(popularCard);
        return split;
    }

    private void refreshData() {
        lblTotalCount.setText(String.valueOf(dataManager.getTotalLocationsCount()));
        lblAcademicCount.setText(String.valueOf(dataManager.getCountByCategory(Category.ACADEMIC)));
        lblFoodCount.setText(String.valueOf(dataManager.getCountByCategory(Category.FOOD)));
        lblSportsCount.setText(String.valueOf(dataManager.getCountByCategory(Category.SPORTS)));
        lblResidentialCount.setText(String.valueOf(dataManager.getCountByCategory(Category.RESIDENTIAL)));

        // Populate Popular Spots
        popularSpotsContainer.removeAll();
        List<CampusLocation> locs = dataManager.getAllLocations();
        int count = 0;

        for (CampusLocation loc : locs) {
            // Pick a mix of notable locations
            if (count >= 5) break;

            JPanel itemRow = new JPanel(new BorderLayout(10, 0));
            itemRow.setOpaque(false);
            itemRow.setBorder(new EmptyBorder(8, 6, 8, 6));

            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setOpaque(false);

            JLabel name = new JLabel(loc.getName());
            name.setFont(Theme.FONT_SUBHEADER);
            name.setForeground(Theme.TEXT_MAIN);

            JLabel desc = new JLabel(loc.getDescription());
            desc.setFont(Theme.FONT_SMALL);
            desc.setForeground(Theme.TEXT_MUTED);

            left.add(name);
            left.add(Box.createVerticalStrut(2));
            left.add(desc);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            right.setOpaque(false);
            right.add(UiUtils.createCategoryBadge(loc.getCategory()));

            JButton btnLocate = UiUtils.createSecondaryButton("Locate");
            btnLocate.setFont(Theme.FONT_SMALL_BOLD);
            btnLocate.addActionListener(e -> navigation.showCampusMapWithLocation(loc.getId()));
            right.add(btnLocate);

            itemRow.add(left, BorderLayout.CENTER);
            itemRow.add(right, BorderLayout.EAST);

            popularSpotsContainer.add(itemRow);
            popularSpotsContainer.add(new JSeparator(SwingConstants.HORIZONTAL));
            count++;
        }

        popularSpotsContainer.revalidate();
        popularSpotsContainer.repaint();
    }

    @Override
    public void onDataChanged() {
        refreshData();
    }
}
