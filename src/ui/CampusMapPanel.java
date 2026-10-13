package ui;

import data.CampusDataManager;
import model.CampusLocation;
import model.Category;
import model.MapItem;
import util.Theme;
import util.UiUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.IOException;
import java.util.List;

/**
 * High-performance Java2D interactive schematic campus map for Alliance University.
 * Supports panning, zooming, building selection, building dragging, real-time persistence,
 * and architectural rendering of landmarks.
 */
public class CampusMapPanel extends JPanel implements CampusDataManager.DataChangeListener {

    private final CampusDataManager dataManager;

    // Viewport transform parameters
    private double zoom = 0.75;
    private double panX = 40;
    private double panY = 20;

    // Interaction state
    private String selectedLocationId = null;
    private MapItem draggingItem = null;
    private Point lastMousePos = null;
    private boolean isPanning = false;
    private boolean isDraggingBuilding = false;
    private double dragStartBuildingX = 0;
    private double dragStartBuildingY = 0;
    private boolean hasUnsavedChanges = false;

    // UI Overlay Components
    private final JPanel detailsPanel;
    private JLabel detailsNameLabel;
    private JPanel detailsCategoryHolder;
    private JTextArea detailsDescText;
    private JLabel detailsCoordLabel;
    private final JButton btnSaveLayout;
    private final JButton btnResetLayout;
    private final JPanel legendPanel;
    private boolean showLegend = true;

    // Campus Canvas Dimensions
    private static final int CANVAS_WIDTH = 1500;
    private static final int CANVAS_HEIGHT = 950;

    public CampusMapPanel() {
        this.dataManager = CampusDataManager.getInstance();
        this.dataManager.addDataChangeListener(this);

        setLayout(new BorderLayout());
        setBackground(Theme.BG_LIGHT);

        // Build floating UI controls and side panels
        detailsPanel = createDetailsCard();
        legendPanel = createLegendPanel();
        JPanel toolBar = createMapToolbar();

        // Top toolbar and floating overlay
        JPanel topOverlay = new JPanel(new BorderLayout());
        topOverlay.setOpaque(false);
        topOverlay.setBorder(new EmptyBorder(12, 16, 0, 16));
        topOverlay.add(toolBar, BorderLayout.WEST);

        // Status banner
        btnSaveLayout = UiUtils.createPrimaryButton("💾 Save Layout");
        btnSaveLayout.setToolTipText("Save current building positions to map_layout.csv");
        btnSaveLayout.addActionListener(e -> saveLayoutAction());

        btnResetLayout = UiUtils.createSecondaryButton("↺ Reset Layout");
        btnResetLayout.setToolTipText("Restore original schematic arrangement");
        btnResetLayout.addActionListener(e -> resetLayoutAction());

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionButtons.setOpaque(false);
        actionButtons.add(btnResetLayout);
        actionButtons.add(btnSaveLayout);
        topOverlay.add(actionButtons, BorderLayout.EAST);

        add(topOverlay, BorderLayout.NORTH);

        // Bottom area: Legend on left, details on right
        JPanel bottomOverlay = new JPanel(new BorderLayout());
        bottomOverlay.setOpaque(false);
        bottomOverlay.setBorder(new EmptyBorder(0, 16, 16, 16));
        bottomOverlay.add(legendPanel, BorderLayout.WEST);
        bottomOverlay.add(detailsPanel, BorderLayout.EAST);
        add(bottomOverlay, BorderLayout.SOUTH);

        setupMouseInteractions();
    }

    private void setupMouseInteractions() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
                Point2D worldPt = toWorld(e.getPoint());

                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Check if clicked inside a building (search from top of stack)
                    List<MapItem> items = dataManager.getAllMapItems();
                    MapItem hit = null;
                    for (int i = items.size() - 1; i >= 0; i--) {
                        MapItem item = items.get(i);
                        if (item.contains(worldPt.getX(), worldPt.getY())) {
                            hit = item;
                            break;
                        }
                    }

                    if (hit != null) {
                        selectLocation(hit.getId());
                        draggingItem = hit;
                        isDraggingBuilding = true;
                        dragStartBuildingX = hit.getX() - worldPt.getX();
                        dragStartBuildingY = hit.getY() - worldPt.getY();
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else {
                        // Clicked empty map terrain -> start pan
                        isPanning = true;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                } else if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                    isPanning = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDraggingBuilding && draggingItem != null) {
                    hasUnsavedChanges = true;
                    updateSaveButtonState();
                }
                isPanning = false;
                isDraggingBuilding = false;
                draggingItem = null;
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos == null) {
                    lastMousePos = e.getPoint();
                    return;
                }

                if (isDraggingBuilding && draggingItem != null) {
                    Point2D worldPt = toWorld(e.getPoint());
                    double newX = Math.max(20, Math.min(CANVAS_WIDTH - draggingItem.getWidth() - 20, worldPt.getX() + dragStartBuildingX));
                    double newY = Math.max(20, Math.min(CANVAS_HEIGHT - draggingItem.getHeight() - 20, worldPt.getY() + dragStartBuildingY));

                    draggingItem.setX(Math.round(newX));
                    draggingItem.setY(Math.round(newY));

                    updateDetailsCoords();
                    repaint();
                } else if (isPanning) {
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;
                    panX += dx;
                    panY += dy;
                    repaint();
                }

                lastMousePos = e.getPoint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomFactor = (e.getWheelRotation() < 0) ? 1.12 : 0.88;
                zoomAtPoint(e.getPoint(), zoomFactor);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    private void zoomAtPoint(Point point, double factor) {
        double newZoom = Math.max(0.35, Math.min(2.5, zoom * factor));
        if (Math.abs(newZoom - zoom) < 0.001) return;

        // Keep world point under cursor stable
        panX = point.x - (point.x - panX) * (newZoom / zoom);
        panY = point.y - (point.y - panY) * (newZoom / zoom);
        zoom = newZoom;
        repaint();
    }

    public Point2D toWorld(Point screenPt) {
        double wx = (screenPt.x - panX) / zoom;
        double wy = (screenPt.y - panY) / zoom;
        return new Point2D.Double(wx, wy);
    }

    public Point toScreen(double worldX, double worldY) {
        int sx = (int) Math.round(worldX * zoom + panX);
        int sy = (int) Math.round(worldY * zoom + panY);
        return new Point(sx, sy);
    }

    public void selectLocation(String id) {
        this.selectedLocationId = id;
        CampusLocation loc = dataManager.getLocation(id);
        MapItem item = dataManager.getMapItem(id);

        if (loc != null && item != null) {
            detailsPanel.setVisible(true);
            detailsNameLabel.setText(loc.getName());
            detailsCategoryHolder.removeAll();
            detailsCategoryHolder.add(UiUtils.createCategoryBadge(loc.getCategory()));
            detailsCategoryHolder.revalidate();
            detailsDescText.setText(loc.getDescription());
            updateDetailsCoords();
        } else {
            detailsPanel.setVisible(false);
        }
        repaint();
    }

    public void centerOnLocation(String id) {
        MapItem item = dataManager.getMapItem(id);
        if (item == null) return;

        selectLocation(id);

        int viewW = getWidth();
        int viewH = getHeight();
        if (viewW <= 0) viewW = 1000;
        if (viewH <= 0) viewH = 700;

        zoom = 1.0;
        panX = (viewW / 2.0) - ((item.getX() + item.getWidth() / 2.0) * zoom);
        panY = (viewH / 2.0) - ((item.getY() + item.getHeight() / 2.0) * zoom);
        repaint();
    }

    public void resetView() {
        int viewW = getWidth();
        int viewH = getHeight();
        if (viewW <= 0) viewW = 1100;
        if (viewH <= 0) viewH = 750;

        double scaleX = (double) (viewW - 80) / CANVAS_WIDTH;
        double scaleY = (double) (viewH - 120) / CANVAS_HEIGHT;
        zoom = Math.max(0.45, Math.min(1.2, Math.min(scaleX, scaleY)));

        panX = (viewW - (CANVAS_WIDTH * zoom)) / 2.0;
        panY = (viewH - (CANVAS_HEIGHT * zoom)) / 2.0;
        repaint();
    }

    private void updateDetailsCoords() {
        if (selectedLocationId != null) {
            MapItem item = dataManager.getMapItem(selectedLocationId);
            if (item != null) {
                detailsCoordLabel.setText(String.format("Position: X: %d, Y: %d  |  Size: %d × %d",
                        (int) item.getX(), (int) item.getY(), (int) item.getWidth(), (int) item.getHeight()));
            }
        }
    }

    private void updateSaveButtonState() {
        if (hasUnsavedChanges) {
            btnSaveLayout.setText("💾 Save Layout *");
            btnSaveLayout.setBackground(Theme.WARNING);
        } else {
            btnSaveLayout.setText("💾 Save Layout");
            btnSaveLayout.setBackground(Theme.PRIMARY);
        }
    }

    private void saveLayoutAction() {
        try {
            dataManager.saveMapLayout();
            hasUnsavedChanges = false;
            updateSaveButtonState();
            JOptionPane.showMessageDialog(this,
                    "Campus map layout successfully saved to 'data/map_layout.csv'!",
                    "Layout Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save layout: " + ex.getMessage(),
                    "Error Saving Layout", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetLayoutAction() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Reset campus layout to default arrangement?\nThis will restore default building coordinates.",
                "Confirm Reset Layout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                dataManager.resetToDefaultLayout();
                hasUnsavedChanges = false;
                updateSaveButtonState();
                resetView();
                JOptionPane.showMessageDialog(this,
                        "Layout restored to original schematic defaults.",
                        "Layout Reset", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to reset layout: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Overlay UI Construction ---

    private JPanel createMapToolbar() {
        JPanel bar = UiUtils.createCardPanel(10);
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 4));

        JButton btnZoomIn = UiUtils.createSecondaryButton("➕ Zoom In");
        btnZoomIn.setFont(Theme.FONT_SMALL_BOLD);
        btnZoomIn.addActionListener(e -> zoomAtPoint(new Point(getWidth() / 2, getHeight() / 2), 1.25));

        JButton btnZoomOut = UiUtils.createSecondaryButton("➖ Zoom Out");
        btnZoomOut.setFont(Theme.FONT_SMALL_BOLD);
        btnZoomOut.addActionListener(e -> zoomAtPoint(new Point(getWidth() / 2, getHeight() / 2), 0.8));

        JButton btnFit = UiUtils.createSecondaryButton("⛶ Fit Campus");
        btnFit.setFont(Theme.FONT_SMALL_BOLD);
        btnFit.addActionListener(e -> resetView());

        JButton btnToggleLegend = UiUtils.createSecondaryButton("🗺 Legend");
        btnToggleLegend.setFont(Theme.FONT_SMALL_BOLD);
        btnToggleLegend.addActionListener(e -> {
            showLegend = !showLegend;
            legendPanel.setVisible(showLegend);
        });

        bar.add(btnZoomIn);
        bar.add(btnZoomOut);
        bar.add(btnFit);
        bar.add(btnToggleLegend);

        return bar;
    }

    private JPanel createLegendPanel() {
        JPanel panel = UiUtils.createCardPanel(10);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("CAMPUS LEGEND");
        title.setFont(Theme.FONT_SMALL_BOLD);
        title.setForeground(Theme.TEXT_MUTED);
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));

        for (Category cat : Category.values()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            row.setOpaque(false);

            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    UiUtils.applyQualityHints(g2);
                    g2.setColor(cat.getPrimaryColor());
                    g2.fillOval(1, 1, 12, 12);
                    g2.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(14, 14));
            dot.setOpaque(false);

            JLabel name = new JLabel(cat.getDisplayName());
            name.setFont(Theme.FONT_SMALL);
            name.setForeground(Theme.TEXT_MAIN);

            row.add(dot);
            row.add(name);
            panel.add(row);
        }

        return panel;
    }

    private JPanel createDetailsCard() {
        JPanel card = UiUtils.createCardPanel(12);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(340, 220));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setVisible(false);

        // Header with title and badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        detailsNameLabel = new JLabel("Building Details");
        detailsNameLabel.setFont(Theme.FONT_SUBHEADER);
        detailsNameLabel.setForeground(Theme.TEXT_MAIN);

        detailsCategoryHolder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        detailsCategoryHolder.setOpaque(false);

        top.add(detailsNameLabel, BorderLayout.NORTH);
        top.add(Box.createVerticalStrut(4), BorderLayout.CENTER);
        top.add(detailsCategoryHolder, BorderLayout.SOUTH);
        card.add(top, BorderLayout.NORTH);

        // Center: Description
        detailsDescText = new JTextArea();
        detailsDescText.setFont(Theme.FONT_SMALL);
        detailsDescText.setForeground(Theme.TEXT_MUTED);
        detailsDescText.setLineWrap(true);
        detailsDescText.setWrapStyleWord(true);
        detailsDescText.setEditable(false);
        detailsDescText.setOpaque(false);
        detailsDescText.setBorder(new EmptyBorder(8, 0, 8, 0));

        JScrollPane scroll = new JScrollPane(detailsDescText);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        // Bottom: Coordinate info & Action
        JPanel btm = new JPanel(new BorderLayout());
        btm.setOpaque(false);

        detailsCoordLabel = new JLabel("Position: X: 0, Y: 0");
        detailsCoordLabel.setFont(Theme.FONT_SMALL);
        detailsCoordLabel.setForeground(Theme.TEXT_LIGHT);
        btm.add(detailsCoordLabel, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        btnRow.setOpaque(false);

        JButton btnClose = UiUtils.createSecondaryButton("Dismiss");
        btnClose.setFont(Theme.FONT_SMALL_BOLD);
        btnClose.addActionListener(e -> {
            selectedLocationId = null;
            card.setVisible(false);
            repaint();
        });

        JButton btnCenter = UiUtils.createPrimaryButton("Center View");
        btnCenter.setFont(Theme.FONT_SMALL_BOLD);
        btnCenter.addActionListener(e -> {
            if (selectedLocationId != null) {
                centerOnLocation(selectedLocationId);
            }
        });

        btnRow.add(btnClose);
        btnRow.add(btnCenter);
        btm.add(btnRow, BorderLayout.SOUTH);

        card.add(btm, BorderLayout.SOUTH);
        return card;
    }

    // --- High-Quality Java2D Map Rendering ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        UiUtils.applyQualityHints(g2);

        // Fill component background
        g2.setColor(Theme.BG_WORKSPACE);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Apply World Transform
        AffineTransform oldTx = g2.getTransform();
        g2.translate(panX, panY);
        g2.scale(zoom, zoom);

        // 1. Draw Campus Canvas Base Terrain
        drawCampusBase(g2);

        // 2. Draw Road and Pedestrian Network
        drawRoadNetwork(g2);

        // 3. Draw Greenery, Trees, and Landscaping
        drawLandscaping(g2);

        // 4. Draw All Campus Buildings & Facilities
        drawBuildingsAndFacilities(g2);

        // Restore transform for Screen Space Overlays (Compass, Scale bar)
        g2.setTransform(oldTx);

        // 5. Screen-Space UI Elements
        drawCompassRose(g2);
        drawScaleBar(g2);

        g2.dispose();
    }

    private void drawCampusBase(Graphics2D g2) {
        // Base terrain
        g2.setColor(Theme.MAP_TERRAIN);
        g2.fill(new RoundRectangle2D.Double(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, 24, 24));

        // Outer boundary fence
        g2.setColor(new Color(180, 195, 175));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new RoundRectangle2D.Double(2, 2, CANVAS_WIDTH - 4, CANVAS_HEIGHT - 4, 24, 24));

        // Subtle terrain grid pattern
        g2.setColor(Theme.MAP_GRID);
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 12}, 0));
        for (int x = 100; x < CANVAS_WIDTH; x += 100) {
            g2.drawLine(x, 10, x, CANVAS_HEIGHT - 10);
        }
        for (int y = 100; y < CANVAS_HEIGHT; y += 100) {
            g2.drawLine(10, y, CANVAS_WIDTH - 10, y);
        }
    }

    private void drawRoadNetwork(Graphics2D g2) {
        // Walkways / Paved Pedestrian Paths (rendered first under roads)
        g2.setColor(Theme.MAP_PATH);
        g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D walkways = new Path2D.Double();
        // Path between Admin and Engineering
        walkways.moveTo(250, 240);
        walkways.lineTo(450, 140);
        // Path around Central rotunda
        walkways.moveTo(560, 420);
        walkways.lineTo(760, 420);
        walkways.lineTo(760, 290);
        // Path from Food Court to Sports Ground
        walkways.moveTo(540, 620);
        walkways.lineTo(380, 650);
        // Path to HOR 1 and HOR 2
        walkways.moveTo(640, 660);
        walkways.lineTo(670, 710);
        walkways.moveTo(750, 610);
        walkways.lineTo(860, 610);

        g2.draw(walkways);

        // Asphalt Road Network
        // 1. Road Base
        g2.setColor(Theme.MAP_ROAD);
        g2.setStroke(new BasicStroke(28f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D roads = new Path2D.Double();
        // West Entrance (Chandapura-Anekal Gateway Blvd)
        roads.moveTo(0, 300);
        roads.lineTo(240, 300);
        roads.curveTo(300, 300, 340, 260, 380, 240);

        // North loop connecting Engineering, Library and East Hostels
        roads.lineTo(550, 240);
        roads.lineTo(720, 240);
        roads.lineTo(910, 240);
        roads.lineTo(1080, 240);

        // Central Loop around School of Business
        roads.moveTo(380, 240);
        roads.lineTo(380, 450);
        roads.curveTo(380, 520, 450, 520, 540, 520);
        roads.lineTo(740, 520);
        roads.curveTo(830, 520, 830, 450, 830, 350);
        roads.lineTo(830, 240);

        // South arterial road connecting Food Court & Sports Complex
        roads.moveTo(540, 520);
        roads.lineTo(540, 690);
        roads.lineTo(840, 690);
        roads.lineTo(1040, 690);

        // Branch to Football ground
        roads.moveTo(380, 520);
        roads.lineTo(260, 570);
        roads.lineTo(260, 620);

        g2.draw(roads);

        // 2. White Centerline Striping
        g2.setColor(Theme.MAP_ROAD_MARK);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[]{8, 8}, 0));
        g2.draw(roads);

        // West Campus Entry Gateway Label
        g2.setColor(new Color(71, 85, 105));
        g2.setFont(Theme.FONT_SMALL_BOLD);
        g2.drawString("◀ MAIN GATEWAY (CHANDAPURA - ANEKAL RD)", 18, 280);

        // Parking stalls near Library & Admin
        drawParkingZone(g2, 380, 120, 55, 100);
        drawParkingZone(g2, 230, 180, 90, 45);
    }

    private void drawParkingZone(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(215, 222, 230));
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 6, 6));
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setStroke(new BasicStroke(1.2f));

        if (w > h) {
            for (int px = x + 10; px < x + w - 5; px += 14) {
                g2.drawLine(px, y + 4, px, y + h - 4);
            }
        } else {
            for (int py = y + 10; py < y + h - 5; py += 14) {
                g2.drawLine(x + 4, py, x + w - 4, py);
            }
        }
        g2.setColor(new Color(100, 116, 139));
        g2.setFont(Theme.FONT_MAP_SUB);
        g2.drawString("PARKING", x + (w / 2) - 18, y + (h / 2) + 3);
    }

    private void drawLandscaping(Graphics2D g2) {
        // Lush green lawns
        g2.setColor(Theme.MAP_LAWN);
        // Central garden plaza
        g2.fill(new Ellipse2D.Double(510, 390, 270, 110));
        // North landscaped park
        g2.fill(new RoundRectangle2D.Double(660, 40, 240, 50, 20, 20));
        // South courtyard park
        g2.fill(new RoundRectangle2D.Double(740, 580, 90, 90, 16, 16));

        // Stylized tree clusters (soft canopy with shadow)
        int[][] trees = {
                {130, 160}, {140, 330}, {170, 420}, {340, 170}, {340, 310},
                {690, 60}, {730, 55}, {770, 65}, {810, 55}, {850, 60},
                {490, 310}, {740, 310}, {500, 480}, {760, 480},
                {900, 120}, {910, 290}, {1090, 100}, {1080, 320},
                {150, 580}, {460, 620}, {480, 810}, {840, 720}, {1030, 520}
        };

        for (int[] t : trees) {
            drawTree(g2, t[0], t[1], 11);
        }
    }

    private void drawTree(Graphics2D g2, int cx, int cy, int radius) {
        // Soft drop shadow
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillOval(cx - radius + 2, cy - radius + 3, radius * 2, radius * 2);

        // Base foliage green
        g2.setColor(new Color(74, 138, 70));
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Highlight layer
        g2.setColor(new Color(110, 185, 95));
        g2.fillOval(cx - radius + 2, cy - radius + 2, (int) (radius * 1.4), (int) (radius * 1.4));

        // Core light dot
        g2.setColor(new Color(165, 215, 140));
        g2.fillOval(cx - 3, cy - 3, 5, 5);
    }

    private void drawBuildingsAndFacilities(Graphics2D g2) {
        List<MapItem> items = dataManager.getAllMapItems();

        for (MapItem item : items) {
            CampusLocation loc = dataManager.getLocation(item.getId());
            boolean isSelected = item.getId().equals(selectedLocationId);

            drawSingleItem(g2, item, loc, isSelected);
        }
    }

    private void drawSingleItem(Graphics2D g2, MapItem item, CampusLocation loc, boolean isSelected) {
        double x = item.getX();
        double y = item.getY();
        double w = item.getWidth();
        double h = item.getHeight();
        String shape = item.getShapeType();
        Category cat = (loc != null) ? loc.getCategory() : Category.OTHER;

        // 1. Soft Drop Shadow
        g2.setColor(new Color(0, 0, 0, isSelected ? 45 : 25));
        g2.fill(new RoundRectangle2D.Double(x + 4, y + 5, w, h, 14, 14));

        // 2. Render Based on Shape Type
        switch (shape) {
            case "FIELD":
                drawSportsField(g2, item, loc, isSelected);
                break;
            case "COURT":
                drawCourt(g2, item, loc, isSelected);
                break;
            case "ROUNDED_HUB":
                drawRotundaHub(g2, item, loc, isSelected);
                break;
            case "DOMED":
                drawDomedBuilding(g2, item, loc, isSelected);
                break;
            case "COURTYARD":
                drawCourtyardBuilding(g2, item, loc, isSelected);
                break;
            case "ADMIN":
                drawAdminBuilding(g2, item, loc, isSelected);
                break;
            default:
                drawStandardBuilding(g2, item, loc, isSelected);
                break;
        }

        // 3. Selection Highlight Halo & Handles
        if (isSelected) {
            g2.setColor(new Color(14, 165, 233, 200)); // Sky/Cyan glow
            g2.setStroke(new BasicStroke(3.0f));
            g2.draw(new RoundRectangle2D.Double(x - 3, y - 3, w + 6, h + 6, 16, 16));

            // Corner grab handles
            g2.setColor(Theme.PRIMARY);
            int hs = 8;
            g2.fillOval((int) (x - 4), (int) (y - 4), hs, hs);
            g2.fillOval((int) (x + w - 4), (int) (y - 4), hs, hs);
            g2.fillOval((int) (x - 4), (int) (y + h - 4), hs, hs);
            g2.fillOval((int) (x + w - 4), (int) (y + h - 4), hs, hs);

            // Floating Pin Indicator
            drawSelectionPin(g2, x + w / 2, y - 8);
        }
    }

    private void drawStandardBuilding(Graphics2D g2, MapItem item, CampusLocation loc, boolean isSelected) {
        double x = item.getX();
        double y = item.getY();
        double w = item.getWidth();
        double h = item.getHeight();
        Category cat = (loc != null) ? loc.getCategory() : Category.OTHER;

        // Base building body
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 10, 10));

        // Category accent header band
        g2.setColor(cat.getPrimaryColor());
        g2.fill(new RoundRectangle2D.Double(x, y, w, 8, 8, 8));

        // Border
        g2.setColor(isSelected ? Theme.PRIMARY : new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Double(x, y, w, h, 10, 10));

        // Building Label
        drawCenteredLabel(g2, loc != null ? loc.getName() : item.getId(), cat, x, y + 10, w, h - 10);
    }

    private void drawRotundaHub(Graphics2D g2, MapItem item, CampusLocation loc, boolean isSelected) {
        // Alliance School of Business / Central Academic Hub (Iconic circular rotunda with courtyard)
        double x = item.getX();
        double y = item.getY();
        double w = item.getWidth();
        double h = item.getHeight();
        Category cat = (loc != null) ? loc.getCategory() : Category.ACADEMIC;

        // Outer circular structure
        g2.setColor(new Color(254, 243, 199)); // Sandstone academic stone
        g2.fill(new Ellipse2D.Double(x, y, w, h));

        // Radiating wing petals / colonnade
        g2.setColor(new Color(251, 191, 36));
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(new Ellipse2D.Double(x + 4, y + 4, w - 8, h - 8));

        // Inner circular courtyard / amphitheater
        double innerW = w * 0.45;
        double innerH = h * 0.45;
        double ix = x + (w - innerW) / 2;
        double iy = y + (h - innerH) / 2;
        g2.setColor(new Color(220, 235, 215)); // Inner garden
        g2.fill(new Ellipse2D.Double(ix, iy, innerW, innerH));
        g2.setColor(new Color(180, 83, 9));
        g2.draw(new Ellipse2D.Double(ix, iy, innerW, innerH));

        // Outer border
        g2.setColor(isSelected ? Theme.PRIMARY : cat.getPrimaryColor());
        g2.setStroke(new BasicStroke(isSelected ? 2.5f : 1.8f));
        g2.draw(new Ellipse2D.Double(x, y, w, h));

        // Label
        drawCenteredLabel(g2, loc != null ? loc.getName() : item.getId(), cat, x, y, w, h);
    }

    private void drawDomedBuilding(Graphics2D g2, MapItem item, CampusLocation loc, boolean isSelected) {
        // Alliance University Library (Modern angled structure with prominent central dome)
        double x = item.getX();
        double y = item.getY();
        double w = item.getWidth();
        double h = item.getHeight();
        Category cat = (loc != null) ? loc.getCategory() : Category.ACADEMIC;

        // Modern base block
        g2.setColor(new Color(241, 245, 249));
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));

        // Angled architectural wings
        g2.setColor(new Color(203, 213, 225));
        g2.fill(new RoundRectangle2D.Double(x + 6, y + 6, w - 12, 16, 6, 6));

        // Prominent Central Dome (Turquoise / Patina copper dome)
        double dw = Math.min(w, h) * 0.48;
        double dh = dw;
        double dx = x + (w - dw) / 2;
        double dy = y + (h - dh) / 2 - 4;

        g2.setColor(new Color(45, 212, 191)); // Dome base
        g2.fill(new Ellipse2D.Double(dx, dy, dw, dh));
        g2.setColor(new Color(13, 148, 136)); // Dome ribs
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Double(dx, dy, dw, dh));
        g2.drawLine((int) (dx + dw / 2), (int) dy, (int) (dx + dw / 2), (int) (dy + dh));
        g2.drawLine((int) dx, (int) (dy + dh / 2), (int) (dx + dw), (int) (dy + dh / 2));

        // Border
        g2.setColor(isSelected ? Theme.PRIMARY : cat.getPrimaryColor());
        g2.setStroke(new BasicStroke(isSelected ? 2.5f : 1.5f));
        g2.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));

        // Label
        drawCenteredLabel(g2, loc != null ? loc.getName() : item.getId(), cat, x, y + h * 0.52, w, h * 0.45);
    }

    private void drawCourtyardBuilding(Graphics2D g2, MapItem item, CampusLocation loc, boolean isSelected) {
        // Quadrangle hostel / Engineering blocks with central open courtyard
        double x = item.getX();
        double y = item.getY();
        double w = item.getWidth();
        double h = item.getHeight();
        Category cat = (loc != null) ? loc.getCategory() : Category.RESIDENTIAL;

        // Outer block
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 8, 8));

        // Inner courtyard cutout
        double cw = w * 0.48;
        double ch = h * 0.42;
        double cx = x + (w - cw) / 2;
        double cy = y + (h - ch) / 2;

        g2.setColor(new Color(215, 235, 210)); // Green inner lawn
        g2.fill(new RoundRectangle2D.Double(cx, cy, cw, ch, 4, 4));
        g2.setColor(new Color(160, 190, 150));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Double(cx, cy, cw, ch, 4, 4));

        // Top accent bar
        g2.setColor(cat.getPrimaryColor());
        g2.fill(new RoundRectangle2D.Double(x, y, w, 6, 6, 6));

        // Border
        g2.setColor(isSelected ? Theme.PRIMARY : new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Double(x, y, w, h, 8, 8));

        drawCenteredLabel(g2, loc != null ? loc.getName() : item.getId(), cat, x, y + 6, w, (cy - y));
    }

    private void drawAdminBuilding(Graphics2D g2, MapItem item, CampusLocation loc, boolean isSelected) {
        // Administrative Block with portico wings and collegiate architecture
        double x = item.getX();
        double y = item.getY();
        double w = item.getWidth();
        double h = item.getHeight();
        Category cat = (loc != null) ? loc.getCategory() : Category.ADMINISTRATIVE;

        g2.setColor(new Color(253, 244, 255)); // Soft lilac white
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 10, 10));

        // Central entrance portico
        double pw = w * 0.38;
        double px = x + (w - pw) / 2;
        g2.setColor(new Color(243, 232, 255));
        g2.fill(new RoundRectangle2D.Double(px, y, pw, h, 6, 6));

        // Category bar
        g2.setColor(cat.getPrimaryColor());
        g2.fill(new RoundRectangle2D.Double(x, y, w, 8, 8, 8));

        // Border
        g2.setColor(isSelected ? Theme.PRIMARY : cat.getPrimaryColor());
        g2.setStroke(new BasicStroke(isSelected ? 2.5f : 1.5f));
        g2.draw(new RoundRectangle2D.Double(x, y, w, h, 10, 10));

        drawCenteredLabel(g2, loc != null ? loc.getName() : item.getId(), cat, x, y + 10, w, h - 10);
    }

    private void drawSportsField(Graphics2D g2, MapItem item, CampusLocation loc, boolean isSelected) {
        double x = item.getX();
        double y = item.getY();
        double w = item.getWidth();
        double h = item.getHeight();

        boolean isCricket = (loc != null && loc.getName().toLowerCase().contains("cricket"));

        if (isCricket) {
            // Cricket Oval
            g2.setColor(new Color(187, 227, 175)); // Green turf
            g2.fill(new Ellipse2D.Double(x, y, w, h));

            // Outer boundary rope
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f));
            g2.draw(new Ellipse2D.Double(x + 4, y + 4, w - 8, h - 8));

            // Inner 30-yard circle
            g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
            g2.draw(new Ellipse2D.Double(x + w * 0.18, y + h * 0.18, w * 0.64, h * 0.64));

            // Center clay pitch strip
            double pw = 16;
            double ph = h * 0.38;
            g2.setColor(new Color(217, 180, 126)); // Earth pitch
            g2.fill(new Rectangle2D.Double(x + (w - pw) / 2, y + (h - ph) / 2, pw, ph));
            g2.setColor(new Color(160, 120, 80));
            g2.draw(new Rectangle2D.Double(x + (w - pw) / 2, y + (h - ph) / 2, pw, ph));
        } else {
            // Football Ground Pitch
            g2.setColor(new Color(110, 180, 95));
            g2.fill(new RoundRectangle2D.Double(x, y, w, h, 8, 8));

            // Touchlines
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(new Rectangle2D.Double(x + 8, y + 8, w - 16, h - 16));

            // Halfway line & Center Circle
            double midX = x + w / 2;
            g2.drawLine((int) midX, (int) (y + 8), (int) midX, (int) (y + h - 8));
            g2.draw(new Ellipse2D.Double(midX - 22, (y + h / 2) - 22, 44, 44));

            // Penalty boxes
            double boxH = h * 0.45;
            double boxW = w * 0.14;
            double boxY = y + (h - boxH) / 2;
            g2.draw(new Rectangle2D.Double(x + 8, boxY, boxW, boxH));
            g2.draw(new Rectangle2D.Double(x + w - 8 - boxW, boxY, boxW, boxH));
        }

        // Label
        drawCenteredLabel(g2, loc != null ? loc.getName() : item.getId(), Category.SPORTS, x, y + h * 0.65, w, h * 0.32);
    }

    private void drawCourt(Graphics2D g2, MapItem item, CampusLocation loc, boolean isSelected) {
        double x = item.getX();
        double y = item.getY();
        double w = item.getWidth();
        double h = item.getHeight();

        boolean isVolleyball = (loc != null && loc.getName().toLowerCase().contains("volleyball"));

        if (isVolleyball) {
            // Sand / Clay volleyball court
            g2.setColor(new Color(245, 218, 170));
            g2.fill(new RoundRectangle2D.Double(x, y, w, h, 6, 6));

            // Court boundary
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new Rectangle2D.Double(x + 6, y + 6, w - 12, h - 12));

            // Net line
            double midX = x + w / 2;
            g2.setColor(new Color(15, 23, 42));
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine((int) midX, (int) (y + 4), (int) midX, (int) (y + h - 4));
        } else {
            // Hardcourt Basketball court (Acrylic Blue & Terracotta)
            g2.setColor(new Color(30, 64, 175)); // Blue court
            g2.fill(new RoundRectangle2D.Double(x, y, w, h, 6, 6));

            // White Court markings
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new Rectangle2D.Double(x + 6, y + 6, w - 12, h - 12));

            double midX = x + w / 2;
            g2.drawLine((int) midX, (int) (y + 6), (int) midX, (int) (y + h - 6));
            g2.draw(new Ellipse2D.Double(midX - 16, (y + h / 2) - 16, 32, 32));

            // Keys
            double keyW = w * 0.18;
            double keyH = h * 0.42;
            double keyY = y + (h - keyH) / 2;
            g2.setColor(new Color(234, 88, 12)); // Terracotta keys
            g2.fill(new Rectangle2D.Double(x + 6, keyY, keyW, keyH));
            g2.fill(new Rectangle2D.Double(x + w - 6 - keyW, keyY, keyW, keyH));
        }

        drawCenteredLabel(g2, loc != null ? loc.getName() : item.getId(), Category.SPORTS, x, y, w, h);
    }

    private void drawCenteredLabel(Graphics2D g2, String name, Category cat, double x, double y, double w, double h) {
        g2.setColor(Theme.TEXT_MAIN);
        g2.setFont(Theme.FONT_MAP_LABEL);

        FontMetrics fm = g2.getFontMetrics();

        // Split long names into 2 lines if needed
        String line1 = name;
        String line2 = "";

        if (fm.stringWidth(name) > (w - 12)) {
            int spaceIdx = name.lastIndexOf(' ', name.length() / 2 + 3);
            if (spaceIdx == -1) spaceIdx = name.indexOf(' ');
            if (spaceIdx > 0 && spaceIdx < name.length()) {
                line1 = name.substring(0, spaceIdx);
                line2 = name.substring(spaceIdx + 1);
            }
        }

        int textX1 = (int) (x + (w - fm.stringWidth(line1)) / 2);
        int textY1 = (int) (y + (h / 2) + (line2.isEmpty() ? 4 : -2));

        // Subdued shadow for crisp readability
        g2.setColor(new Color(255, 255, 255, 200));
        g2.drawString(line1, textX1 + 1, textY1 + 1);
        g2.setColor(Theme.TEXT_MAIN);
        g2.drawString(line1, textX1, textY1);

        if (!line2.isEmpty()) {
            int textX2 = (int) (x + (w - fm.stringWidth(line2)) / 2);
            int textY2 = textY1 + fm.getHeight() - 2;
            g2.setColor(new Color(255, 255, 255, 200));
            g2.drawString(line2, textX2 + 1, textY2 + 1);
            g2.setColor(Theme.TEXT_MAIN);
            g2.drawString(line2, textX2, textY2);
        }
    }

    private void drawSelectionPin(Graphics2D g2, double px, double py) {
        // Animated-style hovering target pin
        g2.setColor(new Color(225, 29, 72));
        g2.fillOval((int) px - 6, (int) py - 16, 12, 12);

        Path2D arrow = new Path2D.Double();
        arrow.moveTo(px - 5, py - 10);
        arrow.lineTo(px, py);
        arrow.lineTo(px + 5, py - 10);
        arrow.closePath();
        g2.fill(arrow);

        g2.setColor(Color.WHITE);
        g2.fillOval((int) px - 2, (int) py - 13, 4, 4);
    }

    private void drawCompassRose(Graphics2D g2) {
        int cx = getWidth() - 70;
        int cy = 70;

        g2.setColor(new Color(255, 255, 255, 220));
        g2.fillOval(cx - 24, cy - 24, 48, 48);
        g2.setColor(new Color(203, 213, 225));
        g2.drawOval(cx - 24, cy - 24, 48, 48);

        // North needle (Red)
        Path2D north = new Path2D.Double();
        north.moveTo(cx, cy);
        north.lineTo(cx - 5, cy);
        north.lineTo(cx, cy - 18);
        north.closePath();
        g2.setColor(new Color(239, 68, 68));
        g2.fill(north);

        // South needle (Navy)
        Path2D south = new Path2D.Double();
        south.moveTo(cx, cy);
        south.lineTo(cx + 5, cy);
        south.lineTo(cx, cy + 18);
        south.closePath();
        g2.setColor(new Color(30, 41, 59));
        g2.fill(south);

        g2.setFont(Theme.FONT_SMALL_BOLD);
        g2.setColor(new Color(239, 68, 68));
        g2.drawString("N", cx - 4, cy - 26);
    }

    private void drawScaleBar(Graphics2D g2) {
        int sx = 24;
        int sy = getHeight() - 24;

        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRoundRect(sx - 8, sy - 18, 120, 24, 6, 6);

        g2.setColor(Theme.TEXT_MUTED);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(sx, sy, sx + 100, sy);
        g2.drawLine(sx, sy - 4, sx, sy + 4);
        g2.drawLine(sx + 50, sy - 2, sx + 50, sy + 2);
        g2.drawLine(sx + 100, sy - 4, sx + 100, sy + 4);

        g2.setFont(Theme.FONT_MAP_SUB);
        g2.drawString("Schematic Scale ~ 100m", sx + 4, sy - 6);
    }

    // --- Listener Implementation ---

    @Override
    public void onDataChanged() {
        if (selectedLocationId != null) {
            CampusLocation loc = dataManager.getLocation(selectedLocationId);
            if (loc == null) {
                selectedLocationId = null;
                detailsPanel.setVisible(false);
            } else {
                selectLocation(selectedLocationId);
            }
        }
        repaint();
    }

    @Override
    public void onLocationSelected(String locationId) {
        centerOnLocation(locationId);
    }
}
