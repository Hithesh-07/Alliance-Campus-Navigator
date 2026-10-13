package model;

import java.util.Objects;

/**
 * Model representing a physical or functional location on the Alliance University campus.
 */
public class CampusLocation {
    private String id;
    private String name;
    private Category category;
    private String description;

    public CampusLocation() {
        this("", "", Category.OTHER, "");
    }

    public CampusLocation(String id, String name, Category category, String description) {
        this.id = id != null ? id.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.category = category != null ? category : Category.OTHER;
        this.description = description != null ? description.trim() : "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id.trim() : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category != null ? category : Category.OTHER;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CampusLocation that = (CampusLocation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + category.getDisplayName() + ")";
    }
}
