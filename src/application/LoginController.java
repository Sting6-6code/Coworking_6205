package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import util.CurrentUser;

import java.io.*;
import java.util.*;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    private static final String DATA_FILE = "data/data.csv";

    @FXML
    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password!");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] f = line.split(",");

                // CSV 格式：
                // userId, username, password, email, type, membership
                if (f.length < 6) continue;

                String userId    = f[0];
                String uname     = f[1];
                String pwd       = f[2];
                String email     = f[3];
                String type      = f[4];
                String membership = f[5];

                if (uname.equals(username) && pwd.equals(password)) {

                    // === 创建 User 对象 ===
                    User current = new User(
                            userId,
                            uname,
                            pwd,
                            email,
                            type,
                            membership
                    );

                    // === 存入 CurrentUser 以便后续 booking 使用 ===
                    CurrentUser.set(current);

                    // === 跳转界面 ===
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            type.equalsIgnoreCase("Admin") ? "admin.fxml" : "user.fxml"
                    ));

                    Scene scene = new Scene(loader.load(), 600, 400);
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(scene);
                    return;
                }
            }

            showAlert("Error", "Invalid username or password!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Login failed due to a system error.");
        }
    }

    @FXML
    private void goToRegister() {
        try {
            Main.changeScene("register.fxml");
        } catch (Exception e) {
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
