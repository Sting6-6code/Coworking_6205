package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Booking;
import model.Space;
import model.Transaction;
import util.BookingDataUtil;
import util.CurrentUser;
import util.TransactionDataUtil;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class TimeSlotSelectorController {

    @FXML private FlowPane timeBar;
    @FXML private Label lblSelectedTime, lblCost, lblTitle;
    @FXML private Button btnConfirm, btnRelease;
    @FXML private DatePicker datePicker; // ✅ NEW: 添加日期选择器
    private LocalDate selectedDate = LocalDate.now(); 

    private Stage stage;
    private final Map<ToggleButton, LocalTime> buttonTimeMap = new LinkedHashMap<>();
    private final List<ToggleButton> selected = new ArrayList<>();

    private Space space;
    private final LocalTime startTime = LocalTime.of(9, 0);
    private final LocalTime endTime = LocalTime.of(21, 0);

    public static void showSelector(Space space) {
        try {
            FXMLLoader loader = new FXMLLoader(TimeSlotSelectorController.class.getResource("/application/TimeSlotSelector.fxml"));
            BorderPane pane = loader.load();
            TimeSlotSelectorController controller = loader.getController();
            controller.init(space);

            Stage dialog = new Stage();
            dialog.setTitle("Booking for " + space.getName());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(pane));
            controller.stage = dialog;
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(Space space) {
        this.space = space;
        this.lblTitle.setText("Booking: " + space.getName());

        // ✅ NEW: 初始化 DatePicker（默认今天，禁止手动输入，只能选择今天及以后）
        datePicker.setValue(LocalDate.now());
        datePicker.setEditable(false);                            // 禁止键盘输入
        datePicker.getEditor().setDisable(true);                  // 禁用输入框
        datePicker.getEditor().setOpacity(1);                     // 保持可见

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now())); // 只能选今天及之后
            }
        });

        datePicker.setOnAction(e -> updateTimeBar()); // ✅ 每次日期更改刷新时间栏

        // ✅ 初始化时间按钮状态
        updateTimeBar();

        // ✅ 注册确认/释放按钮事件
        btnConfirm.setOnAction(e -> confirm());
        btnRelease.setOnAction(e -> release());
    }

    // ✅ NEW: 替代原先生成按钮的逻辑
    private void updateTimeBar() {
        timeBar.getChildren().clear();
        buttonTimeMap.clear();
        selected.clear();

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) return;

        List<Booking> existingBookings = BookingDataUtil.getBookingsBySpaceIdAndDate(space.getSpaceId(), selectedDate)
                .stream()
                .filter(b -> b.getStatus().equals("booked"))
                .collect(Collectors.toList());

        Map<LocalTime, Booking> bookingMap = new HashMap<>();
        for (Booking b : existingBookings) {
            LocalTime bStart = b.getStartTime();
            LocalTime bEnd = b.getEndTime();
            while (!bStart.isAfter(bEnd.minusMinutes(30))) {
                bookingMap.put(bStart, b);
                bStart = bStart.plusMinutes(30);
            }
        }

        LocalTime time = startTime;
        while (time.isBefore(endTime)) {
            ToggleButton btn = new ToggleButton(time.toString());
            btn.setPrefWidth(80);
            btn.setOnAction(e -> handleToggle(btn));

            Booking match = bookingMap.get(time);
            if (match != null) {
                if (!match.getUserId().equals(CurrentUser.getUserId())) {
                    // 他人预定 → 禁用
                    btn.setDisable(true);
                    btn.setStyle("-fx-background-color: #bbbbbb;"); // 灰色
                } else {
                    // 当前用户预定 → 可选中释放
                    btn.setDisable(false);
                    btn.setStyle("-fx-background-color: #ffdd88;"); // 黄色表示“你订的”
                }
            }

            buttonTimeMap.put(btn, time);
            timeBar.getChildren().add(btn);
            time = time.plusMinutes(30);
        }

        updateLabels();
    }

    
    @FXML
    private void handleDateChange() {
        updateTimeBar(); // 每次日期变化时重新渲染按钮状态
    }

    private void handleToggle(ToggleButton btn) {
        if (btn.isSelected()) {
            selected.add(btn);
        } else {
            selected.remove(btn);
        }
        selected.sort(Comparator.comparing(buttonTimeMap::get));
        updateLabels();
    }

    private void updateLabels() {
        if (selected.isEmpty()) {
            lblSelectedTime.setText("Selected: —");
            lblCost.setText("Estimated Cost: —");
            return;
        }
        LocalTime start = buttonTimeMap.get(selected.get(0));
        LocalTime end = buttonTimeMap.get(selected.get(selected.size() - 1)).plusMinutes(30);
        lblSelectedTime.setText("Selected: " + start + " - " + end);

        long minutes = Duration.between(start, end).toMinutes();
        double cost = (minutes / 60.0) * space.getCreditsPerHour();
        lblCost.setText(String.format("Estimated Cost: %.2f credits", cost));
    }

    private void confirm() {
        if (selected.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please select at least one time slot.").showAndWait();
            return;
        }

        LocalTime start = buttonTimeMap.get(selected.get(0));
        LocalTime end = buttonTimeMap.get(selected.get(selected.size() - 1)).plusMinutes(30);

        // ✅ MODIFIED: 使用用户选择的日期，而不是 LocalDate.now()
        LocalDate date = datePicker.getValue();
        if (date == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a date.").showAndWait();
            return;
        }

        long minutes = Duration.between(start, end).toMinutes();
        int totalCredits = (int) Math.ceil((minutes / 60.0) * space.getCreditsPerHour());

        String summary =
                "Please confirm your booking:\n\n" +
                "Space Name:  " + space.getName() + "\n" +
                "Building:    " + space.getBuilding() + "\n" +
                "Floor:       " + space.getFloor() + "\n" +
                "Type:        " + space.getType() + "\n" +
                "Capacity:    " + space.getCapacity() + "\n" +
                "----------------------------------------\n" +
                "Date:        " + date + "\n" +
                "Time:        " + start + " - " + end + "\n" +
                "Total Credits: " + totalCredits + "\n";

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Booking");
        confirmDialog.setHeaderText("Booking Summary");
        confirmDialog.setContentText(summary);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        Booking booking = new Booking();
        String bookingId = UUID.randomUUID().toString();
        booking.setBookingId(bookingId);
        booking.setUserId(CurrentUser.getUserId());
        booking.setSpaceId(space.getSpaceId());
        booking.setDate(date); // ✅ MODIFIED: 正确写入选择日期
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setTotalCredits(totalCredits);
        booking.setStatus("booked");

        BookingDataUtil.addBooking(booking);

        // Create corresponding Transaction record
        try {
            String userId = CurrentUser.getUserId();
            if (userId == null) {
                System.err.println("ERROR: CurrentUser.getUserId() returned null!");
                new Alert(Alert.AlertType.WARNING, "Warning: User ID is null. Transaction not created.").showAndWait();
            } else {
                double amount = (minutes / 60.0) * space.getCreditsPerHour();
                String description = String.format("Booking: %s (%s) - %s %s-%s", 
                        space.getName(), space.getType(), date, start, end);
                
                Transaction transaction = new Transaction(
                        userId,
                        Transaction.TransactionType.BOOKING,
                        amount,
                        date,
                        description,
                        bookingId
                );
                
                TransactionDataUtil.addTransaction(transaction);
                System.out.println("Transaction created successfully: " + transaction.getTransactionId());
            }
        } catch (Exception e) {
            System.err.println("ERROR creating transaction: " + e.getMessage());
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error creating transaction: " + e.getMessage()).showAndWait();
        }

        new Alert(Alert.AlertType.INFORMATION, "Booking completed!").showAndWait();
        stage.close();
    }

    private void release() {
        LocalDate date = datePicker.getValue();

        if (selected.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please select time slots to release.").showAndWait();
            return;
        }

        boolean anyReleased = false;

        for (ToggleButton btn : selected) {
            LocalTime time = buttonTimeMap.get(btn);

            // ⭐ NEW: 先找覆盖该 slot 的 booking（整段 booking）
       
            Booking booking = BookingDataUtil.getBookingCoveringSlot(space.getSpaceId(), date, time);
            // ⭐ FIX: booking 不存在 → skip
            if (booking == null) continue;

            // ⭐ FIX: 只能释放自己订的
            if (!booking.getUserId().equals(CurrentUser.getUserId())) continue;

            if (!booking.getStatus().equals("booked")) continue;

            // ⭐⭐ NEW: 分裂或更新 booking（核心）
            BookingDataUtil.releaseSingleSlot(
                    space.getSpaceId(),
                    date,
                    time
            );

            anyReleased = true;
        }

        // 清空 UI 选择
        selected.clear();
        updateLabels();
        updateTimeBar();

        if (anyReleased) {
            new Alert(Alert.AlertType.INFORMATION, "Selected time slots released successfully.").showAndWait();
        } else {
            new Alert(Alert.AlertType.WARNING, "No valid bookings were found to release.").showAndWait();
        }
    }

    @FXML
    private void handleClose() {
        stage.close();
    }
}
