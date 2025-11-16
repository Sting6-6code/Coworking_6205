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
import util.BookingDataUtil;
import util.TransactionDataUtil;

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
        System.out.println("Refreshing analytics data...");
        
        // Load all data
        List<Transaction> transactions = loadTransactions();
        List<Booking> bookings = loadBookings();
        Map<String, Space> spaces = loadSpaces();
        List<User> users = loadUsers();

        System.out.println("Loaded data - Transactions: " + transactions.size() + 
                          ", Bookings: " + bookings.size() + 
                          ", Spaces: " + spaces.size() + 
                          ", Users: " + users.size());

        // Calculate statistics
        calculateStatistics(transactions, bookings, spaces, users);
        
        System.out.println("Analytics refreshed successfully.");
    }

    private List<Transaction> loadTransactions() {
        // Reload transactions from file
        TransactionDataUtil.reload();
        
        // Get all transactions
        return new ArrayList<>(TransactionDataUtil.getAllTransactions());
    }

    private List<Booking> loadBookings() {
        // Reload bookings from file to ensure we have the latest data
        BookingDataUtil.loadBookings();
        
        // Get all bookings from BookingDataUtil
        return new ArrayList<>(BookingDataUtil.getAllBookings());
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
        final int[] processedCount = {0};
        final int[] buildingFoundCount = {0};
        final int[] buildingNotFoundCount = {0};
        
        transactions.stream()
            .filter(t -> t.getType() == Transaction.TransactionType.BOOKING)
            .forEach(t -> {
                processedCount[0]++;
                String building = null;
                
                // Method 1: Try to find booking by relatedId (new system)
                Booking booking = bookings.stream()
                    .filter(b -> b.getBookingId().equals(t.getRelatedId()))
                    .findFirst()
                    .orElse(null);
                
                if (booking != null) {
                    // New system: get building from booking's spaceId
                    Space space = spaces.get(booking.getSpaceId());
                    if (space != null) {
                        building = space.getBuilding();
                        System.out.println("Found building from booking: " + building + " for transaction: " + t.getTransactionId() + ", spaceId: " + booking.getSpaceId());
                    } else {
                        System.err.println("WARNING: Space not found for spaceId: " + booking.getSpaceId() + " in transaction: " + t.getTransactionId());
                    }
                } else {
                    // Method 2: Old system - parse description to find space
                    // Description format: "Booking: New1 (A) - Quantity: 1"
                    // or "Booking: New1 (A) [Building: A] - Quantity: 1" (new format with building)
                    // or "Booking: event_001 (event) - 2025-11-15 13:00-13:30"
                    String description = t.getDescription();
                    if (description != null && description.startsWith("Booking: ")) {
                        try {
                            // First, try to extract building directly from description if available
                            // Format: "Booking: ... [Building: X] - ..."
                            int buildingIndex = description.indexOf("[Building: ");
                            if (buildingIndex > 0) {
                                int buildingEnd = description.indexOf("]", buildingIndex);
                                if (buildingEnd > buildingIndex) {
                                    building = description.substring(buildingIndex + "[Building: ".length(), buildingEnd).trim();
                                    System.out.println("Found building from description: " + building + " for transaction: " + t.getTransactionId());
                                }
                            }
                            
                            // If building not found in description, try to find by space name
                            if (building == null) {
                                // Extract space name from description
                                // Format: "Booking: <name> (<type>) - ..."
                                String afterBooking = description.substring("Booking: ".length());
                                int parenIndex = afterBooking.indexOf(" (");
                                if (parenIndex > 0) {
                                    String spaceName = afterBooking.substring(0, parenIndex).trim();
                                    
                                    // Find space by name
                                    Space space = spaces.values().stream()
                                        .filter(s -> s.getName().equals(spaceName))
                                        .findFirst()
                                        .orElse(null);
                                    
                                    if (space != null) {
                                        building = space.getBuilding();
                                        System.out.println("Found building from space name: " + building + " for transaction: " + t.getTransactionId() + ", spaceName: " + spaceName);
                                    } else {
                                        System.out.println("WARNING: Space not found by name: " + spaceName + " in transaction: " + t.getTransactionId());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing description for transaction: " + t.getTransactionId() + ", description: " + description);
                            e.printStackTrace();
                        }
                    }
                }
                
                // Add to building data if building was found
                if (building != null && !building.isEmpty()) {
                    BuildingRevenueData data = buildingData.getOrDefault(building, 
                        new BuildingRevenueData(building));
                    data.addRevenue(t.getAmount());
                    data.incrementBookings();
                    buildingData.put(building, data);
                    buildingFoundCount[0]++;
                } else {
                    System.err.println("WARNING: Could not determine building for transaction: " + t.getTransactionId() + ", description: " + t.getDescription() + ", relatedId: " + t.getRelatedId());
                    buildingNotFoundCount[0]++;
                }
            });
        
        System.out.println("Building revenue calculation summary:");
        System.out.println("  Total BOOKING transactions processed: " + processedCount[0]);
        System.out.println("  Buildings found: " + buildingFoundCount[0]);
        System.out.println("  Buildings not found: " + buildingNotFoundCount[0]);
        System.out.println("  Unique buildings with revenue: " + buildingData.size());
        for (String b : buildingData.keySet()) {
            BuildingRevenueData data = buildingData.get(b);
            System.out.println("    Building " + b + ": Revenue=$" + data.getRevenue() + ", Bookings=" + data.getBookings());
        }
        
        // Only show buildings with revenue > 0 (filter out empty buildings)
        ObservableList<BuildingRevenueData> buildingList = FXCollections.observableArrayList();
        for (BuildingRevenueData data : buildingData.values()) {
            if (data.getRevenue() > 0) {
                buildingList.add(data);
            }
        }
        buildingList.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));
        buildingRevenueTable.setItems(buildingList);
        
        System.out.println("  Buildings displayed in table: " + buildingList.size());
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

