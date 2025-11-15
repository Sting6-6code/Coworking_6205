package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.Space;
import model.Booking;
import model.TimeSlot;

import java.util.*;
import java.util.stream.Collectors;

// ==== MOD 0: 新增 CSV 读取相关 import ====
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserSpaceController {

    private enum ViewMode {
        BUILDING_OVERVIEW, FLOOR_VIEW
    }

    @FXML
    private ComboBox<String> cbTypeFilter;
    @FXML
    private Spinner<Integer> spCapMin;

    @FXML
    private TextField txtSelectedBuilding;
    @FXML
    private ComboBox<String> cbFloorFilter;
    @FXML
    private CheckBox chkOnlyAvailable;
    @FXML
    private AnchorPane canvas;
    @FXML
    private Label lblStatus;

    private ViewMode currentMode = ViewMode.BUILDING_OVERVIEW;
    private String selectedBuilding = null;
    private String selectedFloor = null;

    private final List<Space> allSpaces = new ArrayList<>();
    private final Map<String, Map<String, List<Space>>> buildingFloorMap = new HashMap<>();

    // ==== MOD 1: 定义 CSV 路径常量（按需改成你的真实路径）====
    private static final String SPACE_CSV_PATH = "data/spaces.csv";

    @FXML
    public void initialize() {
        loadSpaces();          // 从 CSV 加载 120 个空间
        initFloorSelector();   // 初始化楼层选择器
        initFilters();         // 初始化过滤控件
        render();

        canvas.setOnScroll(e -> {
            double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            canvas.setScaleX(canvas.getScaleX() * factor);
            canvas.setScaleY(canvas.getScaleY() * factor);
        });
    }

    private void initFilters() {
        // Type 下拉
        cbTypeFilter.getItems().setAll("All", "room", "desk", "office", "conference", "event");
        cbTypeFilter.getSelectionModel().select("All");

        // Capacity Spinner
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

    private void refreshFloorOptions() {
        cbFloorFilter.getItems().clear();

        if (selectedBuilding == null)
            return;

        Map<String, List<Space>> floorMap = buildingFloorMap.getOrDefault(selectedBuilding, Map.of());
        List<String> floors = new ArrayList<>(floorMap.keySet());
        Collections.sort(floors); // 可选

        cbFloorFilter.getItems().addAll(floors);
        if (!floors.isEmpty()) {
            cbFloorFilter.setValue(floors.get(0));
            selectedFloor = floors.get(0);
        }
    }

    /** 根据房间类型返回每小时所需 Credits（目前没在 loadSpaces 里用，可以先保留）*/
    private int getCreditsPerHour(String type) {
        return switch (type) {
            case "desk" -> 9;
            case "room" -> 12;
            case "office" -> 15;
            case "conference" -> 18;
            case "event" -> 25;
            default -> 10;
        };
    }


    // ==== MOD 2: 重写 loadSpaces() —— 从 CSV 读入 120 个房间 ====
    /**
     * 从 spaces.csv 读取所有空间，并按 building + floor 填充 buildingFloorMap。
     * CSV 字段顺序: id,name,floor,spaceId,type,building,seats,status,creditsPerHour
     */
    private void loadSpaces() {
        allSpaces.clear();
        buildingFloorMap.clear();

        try (BufferedReader br = Files.newBufferedReader(
                Paths.get(SPACE_CSV_PATH), StandardCharsets.UTF_8)) {

            String line = br.readLine(); // 跳过表头

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] p = line.split(",");

                if (p.length < 9) continue;

                // 读取 CSV 字段
                String id        = p[0].trim();  // 数字主键 ID
                String name      = p[1].trim();
                String floor     = p[2].trim();
                String spaceId   = p[3].trim();  // 房间编号 A-01-01
                String type      = p[4].trim();
                String building  = p[5].trim();
                int seats        = Integer.parseInt(p[6].trim());
                String status    = p[7].trim();
                double credits   = Double.parseDouble(p[8].trim());

                // -------- 正确匹配新版 Space 构造方法 --------
                Space space = new Space(
                        id,         // CSV 原始 id
                        name,       // 名称
                        floor,      // 楼层
                        spaceId,    // Space 编号
                        type,
                        building,
                        seats,
                        status,
                        credits,
                        0, 0, 0, 0  // x, y, w, h（先不用布局坐标）
                );

                allSpaces.add(space);

                // 按楼栋 + 楼层归类
                buildingFloorMap
                        .computeIfAbsent(building, b -> new HashMap<>())
                        .computeIfAbsent(floor, f -> new ArrayList<>())
                        .add(space);
            }

            setStatus("Loaded " + allSpaces.size() + " spaces from CSV.");
        } catch (IOException e) {
            e.printStackTrace();
            setStatus("Failed to load spaces.csv: " + e.getMessage());
        }
    }


    // ==== MOD 3: initFloorSelector 不再写死 "1,2,3"，只保留监听 ====
    private void initFloorSelector() {
        cbFloorFilter.getItems().clear();
        cbFloorFilter.setOnAction(e -> {
            if (currentMode == ViewMode.FLOOR_VIEW) {
                selectedFloor = cbFloorFilter.getValue();
                render();
            }
        });
    }

    private void render() {
        canvas.getChildren().clear();
        switch (currentMode) {
            case BUILDING_OVERVIEW -> renderBuildingOverview();
            case FLOOR_VIEW -> renderFloorLayout();
        }
    }

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
                selectedFloor = null; // 清空选中的楼层
                txtSelectedBuilding.setText("Building " + b);
                refreshFloorOptions();      // 根据 building 动态加载楼层
                currentMode = ViewMode.FLOOR_VIEW;
                render();
            });

            canvas.getChildren().add(tile);
        }
        setStatus("Click a building to view floor layout");
    }

    // ==== MOD 4: 重写 renderFloorLayout —— 每层用“楼层大方块 + 网格房间”布局 ====
    private void renderFloorLayout() {
        if (selectedBuilding == null || selectedFloor == null)
            return;

        List<Space> spaces = buildingFloorMap
                .getOrDefault(selectedBuilding, Map.of())
                .getOrDefault(selectedFloor, List.of());

        String selectedType = cbTypeFilter.getValue();
        int minCapacity = spCapMin.getValue();
        boolean onlyAvailable = chkOnlyAvailable.isSelected();

        canvas.getChildren().clear();

        // 楼层背景面板
        double fx = 50, fy = 50, fw = 700, fh = 450;
        Pane floorPane = new Pane();
        floorPane.relocate(fx, fy);
        floorPane.setPrefSize(fw, fh);
        floorPane.getStyleClass().add("floor-pane");
        canvas.getChildren().add(floorPane);

        // ✅ 单独渲染电梯
        for (Space s : spaces) {
            if (s.getType().equalsIgnoreCase("elevator")) {
                StackPane elevatorTile = createSpaceTile(s, onlyAvailable);
                elevatorTile.setPrefSize(80, 80);
                elevatorTile.setLayoutX(10);
                elevatorTile.setLayoutY(10);
                floorPane.getChildren().add(elevatorTile);
            }
        }

        // ✅ 排除电梯后进行普通空间网格排布
        List<Space> nonElevatorSpaces = spaces.stream()
                .filter(s -> !s.getType().equalsIgnoreCase("elevator"))
                .collect(Collectors.toList());

        int cols = 4;
        int rows = (int) Math.ceil(nonElevatorSpaces.size() / (double) cols);
        if (rows == 0) rows = 1;

        double cellW = fw / cols;
        double cellH = fh / rows;

        int shownCount = 0;

        for (int i = 0; i < nonElevatorSpaces.size(); i++) {
            Space s = nonElevatorSpaces.get(i);
            int row = i / cols;
            int col = i % cols;

            StackPane tile = createSpaceTile(s, onlyAvailable);
            tile.setPrefSize(cellW - 16, cellH - 16);
            tile.setLayoutX(col * cellW + 8);
            tile.setLayoutY(row * cellH + 8);

            // ✅ 判断是否满足过滤条件
            boolean typeMatch = selectedType == null
                    || selectedType.equals("All")
                    || s.getType().equalsIgnoreCase(selectedType);
            boolean capMatch = s.getCapacity() >= minCapacity;
            boolean availMatch = !onlyAvailable || "available".equalsIgnoreCase(s.getStatus());

            boolean shouldShow = typeMatch && capMatch && availMatch;

            if (!shouldShow) {
                tile.setOpacity(0.2);               // 不匹配：淡化
                tile.setDisable(true);              // 禁用点击
                tile.setMouseTransparent(true);     // 鼠标穿透
            } else {
                shownCount++;
            }

            floorPane.getChildren().add(tile);
        }

        setStatus("Viewing Building " + selectedBuilding + " Floor " + selectedFloor
                + " - Showing " + shownCount + " / " + spaces.size() + " spaces");
    }


    private StackPane createSpaceTile(Space s, boolean highlightOnlyAvail) {
        StackPane box = new StackPane();
        box.getStyleClass().add("space-tile");

        String cssClass = cssForStatus(s.getStatus(), highlightOnlyAvail);
        if (cssClass != null && !cssClass.isEmpty()) {
            box.getStyleClass().add(cssClass);
        }

        Text title = new Text(s.getName());
        title.getStyleClass().add("subtitle");
        box.getChildren().add(title);

        Tooltip tip = new Tooltip(
                s.getName() + "\n" +
                "Building: " + s.getBuilding() + "\n" +
                "Floor: " + s.getFloor() + "\n" +
                "Type: " + s.getType() + "\n" +
                "Capacity: " + s.getCapacity() + "\n" +
                "Status: " + s.getStatus());
        tip.setShowDelay(Duration.millis(150));
        Tooltip.install(box, tip);

        box.setOnMouseClicked(e -> onSpaceClicked(s));

        return box;
    }

    private String cssForStatus(String status, boolean highlightOnlyAvail) {
        String st = status == null ? "" : status.toLowerCase();

        if (highlightOnlyAvail) {
            if ("available".equals(st))
                return "space-available";
            return "space-greyed";
        } else {
            return switch (st) {
                case "available" -> "space-available";
                case "maintenance" -> "space-maint";
                case "booked" -> "space-booked";
                default -> "space-booked";
            };
        }
    }

    private void onSpaceClicked(Space s) {
        if ("booked".equalsIgnoreCase(s.getStatus())) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Booked Space");
            confirm.setHeaderText(s.getName() + " is already booked.");
            confirm.setContentText("Do you want to release it?");

            ButtonType releaseBtn = new ButtonType("Release");
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(releaseBtn, cancelBtn);

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == releaseBtn) {
                s.setStatus("available");
                setStatus("Released " + s.getName() + " successfully.");
                render();
            } else {
                setStatus("Release cancelled.");
            }
        } else if (!s.isAvailable()) {
            showAlert(Alert.AlertType.WARNING, "Not Available",
                    s.getName() + " cannot be booked at the moment.");
        } else {
            // 🔥 跳转到时间槽选择界面
            TimeSlotSelectorController.showSelector(s);

            // 返回时刷新
            render();
        }
    }

    private void setStatus(String s) {
        if (lblStatus != null) {
            lblStatus.setText(s);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type, message, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }

    private static String nz(String s, String def) {
        String t = nz(s);
        return t.isEmpty() ? def : t;
    }

    private String capitalize(String s) {
        return s == null || s.isEmpty() ? "" : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @FXML
    private void onBackToOverview() {
        currentMode = ViewMode.BUILDING_OVERVIEW;
        selectedBuilding = null;
        selectedFloor = null;
        render();
    }

    @FXML
    private void handleOverview() {
        currentMode = ViewMode.BUILDING_OVERVIEW;
        selectedBuilding = null;
        selectedFloor = null;
        render();
        setStatus("Switched to Building Overview.");
    }

    @FXML
    private void handleBackToBuildings() {
        if (selectedBuilding != null) {
            currentMode = ViewMode.FLOOR_VIEW;
            selectedFloor = cbFloorFilter.getValue();
            render();
            setStatus("Back to Building " + selectedBuilding + " Floor Selection");
        } else {
            setStatus("No building selected.");
        }
    }
}
