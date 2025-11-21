package controller;

import javafx.application.Platform;                         // *** 新增：用于在 FX 线程更新 UI
import javafx.concurrent.Worker;                           // *** 新增：监听 WebView 加载状态
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

import netscape.javascript.JSObject;                      // *** 新增：JS 与 Java 交互

public class UserSpaceController {

    /* ======================== 状态枚举 ======================== */

    // *** 修改：真正使用 MAP_VIEW 模式（初始显示地图）
    private enum ViewMode { MAP_VIEW, BUILDING_OVERVIEW, FLOOR_VIEW }

    /* ======================== FXML 绑定 ======================== */

    @FXML private ComboBox<String> cbTypeFilter;
    @FXML private Spinner<Integer> spCapMin;
    @FXML private ComboBox<String> cbFloorFilter;
    @FXML private CheckBox chkOnlyAvailable;
    @FXML private TextField txtSelectedBuilding;
    @FXML private AnchorPane canvas;
    @FXML private Label lblStatus;

    // *** 新增：地图视图 WebView（在 FXML 里要有 fx:id="mapView" 的 WebView）
    @FXML private WebView mapView;
    

    /* ======================== 控制器内部状态 ======================== */

    // *** 修改：初始模式改为 MAP_VIEW（先显示地图，再进入楼层视图）
    private ViewMode currentMode = ViewMode.MAP_VIEW;
    private String selectedBuilding = null;
    private String selectedFloor = null;

    /* ======================== Service 层 ======================== */

    private final SpaceService spaceService = new SpaceService();
    private SpaceIndexDS index;

    /* ======================== 初始化 ======================== */

    @FXML
    public void initialize() {

        /* 1. 从 CSV 加载空间数据（不在 Controller 内部做） */
        spaceService.loadSpacesFromCSV();
        index = spaceService.getIndex();

        /* 2. 初始化过滤器 */
        initFilters();

        /* 3. 初始化地图视图（加载 building_map.html） */
        initMapView();                                     // *** 新增

        /* 4. 设置默认视图模式为 MAP_VIEW（只显示地图，不立刻渲染 canvas） */
        setMode(ViewMode.MAP_VIEW);                        // *** 新增

        /* 5. 支持缩放 */
        canvas.setOnScroll(e -> {
            double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            canvas.setScaleX(canvas.getScaleX() * factor);
            canvas.setScaleY(canvas.getScaleY() * factor);
        });
    }

    /* ======================== 过滤器 ======================== */

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

    /* ======================== 视图模式切换（新增） ======================== */

    // *** 新增：统一控制 Map / Building / Floor 三种模式的显示 & 隐藏
    private void setMode(ViewMode mode) {
        this.currentMode = mode;

        if (mode == ViewMode.MAP_VIEW) {
            // 只显示地图，隐藏画布
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
            // 进入 Building 或 Floor 视图：显示画布，隐藏地图
            if (mapView != null) {
                mapView.setVisible(false);
                mapView.setManaged(false);
            }
            if (canvas != null) {
                canvas.setVisible(true);
                canvas.setManaged(true);
            }
            render();   // 重新按当前模式渲染
        }
    }

    /* ======================== 主渲染逻辑 ======================== */

    private void render() {
        canvas.getChildren().clear();

        // *** 修改：显式区分三种模式，MAP_VIEW 时不渲染 canvas
        if (currentMode == ViewMode.BUILDING_OVERVIEW) {
            renderBuildingOverview();
        } else if (currentMode == ViewMode.FLOOR_VIEW) {
            renderFloorLayout();
        } else {
            // MAP_VIEW：什么都不画，避免多余操作
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

                // 从 index 获取楼层
                cbFloorFilter.getItems().setAll(index.getFloors(b));
                if (!cbFloorFilter.getItems().isEmpty()) {
                    selectedFloor = cbFloorFilter.getItems().get(0);
                    cbFloorFilter.setValue(selectedFloor);
                }

                // *** 修改：通过 setMode 切到 FLOOR_VIEW
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

        // 使用 DataStructure 层过滤空间
        List<Space> filtered = index.filterSpaces(
                selectedBuilding,
                selectedFloor,
                cbTypeFilter.getValue(),
                spCapMin.getValue(),
                chkOnlyAvailable.isSelected()
        );

        // 楼层背景
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

    /* ======================== Tile 渲染 ======================== */

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

    /* ======================== Tile 点击逻辑 ======================== */

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

        // 跳转时间槽选择界面
        TimeSlotSelectorController.showSelector(s);
        render();
    }

    /* ======================== 工具方法 ======================== */

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg);
    }

    /* ======================== 按钮事件 ======================== */

    @FXML
    private void onBackToOverview() {
        // *** 修改：回到 Building Overview（canvas 模式）
        selectedBuilding = null;
        selectedFloor = null;
        setMode(ViewMode.MAP_VIEW);
    }

    /* ======================== 地图初始化 & JS 交互（新增） ======================== */

    // *** 修改 + 扩展：初始化 WebView，并把当前 Controller 暴露给 JS
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

        // 页面加载完成后，把 this 暴露给 JS：window.app = this
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
     * *** 新增：给 building_map.html 调用的入口方法
     * JS 中 marker 点击时会调用：window.app.selectBuilding("A")
     */
    public void selectBuilding(String buildingCode) {
        Platform.runLater(() -> {
            System.out.println("JS selected building: " + buildingCode);
            selectedBuilding = buildingCode;

            if (txtSelectedBuilding != null) {
                txtSelectedBuilding.setText("Building " + buildingCode);
            }

            // 从索引中获取该 Building 的楼层列表
            if (cbFloorFilter != null) {
                cbFloorFilter.getItems().setAll(index.getFloors(buildingCode));
                if (!cbFloorFilter.getItems().isEmpty()) {
                    selectedFloor = cbFloorFilter.getItems().get(0);
                    cbFloorFilter.setValue(selectedFloor);
                }
            }

            // 切换到楼层视图并渲染
            setMode(ViewMode.FLOOR_VIEW);
        });
    }
}
