package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import model.viewmodel.SpaceInventoryTableModel;

import java.io.*;

public class AdminBookingsController {

    @FXML private TableView<SpaceInventoryTableModel> bookingTable;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colName;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colLocation;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colFloor;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colType;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colPrice;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colQuantity;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colAvailable;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colEdit;

    private static final String DATA_FILE = "data/booking.csv";
    private ObservableList<SpaceInventoryTableModel> bookingList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadDataFromCSV();
    }

    private void setupTable() {
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colLocation.setCellValueFactory(data -> data.getValue().locationProperty());
        colFloor.setCellValueFactory(data -> data.getValue().floorProperty());
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colPrice.setCellValueFactory(data -> data.getValue().priceProperty());
        colQuantity.setCellValueFactory(data -> data.getValue().quantityProperty());
        colAvailable.setCellValueFactory(data -> data.getValue().availableProperty());

        // Edit button
        colEdit.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✎");

            {
                editBtn.setOnAction(event -> {
                    SpaceInventoryTableModel booking = getTableView().getItems().get(getIndex());
                    openEditDialog(booking);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        bookingTable.setItems(bookingList);
    }

    private void openEditDialog(SpaceInventoryTableModel booking) {
        Dialog<SpaceInventoryTableModel> dialog = new Dialog<>();
        dialog.setTitle("Edit Booking");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(booking.getName());
        TextField locationField = new TextField(booking.getLocation());
        TextField floorField = new TextField(booking.getFloor());
        TextField typeField = new TextField(booking.getType());
        TextField priceField = new TextField(booking.getPrice());
        TextField quantityField = new TextField(booking.getQuantity());
        TextField availableField = new TextField(booking.getAvailable());

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1); grid.add(locationField, 1, 1);
        grid.add(new Label("Floor Number:"), 0, 2); grid.add(floorField, 1, 2);
        grid.add(new Label("Type:"), 0, 3); grid.add(typeField, 1, 3);
        grid.add(new Label("Price:"), 0, 4); grid.add(priceField, 1, 4);
        grid.add(new Label("Quantity:"), 0, 5); grid.add(quantityField, 1, 5);
        grid.add(new Label("Available:"), 0, 6); grid.add(availableField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                booking.setName(nameField.getText());
                booking.setLocation(locationField.getText());
                booking.setFloor(floorField.getText());
                booking.setType(typeField.getText());
                booking.setPrice(priceField.getText());
                booking.setQuantity(quantityField.getText());
                booking.setAvailable(availableField.getText());
                return booking;
            }
            return null;
        });

        dialog.showAndWait();
        bookingTable.refresh();
    }

    @FXML
    private void createBooking() {
        Dialog<SpaceInventoryTableModel> dialog = new Dialog<>();
        dialog.setTitle("Create New Booking");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField locationField = new TextField();
        TextField floorField = new TextField();
        TextField typeField = new TextField();
        TextField priceField = new TextField();
        TextField quantityField = new TextField();

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1); grid.add(locationField, 1, 1);
        grid.add(new Label("Floor Number:"), 0, 2); grid.add(floorField, 1, 2);
        grid.add(new Label("Type:"), 0, 3); grid.add(typeField, 1, 3);
        grid.add(new Label("Price:"), 0, 4); grid.add(priceField, 1, 4);
        grid.add(new Label("Quantity:"), 0, 5); grid.add(quantityField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String qty = quantityField.getText();
                return new SpaceInventoryTableModel(
                        nameField.getText(),
                        locationField.getText(),
                        floorField.getText(),
                        typeField.getText(),
                        priceField.getText(),
                        qty,
                        qty // available = quantity initially
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(booking -> {
            bookingList.add(booking);
            saveChanges(); // Auto save
        });
    }

    @FXML
    private void deleteBooking() {
        SpaceInventoryTableModel selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            bookingList.remove(selected);
        }
    }

    @FXML
    private void saveChanges() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (SpaceInventoryTableModel b : bookingList) {
                bw.write(b.getName() + "," + b.getLocation() + "," + b.getFloor() + "," +
                         b.getType() + "," + b.getPrice() + "," + b.getQuantity() + "," + b.getAvailable());
                bw.newLine();
            }
            showAlert("Saved", "Booking data saved successfully!");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void loadDataFromCSV() {
        bookingList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] data = line.split(",");
                    String name = data.length > 0 ? data[0] : "";
                    String location = data.length > 1 ? data[1] : "";
                    String floor = data.length > 2 ? data[2] : "";
                    String type = data.length > 3 ? data[3] : "";
                    String price = data.length > 4 ? data[4] : "";
                    String quantity = data.length > 5 ? data[5] : "";
                    String available = data.length > 6 ? data[6] : quantity; // If old file doesn't have available
                    bookingList.add(new SpaceInventoryTableModel(name, location, floor, type, price, quantity, available));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}
