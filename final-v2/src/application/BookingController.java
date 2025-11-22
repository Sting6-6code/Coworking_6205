package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Booking;
import model.Space;
import model.Transaction;
import model.viewmodel.SpaceInventoryTableModel;
import util.BookingDataUtil;
import util.CurrentUser;
import util.TransactionDataUtil;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BookingController {

    @FXML private ChoiceBox<String> companyChoice;
    @FXML private ChoiceBox<String> floorChoice;
    @FXML private TextField quantityField;
    @FXML private TableView<SpaceInventoryTableModel> userBookingTable;

    @FXML private TableColumn<SpaceInventoryTableModel, String> colName;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colLocation;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colFloor;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colType;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colPrice;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colQuantity;
    @FXML private TableColumn<SpaceInventoryTableModel, String> colAvailable; // New column to display available quantity

    private static final String BOOKING_FILE = "data/booking.csv";
    private static final String USERBOOKING_FILE = "data/userbooking.csv";
    private static final String SPACES_FILE = "data/spaces.csv";

    private ObservableList<SpaceInventoryTableModel> allBookingList = FXCollections.observableArrayList();
    private ObservableList<SpaceInventoryTableModel> userBookingList = FXCollections.observableArrayList();

    private static String currentUser;

    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    @FXML
    public void initialize() {
        // Get username from CurrentUser and set currentUser
        if (CurrentUser.get() != null) {
            currentUser = CurrentUser.get().getUsername();
            System.out.println("BookingController initialized with user: " + currentUser);
        } else {
            System.err.println("WARNING: CurrentUser is null in BookingController!");
        }
        
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

    @FXML private TextField availableField; // Display available quantity

    private void populateCompanyChoice() {
        List<String> companies = allBookingList.stream()
                .map(SpaceInventoryTableModel::getName)
                .distinct()
                .collect(Collectors.toList());
        companyChoice.setItems(FXCollections.observableArrayList(companies));

        companyChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Update floor selection
            List<String> floors = allBookingList.stream()
                    .filter(b -> b.getName().equals(newVal))
                    .map(SpaceInventoryTableModel::getFloor)
                    .distinct()
                    .collect(Collectors.toList());
            floorChoice.setItems(FXCollections.observableArrayList(floors));

            // Clear Available
            availableField.setText("");
        });

        floorChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            String selectedCompany = companyChoice.getValue();
            if (selectedCompany != null && newVal != null) {
                SpaceInventoryTableModel target = allBookingList.stream()
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
                    if (data.length < 7) continue; // Includes Available
                    allBookingList.add(new SpaceInventoryTableModel(
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
        
        // Ensure currentUser is set
        if (currentUser == null && CurrentUser.get() != null) {
            currentUser = CurrentUser.get().getUsername();
        }
        
        if (currentUser == null) {
            System.err.println("WARNING: currentUser is null, cannot load user bookings!");
            return;
        }
        
        // Get userId for matching (userbooking.csv may store userId or username)
        String userId = CurrentUser.get() != null ? CurrentUser.get().getUserId() : null;
        
        System.out.println("Loading bookings for user: " + currentUser + " (userId: " + userId + ")");
        
        int oldSystemCount = 0;
        int newSystemCount = 0;
        
        // 1. Load bookings from old system (from userbooking.csv)
        try (BufferedReader br = new BufferedReader(new FileReader(USERBOOKING_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] data = line.split(",");
                    if (data.length < 7) continue;
                    String firstField = data[0];
                    
                    // Match username or userId (compatible with old data)
                    boolean matches = false;
                    if (firstField != null && !firstField.equals("null")) {
                        if (firstField.equals(currentUser)) {
                            matches = true; // Match username
                        } else if (userId != null && firstField.equals(userId)) {
                            matches = true; // Match userId
                        }
                    }
                    
                    if (matches) {
                    	int quantity = 1;
                    	try {
                    	    quantity = Integer.parseInt(data[6]);
                    	} catch (NumberFormatException e) {
                    	    quantity = 1;
                    	}

                    	// Directly add a record, quantity is the value from CSV
                    	userBookingList.add(new SpaceInventoryTableModel(
                    	        data[1], data[2], data[3], data[4], data[5], String.valueOf(quantity), "0"
                    	));
                    	oldSystemCount++;
                    }
                }
            }
            System.out.println("Loaded " + oldSystemCount + " bookings from old system (userbooking.csv)");
        } catch (FileNotFoundException e) {
            // Create file if it doesn't exist
            try {
                new File(USERBOOKING_FILE).createNewFile();
                System.out.println("Created new userbooking.csv file");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("ERROR loading user bookings: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 2. Load bookings from new system (from BookingDataUtil, i.e., bookings.csv)
        if (userId != null) {
            try {
                // Reload bookings to ensure latest data
                BookingDataUtil.loadBookings();
                
                // Get all bookings for current user (only including status "booked")
                List<Booking> newSystemBookings = BookingDataUtil.getAllBookings().stream()
                        .filter(b -> b.getUserId().equals(userId) && "booked".equalsIgnoreCase(b.getStatus()))
                        .collect(Collectors.toList());
                
                // Load Space information for conversion
                Map<String, Space> spacesMap = loadSpaces();
                
                // Convert new system bookings to SpaceInventoryTableModel
                for (Booking booking : newSystemBookings) {
                    Space space = spacesMap.get(booking.getSpaceId());
                    if (space != null) {
                        // Calculate booking duration (hours)
                        long hours = ChronoUnit.HOURS.between(booking.getStartTime(), booking.getEndTime());
                        if (hours == 0) {
                            // If less than 1 hour, calculate by minutes and convert to hours (keep 1 decimal place)
                            long minutes = ChronoUnit.MINUTES.between(booking.getStartTime(), booking.getEndTime());
                            hours = minutes > 0 ? 1 : 0; // At least 1 hour
                        }
                        
                        // Calculate total price: creditsPerHour * hours
                        double totalPrice = space.getCreditsPerHour() * hours;
                        
                        // Create description: includes date and time information
                        String description = String.format("%s (%s) - %s %s-%s", 
                                space.getName(), space.getType(), 
                                booking.getDate(), booking.getStartTime(), booking.getEndTime());
                        
                        // Add to list (use space's name as Name, building as Location, floor as Floor)
                        userBookingList.add(new SpaceInventoryTableModel(
                                space.getName(),                    // Name
                                space.getBuilding(),                // Location (building)
                                space.getFloor(),                   // Floor
                                space.getType(),                    // Type
                                String.format("%.2f", totalPrice),  // Price (total price)
                                "1",                                // Quantity (each booking in new system is 1)
                                "0"                                 // Available (not applicable)
                        ));
                        newSystemCount++;
                    } else {
                        System.err.println("WARNING: Space not found for spaceId: " + booking.getSpaceId());
                    }
                }
                
                System.out.println("Loaded " + newSystemCount + " bookings from new system (bookings.csv)");
            } catch (Exception e) {
                System.err.println("ERROR loading new system bookings: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("WARNING: userId is null, cannot load new system bookings!");
        }
        
        System.out.println("Total bookings loaded: " + userBookingList.size() + " (old: " + oldSystemCount + ", new: " + newSystemCount + ")");
    }
    
    /**
     * Load all Space information from spaces.csv
     */
    private Map<String, Space> loadSpaces() {
        Map<String, Space> spacesMap = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(SPACES_FILE))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                if (line.trim().isEmpty()) continue;
                
                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 9) {
                        Space space = new Space(
                                parts[0].trim(),                                    // id
                                parts[1].trim(),                                    // name
                                parts[2].trim(),                                    // floor
                                parts[3].trim(),                                    // spaceId
                                parts[4].trim(),                                    // type
                                parts[5].trim(),                                    // building
                                Integer.parseInt(parts[6].trim()),                  // capacity
                                parts[7].trim(),                                    // status
                                Double.parseDouble(parts[8].trim())                 // creditsPerHour
                        );
                        spacesMap.put(space.getSpaceId(), space);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing space line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Spaces file not found: " + SPACES_FILE);
        } catch (IOException e) {
            System.err.println("ERROR loading spaces: " + e.getMessage());
            e.printStackTrace();
        }
        
        return spacesMap;
    }

    @FXML
    private void bookRoom() {
        // Ensure currentUser is set
        if (currentUser == null && CurrentUser.get() != null) {
            currentUser = CurrentUser.get().getUsername();
        }

        if (currentUser == null) {
            showAlert("Error", "User not logged in!");
            return;
        }

        String selectedCompany = companyChoice.getValue();
        String selectedFloor = floorChoice.getValue();
        int bookQty;

        try {
            bookQty = Integer.parseInt(quantityField.getText());
            if (bookQty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid quantity!");
            return;
        }

        if (selectedCompany == null || selectedFloor == null) {
            showAlert("Error", "Please select company and floor!");
            return;
        }

        SpaceInventoryTableModel target = allBookingList.stream()
                .filter(b -> b.getName().equals(selectedCompany) && b.getFloor().equals(selectedFloor))
                .findFirst().orElse(null);

        if (target == null) {
            showAlert("Error", "Space not found!");
            return;
        }

        int availableQty = Integer.parseInt(target.getAvailable());
        if (availableQty <= 0) {
            showAlert("Error", "This space has no available quantity!");
            return;
        }

        if (bookQty > availableQty) {
            showAlert("Error", "Insufficient available quantity!");
            return;
        }

        // Update available quantity
        target.setAvailable(String.valueOf(availableQty - bookQty));
        saveAllBookings();

        // Get userId
        String userId = null;
        if (CurrentUser.get() != null) {
            userId = CurrentUser.get().getUserId();
        } else if (currentUser != null) {
            userId = getUserIdFromUsername(currentUser);
        }

        if (userId == null) {
            System.err.println("ERROR: Cannot get userId in BookingController! currentUser: " + currentUser);
            showAlert("Error", "Unable to get user ID!");
            return;
        }

        // Get Space information
        Map<String, Space> spacesMap = loadSpaces();
        Space space = spacesMap.values().stream()
                .filter(s -> s.getName().equals(target.getName()) && s.getType().equals(target.getType()))
                .findFirst()
                .orElse(null);

        double pricePerUnit = Double.parseDouble(target.getPrice());
        double totalPrice = pricePerUnit * bookQty;

        // 1. Add to user table list (only add one record, quantity = user input)
        userBookingList.add(new SpaceInventoryTableModel(
                target.getName(),
                target.getLocation(),
                target.getFloor(),
                target.getType(),
                target.getPrice(),
                String.valueOf(bookQty), // User input quantity
                "0"
        ));

        // 2. Add Transaction (merge quantity)
        try {
            String description;
            if (space != null) {
                description = String.format("Booking: %s (%s) [Building: %s] - Quantity: %d",
                        target.getName(), target.getType(), space.getBuilding(), bookQty);
            } else {
                description = String.format("Booking: %s (%s) - Quantity: %d",
                        target.getName(), target.getType(), bookQty);
            }

            String bookingId = UUID.randomUUID().toString();
            Transaction transaction = new Transaction(
                    userId,
                    Transaction.TransactionType.BOOKING,
                    totalPrice, // Total price
                    LocalDate.now(),
                    description,
                    bookingId
            );

            TransactionDataUtil.addTransaction(transaction);
            System.out.println("Transaction created: " + transaction.getTransactionId() + ", userId: " + userId);
        } catch (Exception e) {
            System.err.println("ERROR creating transaction: " + e.getMessage());
            e.printStackTrace();
        }

        // 3. Write to userbooking.csv (only write one record, quantity = user input)
        appendUserBooking(
                target.getName(),
                target.getLocation(),
                target.getFloor(),
                target.getType(),
                target.getPrice(),
                String.valueOf(bookQty)
        );

        // Refresh table
        userBookingTable.setItems(null);
        userBookingTable.setItems(userBookingList);
        userBookingTable.refresh();

        System.out.println("Table refreshed. Total items: " + userBookingList.size());
        showAlert("Success", "Booking successful!");
    }

    private void saveAllBookings() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKING_FILE))) {
            for (SpaceInventoryTableModel b : allBookingList) {
                bw.write(String.join(",", b.getName(), b.getLocation(), b.getFloor(),
                        b.getType(), b.getPrice(), b.getQuantity(), b.getAvailable()));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendUserBooking(
            String name,
            String location,
            String floor,
            String type,
            String price,
            String quantity
    ) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERBOOKING_FILE, true))) {

            bw.write(String.join(",",
                    currentUser,
                    name,
                    location,
                    floor,
                    type,
                    price,
                    quantity        // Save the input quantity
            ));
            bw.newLine();

            System.out.println("Appended user booking: " + name + " qty=" + quantity);

        } catch (IOException e) {
            System.err.println("ERROR appending user booking: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Get userId from username by looking up in data.csv
     */
    private String getUserIdFromUsername(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader("data/data.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[1].equals(username)) {
                    return parts[0]; // Return userId (first field)
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading data.csv: " + e.getMessage());
        }
        return null;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
