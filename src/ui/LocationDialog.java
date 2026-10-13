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

/**
 * Modal dialog for creating or updating a campus location and its map geometry.
 */
public class LocationDialog extends JDialog {

    private final CampusDataManager dataManager;
    private final boolean isEditMode;
    private boolean saved = false;

    private JTextField txtId;
    private JTextField txtName;
    private JComboBox<Category> cmbCategory;
    private JTextArea txtDesc;
    private JTextField txtX;
    private JTextField txtY;
    private JTextField txtW;
    private JTextField txtH;
    private JComboBox<String> cmbShape;

    public LocationDialog(Frame parent, CampusLocation location, MapItem mapItem) {
        super(parent, (location == null ? "Add New Campus Location" : "Edit Campus Location"), true);
        this.dataManager = CampusDataManager.getInstance();
        this.isEditMode = (location != null);

        setSize(540, 620);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Theme.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SIDEBAR_BG);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel(isEditMode ? "Edit Location Details" : "Register New Location");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Alliance University Campus Navigation System");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(new Color(203, 213, 225));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        mainPanel.add(header, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(createFieldLabel("Location ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtId = UiUtils.createTextField(15);
        if (isEditMode) {
            txtId.setText(location.getId());
            txtId.setEditable(false);
            txtId.setBackground(new Color(241, 245, 249));
        } else {
            txtId.setText(dataManager.generateNextId());
        }
        form.add(txtId, gbc);

        // Row 1: Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(createFieldLabel("Building / Location Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtName = UiUtils.createTextField(25);
        if (isEditMode) txtName.setText(location.getName());
        form.add(txtName, gbc);

        // Row 2: Category
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(createFieldLabel("Campus Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbCategory = new JComboBox<>(Category.values());
        cmbCategory.setFont(Theme.FONT_BODY);
        if (isEditMode) cmbCategory.setSelectedItem(location.getCategory());
        form.add(cmbCategory, gbc);

        // Row 3: Description
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(createFieldLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDesc = new JTextArea(4, 25);
        txtDesc.setFont(Theme.FONT_BODY);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        if (isEditMode) txtDesc.setText(location.getDescription());
        JScrollPane descScroll = new JScrollPane(txtDesc);
        descScroll.setBorder(BorderFactory.createLineBorder(Theme.CARD_BORDER));
        form.add(descScroll, gbc);

        // Separator: Map Geometry
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel geoTitle = new JLabel("Map Geometry & Architectural Styling");
        geoTitle.setFont(Theme.FONT_SUBHEADER);
        geoTitle.setForeground(Theme.PRIMARY);
        form.add(geoTitle, gbc);
        gbc.gridwidth = 1;

        // Row 5: Coordinates X, Y
        gbc.gridx = 0; gbc.gridy = 5;
        form.add(createFieldLabel("Map Coordinates (X, Y):"), gbc);
        gbc.gridx = 1;
        JPanel coordRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        coordRow.setOpaque(false);
        txtX = UiUtils.createTextField(5);
        txtY = UiUtils.createTextField(5);
        txtX.setText(String.valueOf(mapItem != null ? (int) mapItem.getX() : 600));
        txtY.setText(String.valueOf(mapItem != null ? (int) mapItem.getY() : 450));
        coordRow.add(new JLabel("X:")); coordRow.add(txtX);
        coordRow.add(new JLabel("Y:")); coordRow.add(txtY);
        form.add(coordRow, gbc);

        // Row 6: Dimensions Width, Height
        gbc.gridx = 0; gbc.gridy = 6;
        form.add(createFieldLabel("Size (Width × Height):"), gbc);
        gbc.gridx = 1;
        JPanel dimRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dimRow.setOpaque(false);
        txtW = UiUtils.createTextField(5);
        txtH = UiUtils.createTextField(5);
        txtW.setText(String.valueOf(mapItem != null ? (int) mapItem.getWidth() : 150));
        txtH.setText(String.valueOf(mapItem != null ? (int) mapItem.getHeight() : 80));
        dimRow.add(new JLabel("W:")); dimRow.add(txtW);
        dimRow.add(new JLabel("H:")); dimRow.add(txtH);
        form.add(dimRow, gbc);

        // Row 7: Shape Type
        gbc.gridx = 0; gbc.gridy = 7;
        form.add(createFieldLabel("Architectural Shape:"), gbc);
        gbc.gridx = 1;
        cmbShape = new JComboBox<>(new String[]{
                "RECTANGLE", "ROUNDED_HUB", "DOMED", "COURTYARD", "ADMIN", "FIELD", "COURT"
        });
        cmbShape.setFont(Theme.FONT_BODY);
        if (mapItem != null && mapItem.getShapeType() != null) {
            cmbShape.setSelectedItem(mapItem.getShapeType());
        }
        form.add(cmbShape, gbc);

        mainPanel.add(form, BorderLayout.CENTER);

        // Actions Bottom Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        bottomBar.setBackground(new Color(241, 245, 249));

        JButton btnCancel = UiUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UiUtils.createPrimaryButton(isEditMode ? "Save Changes" : "Create Location");
        btnSave.addActionListener(e -> saveAction());

        bottomBar.add(btnCancel);
        bottomBar.add(btnSave);
        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BODY_BOLD);
        lbl.setForeground(Theme.TEXT_MAIN);
        return lbl;
    }

    private void saveAction() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        Category cat = (Category) cmbCategory.getSelectedItem();
        String desc = txtDesc.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Location ID cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Location Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double x, y, w, h;
        try {
            x = Double.parseDouble(txtX.getText().trim());
            y = Double.parseDouble(txtY.getText().trim());
            w = Math.max(30, Double.parseDouble(txtW.getText().trim()));
            h = Math.max(25, Double.parseDouble(txtH.getText().trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Coordinates and Dimensions must be valid numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String shape = (String) cmbShape.getSelectedItem();

        try {
            CampusLocation loc = new CampusLocation(id, name, cat, desc);
            MapItem item = new MapItem(id, x, y, w, h, shape, "");

            if (isEditMode) {
                dataManager.updateLocationAndMapItem(loc, item);
            } else {
                dataManager.addLocation(loc, item);
            }

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving location: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
