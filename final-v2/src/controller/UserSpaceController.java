package controller;

import javafx.application.Platform;                         // *** NEW: Update UI on FX thread
import javafx.concurrent.Worker;                           // *** NEW: Listen to WebView loading status
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import model.Space;
import model.TimeSlot;
import service.SpaceService;
import datastructure.SpaceIndexDS;

import java.net.URL;
import java.util.List;

import netscape.javascript.JSObject;                      // *** NEW: JS and Java interaction

public class UserSpaceController {

    /* ======================== Status Enum ======================== */

    // *** MODIFIED: Truly use MAP_VIEW mode (Show map initially)
    private enum ViewMode { MAP_VIEW, BUILDING_OVERVIEW, FLOOR_VIEW }

    /* ======================== FXML Binding ======================== */

    @FXML private ComboBox<String> cbTypeFilter;
    @FXML private Spinner<Integer> spCapMin;
    @FXML private ComboBox<String> cbFloorFilter;
    @FXML private CheckBox chkOnlyAvailable;
    @FXML private TextField txtSelectedBuilding;
    @FXML private AnchorPane canvas;
    @FXML private Label lblStatus;

    // *** NEW: Map view WebView (Must have fx:id="mapView" in FXML)
    @FXML private WebView mapView;
    

    /* ======================== Controller Internal State ======================== */

    // *** MODIFIED: Initial mode changed to MAP_VIEW (Show map first, then enter floor view)
    private ViewMode currentMode = ViewMode.MAP_VIEW;
    private String selectedBuilding = null;
    private String selectedFloor = null;

    /* ======================== Service Layer ======================== */

    private final SpaceService spaceService = new SpaceService();
    private SpaceIndexDS index;

    /* ======================== Initialization ======================== */

    @FXML
    public void initialize() {

        /* 1. Load space data from CSV (Not done inside Controller) */
        spaceService.loadSpacesFromCSV();
        index = spaceService.getIndex();

        /* 2. Initialize filters */
        initFilters();

        /* 3. Initialize map view (Load building_map.html) */
        initMapView();                                     // *** NEW

        /* 4. Set default view mode to MAP_VIEW (Only show map, do not render canvas immediately) */
        setMode(ViewMode.MAP_VIEW);                        // *** NEW

        /* 5. Support zooming */
        canvas.setOnScroll(e -> {
            double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            canvas.setScaleX(canvas.getScaleX() * factor);
            canvas.setScaleY(canvas.getScaleY() * factor);
        });
    }

    /* ======================== Filters ======================== */

    private void initFilters() {

        cbTypeFilter.getItems().setAll("All", "room", "desk", "office", "conference", "event");
        cbTypeFilter.getSelectionModel().select("All");

        spCapMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0));

        cbFloorFilter.valueProperty().addListener((obs, o, n) -> {
            if (currentMode == ViewMode.FLOOR_VIEW) {
                selectedFloor = n;
                render();
            }
        });

        cbTypeFilter.valueProperty().addListener((obs, o, n) -> render());
        spCapMin.valueProperty().addListener((obs, o, n) -> render());
        chkOnlyAvailable.selectedProperty().addListener((obs, o, n) -> render());
    }

    /* ======================== View Mode Switching (New) ======================== */

    // *** NEW: Unified control for Map / Building / Floor view display & hide
    private void setMode(ViewMode mode) {
        this.currentMode = mode;

        if (mode == ViewMode.MAP_VIEW) {
            // Only show map, hide canvas
            if (mapView != null) {
                mapView.setVisible(true);
                mapView.setManaged(true);
            }
            if (canvas != null) {
                canvas.setVisible(false);
                canvas.setManaged(false);
            }
            setStatus("Click a building marker on the map.");
        } else {
            // Enter Building or Floor view: show canvas, hide map
            if (mapView != null) {
                mapView.setVisible(false);
                mapView.setManaged(false);
            }
            if (canvas != null) {
                canvas.setVisible(true);
                canvas.setManaged(true);
            }
            render();   // Re-render according to current mode
        }
    }

    /* ======================== Main Rendering Logic ======================== */

    private void render() {
        canvas.getChildren().clear();

        // *** MODIFIED: Explicitly distinguish three modes, do not render canvas in MAP_VIEW
        if (currentMode == ViewMode.BUILDING_OVERVIEW) {
            renderBuildingOverview();
        } else if (currentMode == ViewMode.FLOOR_VIEW) {
            renderFloorLayout();
        } else {
            // MAP_VIEW: Do nothing to avoid redundant operations
        }
    }

    /* ======================== Building Overview ======================== */

    private void renderBuildingOverview() {

        double xStart = 80, y = 100, size = 120, gap = 150;
        String[] buildings = { "A", "B", "C", "D", "E" };

        for (int i = 0; i < buildings.length; i++) {
            String b = buildings[i];

            StackPane tile = new StackPane();
            tile.setPrefSize(size, size);
            tile.relocate(xStart + i * gap, y);
            tile.getStyleClass().addAll("space-tile", "space-available");

            Text label = new Text("Building " + b);
            tile.getChildren().add(label);

            tile.setOnMouseClicked(e -> {
                selectedBuilding = b;
                txtSelectedBuilding.setText("Building " + b);

                // Get floors from index
                cbFloorFilter.getItems().setAll(index.getFloors(b));
                if (!cbFloorFilter.getItems().isEmpty()) {
                    selectedFloor = cbFloorFilter.getItems().get(0);
                    cbFloorFilter.setValue(selectedFloor);
                }

                // *** MODIFIED: Switch to FLOOR_VIEW via setMode
                setMode(ViewMode.FLOOR_VIEW);
            });

            canvas.getChildren().add(tile);
        }

        setStatus("Click a building to view floors.");
    }

    /* ======================== Floor Layout ======================== */

    private void renderFloorLayout() {

        if (selectedBuilding == null || selectedFloor == null)
            return;

        // Use DataStructure layer to filter spaces
        List<Space> filtered = index.filterSpaces(
                selectedBuilding,
                selectedFloor,
                cbTypeFilter.getValue(),
                spCapMin.getValue(),
                chkOnlyAvailable.isSelected()
        );

        // Floor background
        double fx = 50, fy = 50, fw = 700, fh = 450;
        Pane floorPane = new Pane();
        floorPane.relocate(fx, fy);
        floorPane.setPrefSize(fw, fh);
        floorPane.getStyleClass().add("floor-pane");
        canvas.getChildren().add(floorPane);

        int cols = 4;
        int rows = Math.max(1, (int) Math.ceil(filtered.size() / (double) cols));

        double cellW = fw / cols;
        double cellH = fh / rows;

        for (int i = 0; i < filtered.size(); i++) {
            Space s = filtered.get(i);
            int row = i / cols;
            int col = i % cols;

            StackPane tile = createSpaceTile(s);
            tile.setPrefSize(cellW - 16, cellH - 16);
            tile.setLayoutX(col * cellW + 8);
            tile.setLayoutY(row * cellH + 8);

            floorPane.getChildren().add(tile);
        }

        setStatus("Building " + selectedBuilding + " Floor " + selectedFloor +
                " - Showing " + filtered.size() + " rooms");
    }

    /* ======================== Tile Rendering ======================== */

    private StackPane createSpaceTile(Space s) {
        StackPane tile = new StackPane();
        tile.getStyleClass().add("space-tile");
        tile.getStyleClass().add(cssForStatus(s.getStatus()));

        Text title = new Text(s.getName());
        title.getStyleClass().add("subtitle");
        tile.getChildren().add(title);

        Tooltip tip = new Tooltip(
                s.getName() + "\n" +
                        "Building: " + s.getBuilding() + "\n" +
                        "Floor: " + s.getFloor() + "\n" +
                        "Type: " + s.getType() + "\n" +
                        "Capacity: " + s.getCapacity() + "\n" +
                        "Status: " + s.getStatus()
        );
        tip.setShowDelay(Duration.millis(150));
        Tooltip.install(tile, tip);

        tile.setOnMouseClicked(e -> onSpaceClicked(s));

        return tile;
    }

    private String cssForStatus(String st) {

        if (st == null) return "space-booked";
        st = st.toLowerCase();

        return switch (st) {
            case "available" -> "space-available";
            case "maintenance" -> "space-maint";
            case "booked" -> "space-booked";
            default -> "space-booked";
        };
    }

    /* ======================== Tile Click Logic ======================== */

    private void onSpaceClicked(Space s) {

        if ("booked".equalsIgnoreCase(s.getStatus())) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    s.getName() + " is booked. Release?");
            confirm.setTitle("Booked");

            ButtonType releaseBtn = new ButtonType("Release");
            confirm.getButtonTypes().addAll(releaseBtn, ButtonType.CANCEL);

            var result = confirm.showAndWait();
            if (result.isPresent() && result.get() == releaseBtn) {
                s.setStatus("available");
                setStatus("Released " + s.getName());
                render();
            }
            return;
        }

        if (!s.isAvailable()) {
            showAlert(Alert.AlertType.WARNING, "Not Available",
                    s.getName() + " cannot be booked.");
            return;
        }

        // Jump to time slot selector interface
        TimeSlotSelectorController.showSelector(s);
        render();
    }

    /* ======================== Utility Methods ======================== */

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg);
    }

    /* ======================== Button Events ======================== */

    @FXML
    private void onBackToOverview() {
        // *** MODIFIED: Back to Building Overview (canvas mode)
        selectedBuilding = null;
        selectedFloor = null;
        setMode(ViewMode.MAP_VIEW);
    }

    /* ======================== Map Initialization & JS Interaction (New) ======================== */

    // *** MODIFIED + EXTENDED: Initialize WebView, expose current Controller to JS
    private void initMapView() {
        if (mapView == null) {
            System.out.println("mapView is null, check FXML fx:id.");
            return;
        }

        WebEngine engine = mapView.getEngine();
        URL url = getClass().getResource("/map/building_map.html");
        System.out.println("URL = " + url);

        if (url == null) {
            System.out.println("ERROR: cannot find /map/building_map.html");
            return;
        }

        engine.load(url.toExternalForm());

        // After page loaded, expose this to JS: window.app = this
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("app", this);
                    System.out.println("JS bridge ready: window.app = UserSpaceController");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * *** NEW: Entry method for building_map.html call
     * Called when marker clicked in JS: window.app.selectBuilding("A")
     */
    public void selectBuilding(String buildingCode) {
        Platform.runLater(() -> {
            System.out.println("JS selected building: " + buildingCode);
            selectedBuilding = buildingCode;

            if (txtSelectedBuilding != null) {
                txtSelectedBuilding.setText("Building " + buildingCode);
            }

            // Get floor list from index for this Building
            if (cbFloorFilter != null) {
                cbFloorFilter.getItems().setAll(index.getFloors(buildingCode));
                if (!cbFloorFilter.getItems().isEmpty()) {
                    selectedFloor = cbFloorFilter.getItems().get(0);
                    cbFloorFilter.setValue(selectedFloor);
                }
            }

            // Switch to floor view and render
            setMode(ViewMode.FLOOR_VIEW);
        });
    }
}
