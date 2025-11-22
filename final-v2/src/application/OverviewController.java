package application;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class OverviewController {

    @FXML private LineChart<String, Number> dailySalesChart;
    @FXML private TableView<TransactionRow> recentTransactionTable;
    @FXML private TableColumn<TransactionRow, String> usernameCol;
    @FXML private TableColumn<TransactionRow, String> dateCol;
    @FXML private TableColumn<TransactionRow, String> typeCol;
    @FXML private TableColumn<TransactionRow, String> amountCol;

    @FXML private javafx.scene.control.Label userCountLabel;
    @FXML private javafx.scene.control.Label transactionCountLabel;
    @FXML private javafx.scene.control.Label totalAmountLabel;

    private static final String TRANSACTION_FILE = "data/transactions.csv";
    private static final String USER_FILE = "data/data.csv"; // userId -> username

    @FXML
    public void initialize() {
        setupTransactionTable();
        loadSummary();
        loadRecentTransactions();
        loadDailySales();
    }

    private void setupTransactionTable() {
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    private void loadSummary() {
        int totalUsers = 0;
        int totalTransactions = 0;
        double totalAmount = 0.0;

        // Count users
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            br.readLine(); // skip header
            while (br.readLine() != null) totalUsers++;
        } catch (Exception e) { e.printStackTrace(); }

        // Count transactions & total amount
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 7) continue;
                totalTransactions++;
                totalAmount += Double.parseDouble(p[3]);
            }
        } catch (Exception e) { e.printStackTrace(); }

        userCountLabel.setText(String.valueOf(totalUsers));
        transactionCountLabel.setText(String.valueOf(totalTransactions));
        totalAmountLabel.setText(String.format("%.2f", totalAmount));
    }

    private void loadRecentTransactions() {
        List<TransactionRow> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 7) continue;
                String userId = p[1];
                String username = getUsernameById(userId);
                String type = p[2];
                String amount = p[3];
                String date = p[4];
                list.add(new TransactionRow(username, date, type, amount));
            }
        } catch (Exception e) { e.printStackTrace(); }

        Collections.reverse(list);
        List<TransactionRow> recent5 = list.stream().limit(5).collect(Collectors.toList());
        recentTransactionTable.setItems(FXCollections.observableArrayList(recent5));
    }

    private void loadDailySales() {
        Map<String, Double> salesMap = new TreeMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 7) continue;
                String date = p[4];
                double amount = Double.parseDouble(p[3]);
                salesMap.put(date, salesMap.getOrDefault(date, 0.0) + amount);
            }
        } catch (Exception e) { e.printStackTrace(); }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales");
        salesMap.forEach((date, amount) -> series.getData().add(new XYChart.Data<>(date, amount)));
        dailySalesChart.getData().add(series);
    }

    private String getUsernameById(String userId) {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 2 && p[0].equals(userId)) return p[1];
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "Unknown";
    }

    public static class TransactionRow {
        private final String username;
        private final String date;
        private final String type;
        private final String amount;

        public TransactionRow(String username, String date, String type, String amount) {
            this.username = username;
            this.date = date;
            this.type = type;
            this.amount = amount;
        }

        public String getUsername() { return username; }
        public String getDate() { return date; }
        public String getType() { return type; }
        public String getAmount() { return amount; }
    }
}
