package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import model.Space;
import model.Booking;
import util.BookingDataUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AdminSpaceController {

    private enum ViewMode { BUILDING_OVERVIEW, FLOOR_VIEW }

    // FXML Components
    @FXML private ComboBox<String> cbTypeFilter;
    @FXML private Spinner<Integer> spCapMin;

    @FXML private ComboBox<String> cbBuildingFilter;
    @FXML private ComboBox<String> cbFloorFilter;

    @FXML private AnchorPane canvas;
    @FXML private Label lblStatus;

    // Data
    private ViewMode currentMode = ViewMode.BUILDING_OVERVIEW;
    private String selectedBuilding = null;
    private String selectedFloor = null;

    private final List<Space> allSpaces = new ArrayList<>();
    private final Map<String, Map<String, List<Space>>> buildingFloorMap = new HashMap<>();

    private static final String SPACE_CSV_PATH = "data/spaces.csv";

    // Initialization
    @FXML
    public void initialize() {
        loadSpaces();
        initFilters();
        initBuildingSelector();

        render();

        canvas.setOnScroll(e -> {
            double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            canvas.setScaleX(canvas.getScaleX() * factor);
            canvas.setScaleY(canvas.getScaleY() * factor);
        });
    }

    // Filters
    private void initFilters() {
        cbTypeFilter.getItems().setAll("All", "room", "desk", "office", "conference", "event");
        cbTypeFilter.getSelectionModel().select("All");

        spCapMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));

        cbTypeFilter.valueProperty().addListener((o, oldVal, newVal) -> render());
        spCapMin.valueProperty().addListener((o, oldVal, newVal) -> render());
    }

    private void initBuildingSelector() {
        cbBuildingFilter.getItems().setAll("A", "B", "C", "D", "E");

        cbBuildingFilter.valueProperty().addListener((o, oldVal, newVal) -> {
            selectedBuilding = newVal;
            selectedFloor = null;

            refreshFloorOptions();
            currentMode = ViewMode.FLOOR_VIEW;
            render();
        });
    }

    private void refreshFloorOptions() {
        cbFloorFilter.getItems().clear();

        if (selectedBuilding == null) return;
        if (!buildingFloorMap.containsKey(selectedBuilding)) return;

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

    // Load CSV
    private void loadSpaces() {
        allSpaces.clear();
        buildingFloorMap.clear();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(SPACE_CSV_PATH), StandardCharsets.UTF_8)) {

            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] p = line.split(",");

                if (p.length < 9) continue;

                Space s = new Space(
                        p[0], p[1], p[2], p[3], p[4],
                        p[5], Integer.parseInt(p[6]),
                        p[7], Double.parseDouble(p[8]),
                        0, 0, 0, 0
                );

                allSpaces.add(s);

                buildingFloorMap
                        .computeIfAbsent(s.getBuilding(), x -> new HashMap<>())
                        .computeIfAbsent(s.getFloor(), x -> new ArrayList<>())
                        .add(s);
            }

            setStatus("Loaded " + allSpaces.size() + " spaces.");

        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Error loading spaces.csv");
        }
    }

    // --- Add Space Dialog ---
    @FXML
    private void showAddSpaceDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add New Space");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField tfName = new TextField();

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("room", "desk", "office", "conference", "event");

        ComboBox<String> cbBuilding = new ComboBox<>();
        cbBuilding.getItems().addAll("A", "B", "C", "D", "E");

        ComboBox<String> cbFloor = new ComboBox<>();
        cbFloor.getItems().addAll("1", "2", "3");

        Spinner<Integer> spCapacity = new Spinner<>(1, 100, 1);

        grid.add(new Label("Name:"), 0, 0); grid.add(tfName, 1, 0);
        grid.add(new Label("Type:"), 0, 1); grid.add(cbType, 1, 1);
        grid.add(new Label("Building:"), 0, 2); grid.add(cbBuilding, 1, 2);
        grid.add(new Label("Floor:"), 0, 3); grid.add(cbFloor, 1, 3);
        grid.add(new Label("Capacity:"), 0, 4); grid.add(spCapacity, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == addButton) {
                if (tfName.getText().isBlank() || cbType.getValue() == null || cbFloor.getValue() == null || cbBuilding.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Incomplete Info", "Please fill in all fields.");
                    return null;
                }

                Map<String, String> m = new HashMap<>();
                m.put("name", tfName.getText());
                m.put("type", cbType.getValue());
                m.put("floor", cbFloor.getValue());
                m.put("building", cbBuilding.getValue());
                m.put("capacity", spCapacity.getValue().toString());
                return m;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(this::handleAddSpace);
    }


 // ===== 修改后的 handleAddSpace 方法（带标注） =====
    private void handleAddSpace(Map<String, String> input) {
        try {
            // --- ✅ 修改1：生成自增 ID（如 121, 122...） ---
            int maxId = allSpaces.stream()
                    .mapToInt(s -> {
                        try {
                            return Integer.parseInt(s.getId());
                        } catch (NumberFormatException e) {
                            return 100; // fallback
                        }
                    })
                    .max().orElse(120);
            String id = String.valueOf(maxId + 1);  // 从121开始累加

            // --- ✅ 修改2：生成唯一的6位spaceId，如 A1B2C3 ---
            String spaceId = generateRandomCode(6);

            // --- 表单字段 ---
            String name = input.get("name");
            String type = input.get("type");
            String floor = input.get("floor");
            String building = input.get("building");
            int seats = Integer.parseInt(input.get("capacity"));

            Space s = new Space(
                    id, name, floor,
                    spaceId,                // ✅ 使用新生成的唯一 spaceId
                    type, building, seats,
                    "available", 12,
                    0, 0, 0, 0
            );

            allSpaces.add(s);

            saveSpacesToCSV();

            setStatus("New space added: " + name);
            loadSpaces();
            render();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Check your space info.");
        }
    }

    // ===== ✅ 新增工具方法：生成随机字母数字组合 =====
    private String generateRandomCode(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }


    private void saveSpacesToCSV() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(SPACE_CSV_PATH))) {

            bw.write("id,name,floor,spaceId,type,building,seats,status,creditsPerHour\n");

            for (Space sp : allSpaces) {
                bw.write(String.join(",",
                        sp.getId(), sp.getName(), sp.getFloor(), sp.getSpaceId(),
                        sp.getType(), sp.getBuilding(),
                        String.valueOf(sp.getCapacity()),
                        sp.getStatus(),
                        String.valueOf(sp.getCreditsPerHour())
                ));
                bw.newLine();
            }
        }
    }

    // --- Rendering ---
    private void render() {
        canvas.getChildren().clear();

        switch (currentMode) {
            case BUILDING_OVERVIEW -> renderBuildingOverview();
            case FLOOR_VIEW -> renderFloorLayout();
        }
    }

    private void renderBuildingOverview() {
        double xStart = 80, y = 100, size = 120, gap = 150;
        String[] buildings = {"A", "B", "C", "D", "E"};

        for (int i = 0; i < buildings.length; i++) {
            String b = buildings[i];

            StackPane tile = new StackPane();
            tile.setPrefSize(size, size);
            tile.relocate(xStart + i * gap, y);
            tile.getStyleClass().addAll("space-tile", "space-available");

            tile.getChildren().add(new Text("Building " + b));

            tile.setOnMouseClicked(e -> {
                selectedBuilding = b;
                selectedFloor = null;
                currentMode = ViewMode.FLOOR_VIEW;

                refreshFloorOptions();
                render();
            });

            canvas.getChildren().add(tile);
        }
    }

    private void renderFloorLayout() {
        if (selectedBuilding == null || selectedFloor == null) return;

        List<Space> spaces = buildingFloorMap
                .get(selectedBuilding)
                .get(selectedFloor);

        String typeFilter = cbTypeFilter.getValue();
        int capMin = spCapMin.getValue();

        Pane floorPane = new Pane();
        floorPane.relocate(50, 50);
        floorPane.setPrefSize(700, 450);
        floorPane.getStyleClass().add("floor-pane");

        canvas.getChildren().add(floorPane);

        int cols = 4;
        int rows = (int) Math.ceil(spaces.size() / 4.0);
        rows = Math.max(rows, 1);

        double cellW = 700 / cols;
        double cellH = 450 / rows;

        for (int i = 0; i < spaces.size(); i++) {
            Space s = spaces.get(i);

            if (!typeFilter.equals("All") &&
                    !s.getType().equalsIgnoreCase(typeFilter)) continue;

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
        List<Booking> bookings =
                BookingDataUtil.getBookingsBySpaceIdAndDate(s.getSpaceId(), today);

        // ✅ 1. 判断是否为 maintenance 状态
        if (s.getStatus().equalsIgnoreCase("maintenance")) {
            box.getStyleClass().add("space-maint");  // 添加样式
        }
        else if (bookings.isEmpty()) {
            box.getStyleClass().add("space-available");
        } else {
            box.getStyleClass().add("space-booked");
        }

        // ✅ 2. 显示基本信息
        box.getChildren().add(new Text(s.getName() + "\n" + s.getType()));

        // ✅ 3. Tooltip 提示内容（包含维护提示）
        String tooltipText = "Space: " + s.getName() + "\n" +
                             "Type: " + s.getType() + "\n" +
                             "Capacity: " + s.getCapacity() + "\n" +
                             "Status: " + s.getStatus();

        if (!s.getStatus().equalsIgnoreCase("maintenance")) {
            tooltipText += "\n\nToday bookings:\n" + 
                    (bookings.isEmpty()
                     ? "None"
                     : bookings.stream()
                         .map(b -> b.getStartTime() + " - " + b.getEndTime())
                         .collect(Collectors.joining("\n")));
        } else {
            tooltipText += "\n\nThis space is under maintenance.";
        }

        Tooltip tip = new Tooltip(tooltipText);
        Tooltip.install(box, tip);

        // ✅ 4. 禁止预订：仅非 maintenance 才能点击
        if (!s.getStatus().equalsIgnoreCase("maintenance")) {
            box.setOnMouseClicked(e -> showBookingDetailDialog(s, bookings));
        }

        return box;
    }


    private void showBookingDetailDialog(Space s, List<Booking> bookings) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Booking Info");
        a.setHeaderText("Booking for " + s.getName());

        if (bookings.isEmpty()) {
            a.setContentText("No bookings today.");
        } else {
            a.setContentText(
                    bookings.stream()
                            .map(b -> b.getStartTime() + " - " + b.getEndTime())
                            .collect(Collectors.joining("\n"))
            );
        }

        a.showAndWait();
    }

    @FXML
    private void handleBack() {
        System.out.println("Admin clicked Back.");
    }

    @FXML
    private void handleOverview() {
        currentMode = ViewMode.BUILDING_OVERVIEW;
        selectedBuilding = null;
        selectedFloor = null;
        render();
    }

    private void setStatus(String s) {
        lblStatus.setText(s);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}
