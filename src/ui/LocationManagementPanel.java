package ui;

import data.CampusDataManager;
import model.CampusLocation;
import model.Category;
import model.MapItem;
import util.Theme;
import util.UiUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Management panel allowing administrators to add, modify, delete, and inspect
 * all university campus locations and their corresponding spatial footprints.
 */
public class LocationManagementPanel extends JPanel implements CampusDataManager.DataChangeListener {

    private final NavigationController navigation;
    private final CampusDataManager dataManager;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField filterField;

    public LocationManagementPanel(NavigationController navigation) {
        this.navigation = navigation;
        this.dataManager = CampusDataManager.getInstance();
        this.dataManager.addDataChangeListener(this);

        setLayout(new BorderLayout());
        setBackground(Theme.BG_LIGHT);

        // Header and Toolbar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(24, 28, 16, 28));

        // Title Row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel title = new JLabel("Manage Campus Locations");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_MAIN);

        JLabel sub = new JLabel("Create, update, or remove campus buildings and map coordinates");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_MUTED);

        JPanel titleText = new JPanel();
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));
        titleText.setOpaque(false);
        titleText.add(title);
        titleText.add(Box.createVerticalStrut(4));
        titleText.add(sub);
        titleRow.add(titleText, BorderLayout.WEST);

        // Action Buttons Row
        JPanel actionBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnRow.setOpaque(false);

        JButton btnAdd = UiUtils.createPrimaryButton("➕ Add Location");
        btnAdd.addActionListener(e -> addLocationAction());

        JButton btnEdit = UiUtils.createSecondaryButton("✏️ Edit");
        btnEdit.addActionListener(e -> editSelectedAction());

        JButton btnDelete = UiUtils.createDangerButton("🗑️ Delete");
        btnDelete.addActionListener(e -> deleteSelectedAction());

        JButton btnShowMap = UiUtils.createSecondaryButton("🗺️ Show on Map");
        btnShowMap.addActionListener(e -> showSelectedOnMap());

        actionBtnRow.add(btnAdd);
        actionBtnRow.add(btnEdit);
        actionBtnRow.add(btnDelete);
        actionBtnRow.add(btnShowMap);

        titleRow.add(actionBtnRow, BorderLayout.EAST);
        topContainer.add(titleRow);
        topContainer.add(Box.createVerticalStrut(16));

        // Quick Search within table
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);

        filterField = UiUtils.createTextField(25);
        filterField.putClientProperty("JTextField.placeholderText", "Filter locations in table...");
        filterField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String text = filterField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        searchBar.add(new JLabel("Quick Filter:"), BorderLayout.WEST);
        searchBar.add(filterField, BorderLayout.CENTER);

        topContainer.add(searchBar);
        add(topContainer, BorderLayout.NORTH);

        // Center Table
        initTable();
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
        tableScroll.setBackground(Theme.BG_LIGHT);
        tableScroll.getViewport().setBackground(Color.WHITE);

        add(tableScroll, BorderLayout.CENTER);

        refreshTableData();
    }

    private void initTable() {
        String[] columns = {"ID", "Building / Location Name", "Category", "Coordinates (X, Y)", "Size (W × H)", "Shape", "Description"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFont(Theme.FONT_BODY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(Theme.TEXT_MAIN);
        table.setGridColor(new Color(226, 232, 240));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(340);

        // Category cell renderer
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Category) {
                    Category cat = (Category) value;
                    lbl.setText(cat.getDisplayName());
                    lbl.setForeground(cat.getPrimaryColor());
                    lbl.setFont(Theme.FONT_SMALL_BOLD);
                }
                return lbl;
            }
        });

        // Double click row to edit
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    editSelectedAction();
                }
            }
        });
    }

    private void refreshTableData() {
        tableModel.setRowCount(0);
        List<CampusLocation> list = dataManager.getAllLocations();

        for (CampusLocation loc : list) {
            MapItem item = dataManager.getMapItem(loc.getId());
            String coords = (item != null) ? String.format("(%d, %d)", (int) item.getX(), (int) item.getY()) : "N/A";
            String size = (item != null) ? String.format("%d × %d", (int) item.getWidth(), (int) item.getHeight()) : "N/A";
            String shape = (item != null) ? item.getShapeType() : "RECTANGLE";

            tableModel.addRow(new Object[]{
                    loc.getId(),
                    loc.getName(),
                    loc.getCategory(),
                    coords,
                    size,
                    shape,
                    loc.getDescription()
            });
        }
    }

    private String getSelectedLocationId() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        int modelRow = table.convertRowIndexToModel(row);
        return (String) tableModel.getValueAt(modelRow, 0);
    }

    private void addLocationAction() {
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        LocationDialog dialog = new LocationDialog(topFrame, null, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshTableData();
        }
    }

    private void editSelectedAction() {
        String id = getSelectedLocationId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Please select a location from the table to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        CampusLocation loc = dataManager.getLocation(id);
        MapItem item = dataManager.getMapItem(id);

        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        LocationDialog dialog = new LocationDialog(topFrame, loc, item);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshTableData();
        }
    }

    private void deleteSelectedAction() {
        String id = getSelectedLocationId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Please select a location from the table to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        CampusLocation loc = dataManager.getLocation(id);
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + loc.getName() + "' [" + id + "]?\nThis will remove it from locations.csv and map_layout.csv.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                dataManager.deleteLocation(id);
                refreshTableData();
                JOptionPane.showMessageDialog(this, "Location deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting location: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showSelectedOnMap() {
        String id = getSelectedLocationId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Please select a location from the table to show on map.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        navigation.showCampusMapWithLocation(id);
    }

    @Override
    public void onDataChanged() {
        refreshTableData();
    }
}
