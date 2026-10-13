package model;

import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * Model representing the 2D spatial footprint and geometry of a campus location on the schematic map.
 */
public class MapItem {
    private String id;
    private double x;
    private double y;
    private double width;
    private double height;
    private String shapeType; // RECTANGLE, ROUNDED_HUB, DOMED, COURTYARD, FIELD, COURT, ADMIN
    private String customColorHex;

    public MapItem() {
        this("", 0, 0, 100, 60, "RECTANGLE", "");
    }

    public MapItem(String id, double x, double y, double width, double height) {
        this(id, x, y, width, height, "RECTANGLE", "");
    }

    public MapItem(String id, double x, double y, double width, double height, String shapeType, String customColorHex) {
        this.id = id != null ? id.trim() : "";
        this.x = x;
        this.y = y;
        this.width = Math.max(30, width);
        this.height = Math.max(25, height);
        this.shapeType = (shapeType != null && !shapeType.trim().isEmpty()) ? shapeType.trim() : "RECTANGLE";
        this.customColorHex = customColorHex != null ? customColorHex.trim() : "";
    }

    public MapItem(MapItem other) {
        this.id = other.id;
        this.x = other.x;
        this.y = other.y;
        this.width = other.width;
        this.height = other.height;
        this.shapeType = other.shapeType;
        this.customColorHex = other.customColorHex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id.trim() : "";
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = Math.max(30, width);
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = Math.max(25, height);
    }

    public String getShapeType() {
        return shapeType;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = (shapeType != null && !shapeType.trim().isEmpty()) ? shapeType.trim() : "RECTANGLE";
    }

    public String getCustomColorHex() {
        return customColorHex;
    }

    public void setCustomColorHex(String customColorHex) {
        this.customColorHex = customColorHex != null ? customColorHex.trim() : "";
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= (x + width) && py >= y && py <= (y + height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapItem mapItem = (MapItem) o;
        return Objects.equals(id, mapItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MapItem{" +
                "id='" + id + '\'' +
                ", pos=(" + (int)x + "," + (int)y + ")" +
                ", size=" + (int)width + "x" + (int)height +
                ", shape='" + shapeType + '\'' +
                '}';
    }
}
