package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import model.User;
import util.CurrentUser; 

import java.io.IOException;

public class AdminController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentArea;


    @FXML
    public void initialize() {
        User user = CurrentUser.get();

        if (user != null && user.getUsername() != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        } else {
            welcomeLabel.setText("Welcome, Admin!");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            CurrentUser.set(null); 
            Main.changeScene("login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Cannot return to login screen.");
        }
    }


    @FXML private void showOverview()  { loadContent("admin_overview.fxml"); }
    @FXML private void showUsers()     { loadContent("admin_users.fxml"); }
    @FXML private void showSpaces()    { loadContent("admin_spaces.fxml"); }
    @FXML private void showBookings()  { loadContent("admin_bookings.fxml"); }
    @FXML private void showBilling()   { loadContent("admin_billing.fxml"); }
    @FXML private void showAnalytics() { loadContent("admin_analytics.fxml"); }
    @FXML private void showQuestions() { loadContent("adminquestion.fxml"); }


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
