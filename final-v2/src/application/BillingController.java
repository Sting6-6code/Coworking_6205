package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Transaction;
import model.viewmodel.TransactionTableModel;
import util.CurrentUser;
import util.TransactionDataUtil;
import datastructure.TransactionBST;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
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

    private ObservableList<TransactionTableModel> allTransactions = FXCollections.observableArrayList();
    
    // ========== BST optimization: use binary search tree to maintain sorting ==========
    private TransactionBST transactionBST;
    // ====================================================

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
            System.err.println("ERROR: CurrentUser is null in BillingController!");
            showAlert("Error", "User not logged in!");
            return;
        }

        System.out.println("Loading transactions for userId: " + currentUserId);
        
        // Reload transactions from file
        TransactionDataUtil.reload();
        
        // Get all transactions first to debug
        List<Transaction> all = TransactionDataUtil.getAllTransactions();
        System.out.println("Total transactions in system: " + all.size());
        
        // Get transactions for current user
        List<Transaction> userTransactions = TransactionDataUtil.getTransactionsByUserId(currentUserId);
        System.out.println("Transactions for current user: " + userTransactions.size());
        
        // ========== BST optimization: use binary search tree to maintain sorting ==========
        // Create BST and insert all transactions (automatically sorted by date)
        transactionBST = new TransactionBST();
        for (Transaction transaction : userTransactions) {
            transactionBST.insert(transaction); // O(log n) insert, automatically maintains sorting
        }
        
        // In-order traversal of BST to get sorted list (from earliest to latest)
        List<Transaction> sortedTransactions = transactionBST.inOrderTraversal(); // O(n)
        
        // Reverse list (from latest to earliest, newest first)
        Collections.reverse(sortedTransactions);
        
        // Convert to TableModel
        for (Transaction transaction : sortedTransactions) {
            String dateStr = transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String typeStr = transaction.getType().name();
            String description = transaction.getDescription();
            double amount = transaction.getAmount();
            
            System.out.println("Adding transaction to table: " + description + ", amount: " + amount);
            allTransactions.add(new TransactionTableModel(dateStr, typeStr, description, amount));
        }
        
        // Before optimization: use Collections.sort() - O(n log n)
        // allTransactions.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        // After optimization: use BST - automatically sorted on insert O(n log n), but supports advanced operations like range queries
        System.out.println("BST size: " + transactionBST.size() + ", Total transactions in table: " + allTransactions.size());
        // ====================================================
        
        filterTransactions();
        updateTotal();
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
        System.out.println("Refreshing billing data...");
        loadTransactions();
        System.out.println("Billing data refreshed. Total transactions: " + allTransactions.size());
    }


    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

