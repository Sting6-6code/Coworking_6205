package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import model.Space;
import model.Booking;
import service.SpaceService;
import service.SortingService;
import service.SortingService.SortMode;
import util.BookingDataUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AdminSpaceController {

    // ========================== Enum Definition ==========================
    private enum ViewMode { BUILDING_OVERVIEW, FLOOR_VIEW }

    // ========================== FXML Elements ==========================
    @FXML private ComboBox<String> cbTypeFilter;
    @FXML private Spinner<Integer> spCapMin;
    @FXML private TextField txtSelectedBuilding;
    @FXML private ComboBox<String> cbFloorFilter;
    @FXML private ComboBox<String> cbSort;
    @FXML private AnchorPane canvas;
    @FXML private Label lblStatus;

    // ========================== Service & Data ==========================
    private final SpaceService spaceService = new SpaceService();
    private SortMode sortMode = SortMode.DEFAULT;

    private ViewMode currentMode = ViewMode.BUILDING_OVERVIEW;
    private String selectedBuilding = null;
    private String selectedFloor = null;

    private List<Space> allSpaces = new ArrayList<>();
    private Map<String, Map<String, List<Space>>> buildingFloorMap = new HashMap<>();

    // ========================== Initialization ==========================
    @FXML
    public void initialize() {
        spaceService.loadSpacesFromCSV();
        allSpaces = spaceService.getAllSpaces();
        buildingFloorMap = spaceService.getBuildingFloorMap();

        initFilters();
        //initBuildingSelector();
        initSorting();
        renderBuildingOverview();

        canvas.setOnScroll(e -> {
            double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            double newScale = Math.min(2.0, Math.max(0.5, canvas.getScaleX() * factor));
            canvas.setScaleX(newScale);
            canvas.setScaleY(newScale);
        });
    }

    // ========================== Filter & Sort ==========================
    private void initSorting() {
        cbSort.getItems().addAll("Default", "Most Booked", "Highest Revenue");
        cbSort.getSelectionModel().select("Default");
        cbSort.valueProperty().addListener((o, oldVal, newVal) -> {
            if (newVal == null) return;
            if (newVal.equals("Most Booked")) sortMode = SortMode.BOOKINGS;
            else if (newVal.equals("Highest Revenue")) sortMode = SortMode.REVENUE;
            else sortMode = SortMode.DEFAULT;
            renderBuildingOverview();
        });
    }

    private void initFilters() {
        cbTypeFilter.getItems().setAll("All", "room", "desk", "office", "conference", "event");
        cbTypeFilter.getSelectionModel().select("All");
        spCapMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));

        cbTypeFilter.valueProperty().addListener((o, oldVal, newVal) -> render());
        spCapMin.valueProperty().addListener((o, oldVal, newVal) -> render());
    }

//    private void initBuildingSelector() {
//        cbBuildingFilter.getItems().setAll("A", "B", "C", "D", "E");
//        cbBuildingFilter.valueProperty().addListener((o, oldVal, newVal) -> {
//            selectedBuilding = newVal;
//            selectedFloor = null;
//            currentMode = ViewMode.FLOOR_VIEW;
//            refreshFloorOptions();
//            render();
//        });
//    }

    private void refreshFloorOptions() {
        cbFloorFilter.getItems().clear();
        if (selectedBuilding == null || !buildingFloorMap.containsKey(selectedBuilding)) return;
        List<String> floors = new ArrayList<>(buildingFloorMap.get(selectedBuilding).keySet());
        Collections.sort(floors);
        cbFloorFilter.getItems().addAll(floors);
        if (!floors.isEmpty()) {
            cbFloorFilter.setValue(floors.get(0));
            selectedFloor = floors.get(0);
        }
        cbFloorFilter.valueProperty().addListener((o, oldVal, newVal) -> {
            selectedFloor = newVal;
            render();
        });
    }

    // ========================== Add New Space Function ==========================
    @FXML
    private void showAddSpaceDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add New Space");

        // Request load CSS
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("../application/application.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-card");

        // Custom Buttons
        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, cancelButton);

        // ====== GridPane Layout Beautification ======
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.getStyleClass().add("dialog-grid");

        TextField tfName = new TextField();
        tfName.getStyleClass().add("dialog-input");

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("room", "desk", "office", "conference", "event");
        cbType.getStyleClass().add("dialog-input");

        ComboBox<String> cbBuilding = new ComboBox<>();
        cbBuilding.getItems().addAll("A", "B", "C", "D", "E");
        cbBuilding.getStyleClass().add("dialog-input");

        ComboBox<String> cbFloor = new ComboBox<>();
        cbFloor.getItems().addAll("1", "2", "3");
        cbFloor.getStyleClass().add("dialog-input");

        Spinner<Integer> spCapacity = new Spinner<>(1, 100, 1);
        spCapacity.getStyleClass().add("dialog-input");

        // Add form
        grid.add(new Label("Name:"), 0, 0);
        grid.add(tfName, 1, 0);

        grid.add(new Label("Type:"), 0, 1);
        grid.add(cbType, 1, 1);

        grid.add(new Label("Building:"), 0, 2);
        grid.add(cbBuilding, 1, 2);

        grid.add(new Label("Floor:"), 0, 3);
        grid.add(cbFloor, 1, 3);

        grid.add(new Label("Capacity:"), 0, 4);
        grid.add(spCapacity, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Result conversion
        dialog.setResultConverter(button -> {
            if (button == addButton) {
                if (tfName.getText().isBlank() || cbType.getValue() == null ||
                        cbBuilding.getValue() == null || cbFloor.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Incomplete Info", "Please fill in all fields.");
                    return null;
                }
                Map<String, String> map = new HashMap<>();
                map.put("name", tfName.getText());
                map.put("type", cbType.getValue());
                map.put("building", cbBuilding.getValue());
                map.put("floor", cbFloor.getValue());
                map.put("capacity", String.valueOf(spCapacity.getValue()));
                return map;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::handleAddSpace);
    }


    private void handleAddSpace(Map<String, String> input) {
        try {
            int maxId = allSpaces.stream()
                    .mapToInt(s -> {
                        try { return Integer.parseInt(s.getId()); }
                        catch (NumberFormatException e) { return 100; }
                    }).max().orElse(120);
            String id = String.valueOf(maxId + 1);
            String spaceId = generateRandomCode(6);

            Space s = new Space(
                    id,
                    input.get("name"),
                    input.get("floor"),
                    spaceId,
                    input.get("type"),
                    input.get("building"),
                    Integer.parseInt(input.get("capacity")),
                    "available",
                    12,
                    0, 0, 0, 0
            );

            spaceService.addSpace(s);
            setStatus("New space added: " + s.getName());
            spaceService.loadSpacesFromCSV();
            allSpaces = spaceService.getAllSpaces();
            buildingFloorMap = spaceService.getBuildingFloorMap();
            renderBuildingOverview();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add new space.");
        }
    }

    private String generateRandomCode(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }

    // ========================== Render Logic ==========================
    private void render() {
        canvas.getChildren().clear();
        if (currentMode == ViewMode.BUILDING_OVERVIEW) renderBuildingOverview();
        else renderFloorLayout();
    }

    private void renderBuildingOverview() {
        canvas.getChildren().clear();
        setStatus("Displaying building overview...");
        Map<String, Long> bookingCounts = spaceService.getBookingCountByBuilding();
        Map<String, Double> revenues = spaceService.getRevenueByBuilding();
        List<String> buildings = SortingService.sortBuildings(sortMode, bookingCounts, revenues);

        double xStart = 80, y = 100, size = 120, gap = 150;
        for (int i = 0; i < buildings.size(); i++) {
            String b = buildings.get(i);
            StackPane tile = new StackPane();
            tile.setPrefSize(size, size);
            tile.relocate(xStart + i * gap, y);
            tile.getStyleClass().addAll("space-tile", "space-available");

            String info = "Building " + b +
                    "\nBookings: " + bookingCounts.getOrDefault(b, 0L) +
                    "\nRevenue: " + String.format("%.2f", revenues.getOrDefault(b, 0.0));
            Tooltip.install(tile, new Tooltip(info));

            tile.getChildren().add(new Text("Building " + b));
            tile.setOnMouseClicked(e -> {
                selectedBuilding = b;
                txtSelectedBuilding.setText("Building " + b);
                selectedFloor = null;
                currentMode = ViewMode.FLOOR_VIEW;
                refreshFloorOptions();
                render();
            });

            if (i < 3 && sortMode != SortMode.DEFAULT)
                tile.setStyle("-fx-border-color: gold; -fx-border-width: 3;");
            canvas.getChildren().add(tile);
        }
        setStatus("Viewing " + buildings.size() + " buildings.");
    }

    private void renderFloorLayout() {
        if (selectedBuilding == null || selectedFloor == null) {
            setStatus("Please select a building and floor.");
            return;
        }
        List<Space> spaces = buildingFloorMap.get(selectedBuilding).get(selectedFloor);
        if (spaces == null) {
            setStatus("No spaces found for this floor.");
            return;
        }

        String typeFilter = cbTypeFilter.getValue();
        int capMin = spCapMin.getValue();

        Pane floorPane = new Pane();
        floorPane.relocate(50, 50);
        floorPane.setPrefSize(700, 450);
        floorPane.getStyleClass().add("floor-pane");
        canvas.getChildren().add(floorPane);

        int cols = 4;
        int rows = Math.max(1, (int) Math.ceil(spaces.size() / 4.0));
        double cellW = 700 / cols;
        double cellH = 450 / rows;

        for (int i = 0; i < spaces.size(); i++) {
            Space s = spaces.get(i);
            if (!typeFilter.equals("All") && !s.getType().equalsIgnoreCase(typeFilter)) continue;
            if (s.getCapacity() < capMin) continue;

            StackPane tile = createAdminSpaceTile(s);
            tile.setPrefSize(cellW - 16, cellH - 16);
            tile.setLayoutX((i % cols) * cellW + 8);
            tile.setLayoutY((i / cols) * cellH + 8);
            floorPane.getChildren().add(tile);
        }
    }

    private StackPane createAdminSpaceTile(Space s) {
        StackPane box = new StackPane();
        box.getStyleClass().add("space-tile");
        LocalDate today = LocalDate.now();
        List<Booking> bookings = BookingDataUtil.getBookingsBySpaceIdAndDate(s.getSpaceId(), today);

        if (s.getStatus().equalsIgnoreCase("maintenance")) box.getStyleClass().add("space-maint");
        else if (bookings.isEmpty()) box.getStyleClass().add("space-available");
        else box.getStyleClass().add("space-booked");

        box.getChildren().add(new Text(s.getName() + "\n" + s.getType()));
        String tooltipText = "Space: " + s.getName() +
                "\nType: " + s.getType() +
                "\nCapacity: " + s.getCapacity() +
                "\nStatus: " + s.getStatus();
        if (!s.getStatus().equalsIgnoreCase("maintenance"))
            tooltipText += "\n\nToday bookings:\n" +
                    (bookings.isEmpty() ? "None"
                            : bookings.stream().map(b -> b.getStartTime() + " - " + b.getEndTime())
                            .collect(Collectors.joining("\n")));
        else tooltipText += "\n\nThis space is under maintenance.";
        Tooltip.install(box, new Tooltip(tooltipText));

        if (!s.getStatus().equalsIgnoreCase("maintenance"))
            box.setOnMouseClicked(e -> showBookingDetailDialog(s, bookings));
        return box;
    }

    private void showBookingDetailDialog(Space s, List<Booking> bookings) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Booking Info");
        a.setHeaderText("Bookings for " + s.getName());
        a.setContentText(bookings.isEmpty()
                ? "No bookings today."
                : bookings.stream().map(b -> b.getStartTime() + " - " + b.getEndTime())
                .collect(Collectors.joining("\n")));
        a.showAndWait();
    }

    // ========================== Button Operations ==========================
    @FXML
    private void handleOverview() {
        currentMode = ViewMode.BUILDING_OVERVIEW;
        selectedBuilding = null;
        selectedFloor = null;
        renderBuildingOverview();
        setStatus("Viewing building overview.");
    }



    // ========================== Utility Methods ==========================
    private void setStatus(String s) { lblStatus.setText(s); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}
