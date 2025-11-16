package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Transaction;
import model.viewmodel.TransactionTableModel;
import util.CurrentUser;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BillingController {

    @FXML
    private TableView<TransactionTableModel> transactionTable;
    @FXML
    private TableColumn<TransactionTableModel, String> colDate;
    @FXML
    private TableColumn<TransactionTableModel, String> colType;
    @FXML
    private TableColumn<TransactionTableModel, String> colDescription;
    @FXML
    private TableColumn<TransactionTableModel, Double> colAmount;
    @FXML
    private ChoiceBox<String> typeFilterChoice;
    @FXML
    private Label totalAmountLabel;

    private static final String TRANSACTIONS_FILE = "data/transactions.csv";
    private ObservableList<TransactionTableModel> allTransactions = FXCollections.observableArrayList();
    private FilteredList<TransactionTableModel> filteredTransactions;

    @FXML
    public void initialize() {
        setupTable();
        setupFilter();
        loadTransactions();
    }

    private void setupTable() {
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

    private void setupFilter() {
        typeFilterChoice.getItems().addAll("All", "BOOKING", "MEMBERSHIP");
        typeFilterChoice.setValue("All");
        typeFilterChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterTransactions();
        });
    }

    private void loadTransactions() {
        allTransactions.clear();
        
        String currentUserId = CurrentUser.get() != null ? CurrentUser.get().getUserId() : null;
        if (currentUserId == null) {
            showAlert("Error", "User not logged in!");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) continue;
                
                try {
                    Transaction transaction = Transaction.fromCSV(line);
                    
                    // Only show transactions for current user
                    if (transaction.getUserId().equals(currentUserId)) {
                        String dateStr = transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        String typeStr = transaction.getType().name();
                        String description = transaction.getDescription();
                        double amount = transaction.getAmount();
                        
                        allTransactions.add(new TransactionTableModel(dateStr, typeStr, description, amount));
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing transaction: " + line);
                    e.printStackTrace();
                }
            }
            
            // Sort by date (newest first)
            allTransactions.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            
            filterTransactions();
            updateTotal();
            
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, create it
            createEmptyTransactionsFile();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load transactions: " + e.getMessage());
        }
    }

    private void filterTransactions() {
        String selectedType = typeFilterChoice.getValue();
        
        if (selectedType == null || "All".equals(selectedType)) {
            transactionTable.setItems(allTransactions);
        } else {
            ObservableList<TransactionTableModel> filtered = FXCollections.observableArrayList();
            for (TransactionTableModel t : allTransactions) {
                if (selectedType.equals(t.getType())) {
                    filtered.add(t);
                }
            }
            transactionTable.setItems(filtered);
        }
        
        updateTotal();
    }

    private void updateTotal() {
        double total = 0.0;
        ObservableList<TransactionTableModel> items = transactionTable.getItems();
        
        for (TransactionTableModel t : items) {
            total += t.getAmount();
        }
        
        totalAmountLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    private void refreshData() {
        loadTransactions();
    }

    private void createEmptyTransactionsFile() {
        try {
            File file = new File(TRANSACTIONS_FILE);
            file.getParentFile().mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("transactionId,userId,type,amount,date,description,relatedId");
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

