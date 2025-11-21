package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Transaction;
import util.CurrentUser;
import util.TransactionDataUtil;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class overview {

    @FXML private Label totalBillLabel;
    @FXML private Label recentBookingLabel;

    private String currentUserId;

    @FXML
    public void initialize() {

        // 获取当前用户 ID
        if (CurrentUser.get() != null) {
            currentUserId = CurrentUser.get().getUserId();
        }

        if (currentUserId == null) {
            System.err.println("ERROR: currentUserId is null in OverviewController");
            totalBillLabel.setText("N/A");
            recentBookingLabel.setText("N/A");
            return;
        }

        loadOverviewData();
    }

    private void loadOverviewData() {

        // 确保从 CSV 最新读取
        TransactionDataUtil.loadTransactions();

        List<Transaction> all = TransactionDataUtil.getAllTransactions();

        // 筛选当前用户所有记录
        List<Transaction> userTx = all.stream()
                .filter(tx -> tx.getUserId().equals(currentUserId))
                .collect(Collectors.toList());

        // ------- 1️⃣ 总账单 -------
        double totalBill = userTx.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        totalBillLabel.setText(String.format("$ %.2f", totalBill));

        // ------- 2️⃣ 最近一天预定（最新 BOOKING）-------
        List<Transaction> bookings = userTx.stream()
                .filter(tx -> tx.getType() == Transaction.TransactionType.BOOKING)
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .collect(Collectors.toList());

        if (bookings.isEmpty()) {
            recentBookingLabel.setText("No recent booking");
        } else {
            Transaction latest = bookings.get(0);
            recentBookingLabel.setText(
                String.format("%s | %s | $%.2f",
                        latest.getDate(),
                        latest.getDescription(),
                        latest.getAmount()
                )
            );
        }

        System.out.println("Overview loaded: total=" + totalBill + ", bookings=" + bookings.size());
    }
}
