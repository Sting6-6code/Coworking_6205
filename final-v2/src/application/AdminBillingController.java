package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Transaction;
import model.User;
import model.viewmodel.TransactionTableModel;
import util.CurrentUser;
import util.TransactionDataUtil;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminBillingController {

    @FXML
    private TableView<TransactionTableModel> transactionTable;
    @FXML
    private TableColumn<TransactionTableModel, String> colUserId;
    @FXML
    private TableColumn<TransactionTableModel, String> colUsername;
    @FXML
    private TableColumn<TransactionTableModel, String> colDate;
    @FXML
    private TableColumn<TransactionTableModel, String> colType;
    @FXML
    private TableColumn<TransactionTableModel, String> colDescription;
    @FXML
    private TableColumn<TransactionTableModel, Double> colAmount;
    @FXML
    private ChoiceBox<String> userFilterChoice;
    @FXML
    private ChoiceBox<String> typeFilterChoice;
    @FXML
    private Label totalRevenueLabel;

    private static final String USERS_FILE = "data/data.csv";
    private ObservableList<TransactionTableModel> allTransactions = FXCollections.observableArrayList();
    private Map<String, String> userIdToUsernameMap = new HashMap<>();

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadUserMap();
        loadTransactions();
    }

    private void setupTable() {
        colUserId.setCellValueFactory(data -> data.getValue().userIdProperty());
        colUsername.setCellValueFactory(data -> data.getValue().usernameProperty());
        colDate.setCellValueFactory(data -> data.getValue().dateProperty());
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colDescription.setCellValueFactory(data -> data.getValue().descriptionProperty());
        colAmount.setCellValueFactory(data -> data.getValue().amountProperty().asObject());
        
        // Format amount column to show currency
        colAmount.setCellFactory(column -> new TableCell<TransactionTableModel, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });

        transactionTable.setItems(allTransactions);
    }

    private void setupFilters() {
        userFilterChoice.getItems().add("All Users");
        userFilterChoice.setValue("All Users");
        
        typeFilterChoice.getItems().addAll("All", "BOOKING", "MEMBERSHIP");
        typeFilterChoice.setValue("All");
        
        userFilterChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterTransactions();
        });
        
        typeFilterChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterTransactions();
        });
    }

    private void loadUserMap() {
        userIdToUsernameMap.clear();
        
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String userId = parts[0];
                    String username = parts[1];
                    userIdToUsernameMap.put(userId, username);
                    
                    // Add to user filter choice
                    if (!userFilterChoice.getItems().contains(username)) {
                        userFilterChoice.getItems().add(username);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTransactions() {
        allTransactions.clear();

        // Reload transactions from file
        TransactionDataUtil.reload();
        
        // Get all transactions
        List<Transaction> transactions = TransactionDataUtil.getAllTransactions();
        
        for (Transaction transaction : transactions) {
            String userId = transaction.getUserId();
            String username = userIdToUsernameMap.getOrDefault(userId, "Unknown");
            String dateStr = transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String typeStr = transaction.getType().name();
            String description = transaction.getDescription();
            double amount = transaction.getAmount();
            String transactionId = transaction.getTransactionId();
            
            allTransactions.add(new TransactionTableModel(
                transactionId, userId, username, dateStr, typeStr, description, amount
            ));
        }
        
        // Sort by date (newest first)
        allTransactions.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        filterTransactions();
        updateTotal();
    }

    private void filterTransactions() {
        String selectedUser = userFilterChoice.getValue();
        String selectedType = typeFilterChoice.getValue();
        
        ObservableList<TransactionTableModel> filtered = FXCollections.observableArrayList();
        
        for (TransactionTableModel t : allTransactions) {
            boolean userMatch = "All Users".equals(selectedUser) || 
                               selectedUser == null || 
                               selectedUser.equals(t.getUsername());
            
            boolean typeMatch = "All".equals(selectedType) || 
                               selectedType == null || 
                               selectedType.equals(t.getType());
            
            if (userMatch && typeMatch) {
                filtered.add(t);
            }
        }
        
        transactionTable.setItems(filtered);
        updateTotal();
    }

    private void updateTotal() {
        double total = 0.0;
        ObservableList<TransactionTableModel> items = transactionTable.getItems();
        
        for (TransactionTableModel t : items) {
            total += t.getAmount();
        }
        
        totalRevenueLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    private void refreshData() {
        loadUserMap();
        loadTransactions();
    }


    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

