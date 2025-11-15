package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class UserController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentArea;

    private static String currentUsername;

    public static void setCurrentUser(String username) {
        currentUsername = username;
    }

    @FXML
    public void initialize() {
        if (currentUsername != null) {
            welcomeLabel.setText("Welcome, " + currentUsername + "!");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Main.changeScene("login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Cannot return to login screen.");
        }
    }

    // 点击侧边栏按钮加载不同的 FXML
    @FXML private void showOverview()  { loadContent("overview.fxml"); }
    @FXML private void showUsers()     { loadContent("users.fxml"); }
    @FXML private void showSpaces()    { loadContent("spaces.fxml"); }
    @FXML private void showBookings()  { loadContent("bookings.fxml"); }
    @FXML private void showBilling()   { loadContent("billing.fxml"); }

    private void loadContent(String fxmlFile) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load " + fxmlFile);
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
