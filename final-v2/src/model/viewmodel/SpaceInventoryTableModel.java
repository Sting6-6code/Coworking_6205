package model.viewmodel;

import javafx.beans.property.SimpleStringProperty;

/**
 * 空间库存的TableView视图模型
 * 用于在JavaFX TableView中展示空间库存数据
 * 包含：名称、位置、楼层、类型、价格、数量、可用数量
 */
public class SpaceInventoryTableModel {
    private final SimpleStringProperty name;
    private final SimpleStringProperty location;
    private final SimpleStringProperty floor;
    private final SimpleStringProperty type;
    private final SimpleStringProperty price;
    private final SimpleStringProperty quantity;
    private final SimpleStringProperty available;

    public SpaceInventoryTableModel(String name, String location, String floor, String type,
                                   String price, String quantity, String available) {
        this.name = new SimpleStringProperty(name);
        this.location = new SimpleStringProperty(location);
        this.floor = new SimpleStringProperty(floor);
        this.type = new SimpleStringProperty(type);
        this.price = new SimpleStringProperty(price);
        this.quantity = new SimpleStringProperty(quantity);
        this.available = new SimpleStringProperty(available);
    }

    // Name property
    public String getName() {
        return name.get();
    }

    public void setName(String val) {
        name.set(val);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    // Location property
    public String getLocation() {
        return location.get();
    }

    public void setLocation(String val) {
        location.set(val);
    }

    public SimpleStringProperty locationProperty() {
        return location;
    }

    // Floor property
    public String getFloor() {
        return floor.get();
    }

    public void setFloor(String val) {
        floor.set(val);
    }

    public SimpleStringProperty floorProperty() {
        return floor;
    }

    // Type property
    public String getType() {
        return type.get();
    }

    public void setType(String val) {
        type.set(val);
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    // Price property
    public String getPrice() {
        return price.get();
    }

    public void setPrice(String val) {
        price.set(val);
    }

    public SimpleStringProperty priceProperty() {
        return price;
    }

    // Quantity property
    public String getQuantity() {
        return quantity.get();
    }

    public void setQuantity(String val) {
        quantity.set(val);
    }

    public SimpleStringProperty quantityProperty() {
        return quantity;
    }

    // Available property
    public String getAvailable() {
        return available.get();
    }

    public void setAvailable(String val) {
        available.set(val);
    }

    public SimpleStringProperty availableProperty() {
        return available;
    }
}

