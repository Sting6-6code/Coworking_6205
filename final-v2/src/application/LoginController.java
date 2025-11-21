package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.User;
import util.CurrentUser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
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
                if (f.length < 6) continue;

                String userId     = f[0];
                String uname      = f[1];
                String pwd        = f[2];
                String email      = f[3];
                String type       = f[4];
                String membership = f[5];

                // 匹配成功
                if (uname.equals(username) && pwd.equals(password)) {

                    // 保存当前用户
                    CurrentUser.set(new User(
                            userId, uname, pwd, email, type, membership
                    ));

                    // ⭐ 使用 Main.changeScene() 切换页面（固定窗口大小）
                    if (type.equalsIgnoreCase("Admin")) {
                        Main.changeScene("admin.fxml");
                    } else {
                        Main.changeScene("user.fxml");
                    }

                    return;
                }
            }

            showAlert("Error", "Invalid username or password!");

        } catch (IOException e) {
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
            showAlert("Error", "Cannot open the register page.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
