package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookingController {

    @FXML private ChoiceBox<String> companyChoice;
    @FXML private ChoiceBox<String> floorChoice;
    @FXML private TextField quantityField;
    @FXML private TableView<BookingData> userBookingTable;

    @FXML private TableColumn<BookingData, String> colName;
    @FXML private TableColumn<BookingData, String> colLocation;
    @FXML private TableColumn<BookingData, String> colFloor;
    @FXML private TableColumn<BookingData, String> colType;
    @FXML private TableColumn<BookingData, String> colPrice;
    @FXML private TableColumn<BookingData, String> colQuantity;
    @FXML private TableColumn<BookingData, String> colAvailable; // 新增列显示可用数量

    private static final String BOOKING_FILE = "data/booking.csv";
    private static final String USERBOOKING_FILE = "data/userbooking.csv";

    private ObservableList<BookingData> allBookingList = FXCollections.observableArrayList();
    private ObservableList<BookingData> userBookingList = FXCollections.observableArrayList();

    private static String currentUser;

    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    @FXML
    public void initialize() {
        loadAllBookings();
        loadUserBookings();
        setupTable();
        populateCompanyChoice();
       
    }

    private void setupTable() {
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colLocation.setCellValueFactory(data -> data.getValue().locationProperty());
        colFloor.setCellValueFactory(data -> data.getValue().floorProperty());
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colPrice.setCellValueFactory(data -> data.getValue().priceProperty());
        colQuantity.setCellValueFactory(data -> data.getValue().quantityProperty());

        userBookingTable.setItems(userBookingList);
    }

    @FXML private TextField availableField; // 显示可用数量

    private void populateCompanyChoice() {
        List<String> companies = allBookingList.stream()
                .map(BookingData::getName)
                .distinct()
                .collect(Collectors.toList());
        companyChoice.setItems(FXCollections.observableArrayList(companies));

        companyChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // 更新楼层选择
            List<String> floors = allBookingList.stream()
                    .filter(b -> b.getName().equals(newVal))
                    .map(BookingData::getFloor)
                    .distinct()
                    .collect(Collectors.toList());
            floorChoice.setItems(FXCollections.observableArrayList(floors));

            // 清空 Available
            availableField.setText("");
        });

        floorChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            String selectedCompany = companyChoice.getValue();
            if (selectedCompany != null && newVal != null) {
                BookingData target = allBookingList.stream()
                        .filter(b -> b.getName().equals(selectedCompany) && b.getFloor().equals(newVal))
                        .findFirst().orElse(null);
                if (target != null) {
                    availableField.setText(target.getAvailable());
                } else {
                    availableField.setText("0");
                }
            }
        });
    }
    private void loadAllBookings() {
        allBookingList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKING_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] data = line.split(",");
                    if (data.length < 7) continue; // 包含 Available
                    allBookingList.add(new BookingData(
                            data[0], data[1], data[2], data[3], data[4], data[5], data[6]
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUserBookings() {
        userBookingList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(USERBOOKING_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] data = line.split(",");
                    if (data.length < 7) continue;
                    String username = data[0];
                    if (username.equals(currentUser)) {
                        userBookingList.add(new BookingData(
                                data[1], data[2], data[3], data[4], data[5], data[6], "0"
                        ));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // 文件不存在则创建
            try {
                new File(USERBOOKING_FILE).createNewFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void bookRoom() {
        String selectedCompany = companyChoice.getValue();
        String selectedFloor = floorChoice.getValue();
        int bookQty;

        try {
            bookQty = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "请输入有效数量！");
            return;
        }

        if (selectedCompany == null || selectedFloor == null) {
            showAlert("Error", "请选择公司和楼层！");
            return;
        }

        BookingData target = allBookingList.stream()
                .filter(b -> b.getName().equals(selectedCompany) && b.getFloor().equals(selectedFloor))
                .findFirst().orElse(null);

        if (target == null) {
            showAlert("Error", "未找到房源！");
            return;
        }

        int availableQty = Integer.parseInt(target.getAvailable());
        if (availableQty <= 0) {
            showAlert("Error", "该房源已无可用数量！");
            return;
        }

        if (bookQty > availableQty) {
            showAlert("Error", "剩余可用数量不足！");
            return;
        }

        // 更新可用数量
        target.setAvailable(String.valueOf(availableQty - bookQty));
        saveAllBookings();

        // 更新用户自己的预订记录
        BookingData existing = userBookingList.stream()
                .filter(b -> b.getName().equals(selectedCompany) && b.getFloor().equals(selectedFloor))
                .findFirst().orElse(null);

        if (existing != null) {
            int newQty = Integer.parseInt(existing.getQuantity()) + bookQty;
            existing.setQuantity(String.valueOf(newQty));
        } else {
            userBookingList.add(new BookingData(
                    target.getName(), target.getLocation(), target.getFloor(),
                    target.getType(), target.getPrice(), String.valueOf(bookQty), "0"
            ));
        }

        saveUserBookings();
        userBookingTable.refresh();
        showAlert("Success", "预订成功！");
    }

    private void saveAllBookings() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKING_FILE))) {
            for (BookingData b : allBookingList) {
                bw.write(String.join(",", b.getName(), b.getLocation(), b.getFloor(),
                        b.getType(), b.getPrice(), b.getQuantity(), b.getAvailable()));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUserBookings() {
        try {
            List<String> lines = new ArrayList<>();
            File file = new File(USERBOOKING_FILE);
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 7 && !data[0].equals(currentUser)) {
                            lines.add(line);
                        }
                    }
                }
            }

            for (BookingData b : userBookingList) {
                lines.add(String.join(",", currentUser, b.getName(), b.getLocation(), b.getFloor(),
                        b.getType(), b.getPrice(), b.getQuantity()));
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (String l : lines) {
                    bw.write(l);
                    bw.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // 内部类
    public static class BookingData {
        private final SimpleStringProperty name, location, floor, type, price, quantity, available;

        public BookingData(String name, String location, String floor, String type, String price, String quantity, String available) {
            this.name = new SimpleStringProperty(name);
            this.location = new SimpleStringProperty(location);
            this.floor = new SimpleStringProperty(floor);
            this.type = new SimpleStringProperty(type);
            this.price = new SimpleStringProperty(price);
            this.quantity = new SimpleStringProperty(quantity);
            this.available = new SimpleStringProperty(available);
        }

        public String getName() { return name.get(); }
        public void setName(String val) { name.set(val); }
        public SimpleStringProperty nameProperty() { return name; }

        public String getLocation() { return location.get(); }
        public void setLocation(String val) { location.set(val); }
        public SimpleStringProperty locationProperty() { return location; }

        public String getFloor() { return floor.get(); }
        public void setFloor(String val) { floor.set(val); }
        public SimpleStringProperty floorProperty() { return floor; }

        public String getType() { return type.get(); }
        public void setType(String val) { type.set(val); }
        public SimpleStringProperty typeProperty() { return type; }

        public String getPrice() { return price.get(); }
        public void setPrice(String val) { price.set(val); }
        public SimpleStringProperty priceProperty() { return price; }

        public String getQuantity() { return quantity.get(); }
        public void setQuantity(String val) { quantity.set(val); }
        public SimpleStringProperty quantityProperty() { return quantity; }

        public String getAvailable() { return available.get(); }
        public void setAvailable(String val) { available.set(val); }
        public SimpleStringProperty availableProperty() { return available; }
    }
}
