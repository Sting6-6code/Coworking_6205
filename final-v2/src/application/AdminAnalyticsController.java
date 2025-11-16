package application;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Booking;
import model.Space;
import model.Transaction;
import model.User;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminAnalyticsController {

    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalBookingsLabel;
    @FXML
    private Label activeUsersLabel;
    @FXML
    private Label popularTypeLabel;
    @FXML
    private Label memberRatioLabel;
    @FXML
    private Label avgSpendingLabel;
    @FXML
    private TableView<BuildingRevenueData> buildingRevenueTable;
    @FXML
    private TableColumn<BuildingRevenueData, String> colBuilding;
    @FXML
    private TableColumn<BuildingRevenueData, Double> colRevenue;
    @FXML
    private TableColumn<BuildingRevenueData, Integer> colBookings;

    private static final String TRANSACTIONS_FILE = "data/transactions.csv";
    private static final String BOOKINGS_FILE = "data/bookings.csv";
    private static final String SPACES_FILE = "data/spaces.csv";
    private static final String USERS_FILE = "data/data.csv";

    @FXML
    public void initialize() {
        setupTable();
        refreshAnalytics();
    }

    private void setupTable() {
        colBuilding.setCellValueFactory(data -> data.getValue().buildingProperty());
        colRevenue.setCellValueFactory(data -> data.getValue().revenueProperty().asObject());
        colBookings.setCellValueFactory(data -> data.getValue().bookingsProperty().asObject());
        
        // Format revenue column
        colRevenue.setCellFactory(column -> new TableCell<BuildingRevenueData, Double>() {
            @Override
            protected void updateItem(Double revenue, boolean empty) {
                super.updateItem(revenue, empty);
                if (empty || revenue == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", revenue));
                }
            }
        });
    }

    @FXML
    private void refreshAnalytics() {
        // Load all data
        List<Transaction> transactions = loadTransactions();
        List<Booking> bookings = loadBookings();
        Map<String, Space> spaces = loadSpaces();
        List<User> users = loadUsers();

        // Calculate statistics
        calculateStatistics(transactions, bookings, spaces, users);
    }

    private List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;
                
                try {
                    transactions.add(Transaction.fromCSV(line));
                } catch (Exception e) {
                    System.err.println("Error parsing transaction: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return transactions;
    }

    private List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;
                
                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 7) {
                        Booking booking = new Booking(
                            parts[0], parts[1], parts[2],
                            LocalDate.parse(parts[3]),
                            java.time.LocalTime.parse(parts[4]),
                            java.time.LocalTime.parse(parts[5]),
                            parts[6]
                        );
                        bookings.add(booking);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing booking: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return bookings;
    }

    private Map<String, Space> loadSpaces() {
        Map<String, Space> spaces = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(SPACES_FILE))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;
                
                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 9) {
                        Space space = new Space(
                            parts[0], parts[1], parts[2], parts[3], parts[4], parts[5],
                            Integer.parseInt(parts[6]), parts[7], Double.parseDouble(parts[8])
                        );
                        spaces.put(space.getSpaceId(), space);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing space: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return spaces;
    }

    private List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    users.add(User.fromCSV(line));
                } catch (Exception e) {
                    System.err.println("Error parsing user: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    private void calculateStatistics(List<Transaction> transactions, List<Booking> bookings,
                                    Map<String, Space> spaces, List<User> users) {
        
        // Total Revenue
        double totalRevenue = transactions.stream()
            .mapToDouble(Transaction::getAmount)
            .sum();
        totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
        
        // Total Bookings
        long totalBookings = bookings.stream()
            .filter(b -> "booked".equals(b.getStatus()))
            .count();
        totalBookingsLabel.setText(String.valueOf(totalBookings));
        
        // Active Users (users who have made at least one transaction)
        Set<String> activeUserIds = new HashSet<>();
        transactions.forEach(t -> activeUserIds.add(t.getUserId()));
        activeUsersLabel.setText(String.valueOf(activeUserIds.size()));
        
        // Most Popular Space Type
        Map<String, Integer> typeCount = new HashMap<>();
        bookings.stream()
            .filter(b -> "booked".equals(b.getStatus()))
            .forEach(b -> {
                Space space = spaces.get(b.getSpaceId());
                if (space != null) {
                    typeCount.put(space.getType(), typeCount.getOrDefault(space.getType(), 0) + 1);
                }
            });
        
        String popularType = typeCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        popularTypeLabel.setText(popularType);
        
        // Member vs Non-Member
        long members = users.stream()
            .filter(u -> "Member".equals(u.getMembership()))
            .count();
        long nonMembers = users.size() - members;
        memberRatioLabel.setText(members + " / " + nonMembers);
        
        // Average Spending
        double avgSpending = activeUserIds.isEmpty() ? 0.0 :
            totalRevenue / activeUserIds.size();
        avgSpendingLabel.setText(String.format("$%.2f", avgSpending));
        
        // Revenue by Building
        Map<String, BuildingRevenueData> buildingData = new HashMap<>();
        
        transactions.stream()
            .filter(t -> t.getType() == Transaction.TransactionType.BOOKING)
            .forEach(t -> {
                // Find the booking to get spaceId
                Booking booking = bookings.stream()
                    .filter(b -> b.getBookingId().equals(t.getRelatedId()))
                    .findFirst()
                    .orElse(null);
                
                if (booking != null) {
                    Space space = spaces.get(booking.getSpaceId());
                    if (space != null) {
                        String building = space.getBuilding();
                        BuildingRevenueData data = buildingData.getOrDefault(building, 
                            new BuildingRevenueData(building));
                        data.addRevenue(t.getAmount());
                        data.incrementBookings();
                        buildingData.put(building, data);
                    }
                }
            });
        
        ObservableList<BuildingRevenueData> buildingList = FXCollections.observableArrayList(buildingData.values());
        buildingList.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));
        buildingRevenueTable.setItems(buildingList);
    }

    // Inner class for building revenue data
    public static class BuildingRevenueData {
        private final SimpleStringProperty building;
        private final SimpleDoubleProperty revenue;
        private final SimpleIntegerProperty bookings;

        public BuildingRevenueData(String building) {
            this.building = new SimpleStringProperty(building);
            this.revenue = new SimpleDoubleProperty(0.0);
            this.bookings = new SimpleIntegerProperty(0);
        }

        public String getBuilding() { return building.get(); }
        public void setBuilding(String value) { building.set(value); }
        public SimpleStringProperty buildingProperty() { return building; }

        public double getRevenue() { return revenue.get(); }
        public void setRevenue(double value) { revenue.set(value); }
        public SimpleDoubleProperty revenueProperty() { return revenue; }
        public void addRevenue(double amount) { revenue.set(revenue.get() + amount); }

        public int getBookings() { return bookings.get(); }
        public void setBookings(int value) { bookings.set(value); }
        public SimpleIntegerProperty bookingsProperty() { return bookings; }
        public void incrementBookings() { bookings.set(bookings.get() + 1); }
    }
}

