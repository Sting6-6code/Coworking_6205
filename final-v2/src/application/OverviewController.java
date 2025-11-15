package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class OverviewController {

    @FXML
    private Label userCountLabel;

    private static final String DATA_FILE = "data/data.csv";

    @FXML
    public void initialize() {
        int userCount = countUsers();
        userCountLabel.setText(String.valueOf(userCount));
    }

    /**
     * 统计 data.csv 中的用户数量
     */
    private int countUsers() {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
}
