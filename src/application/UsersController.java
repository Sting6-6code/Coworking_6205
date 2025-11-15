package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.*;

public class UsersController {

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label membershipLabel;
    @FXML private Button editButton;
    @FXML private Button upgradeButton;

    private static final String DATA_FILE = "data/data.csv";
    
    // ✅ 静态字段用于跨控制器共享用户名
    private static String currentUser;

    private String currentEmail;
    private String currentMembership;
    private String currentPassword;

    // ✅ 提供静态设置方法（由 LoginController 调用）
    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    @FXML
    public void initialize() {
        if (currentUser == null || currentUser.isEmpty()) {
            System.err.println("⚠️ currentUser is null! Make sure setCurrentUser() was called before loading this FXML.");
            return;
        }
        loadUserData();
    }

    private void loadUserData() {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] f = line.split(",");
                if (f.length >= 5 && f[0].equals(currentUser)) {
                    currentPassword = f[1];
                    currentEmail = f[2];
                    currentMembership = f[4].equalsIgnoreCase("Member") ? "Member" : "Non-member";

                    usernameLabel.setText(currentUser);
                    emailLabel.setText(currentEmail);
                    membershipLabel.setText(currentMembership);
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditProfile() {
        TextInputDialog pwdDialog = new TextInputDialog();
        pwdDialog.setTitle("Password Verification");
        pwdDialog.setHeaderText("Please enter your password to edit profile:");
        Optional<String> result = pwdDialog.showAndWait();

        if (result.isPresent() && result.get().equals(currentPassword)) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Profile");

            TextField usernameField = new TextField(currentUser);
            TextField emailField = new TextField(currentEmail);
            PasswordField newPwdField = new PasswordField();
            newPwdField.setPromptText("Enter new password");

            VBox vbox = new VBox(10, new Label("Username:"), usernameField,
                    new Label("Email:"), emailField,
                    new Label("New Password:"), newPwdField);
            dialog.getDialogPane().setContent(vbox);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> update = dialog.showAndWait();
            if (update.isPresent() && update.get() == ButtonType.OK) {
                updateUserInfo(usernameField.getText(), emailField.getText(), newPwdField.getText());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Incorrect password!");
        }
    }

    @FXML
    private void handleUpgradeMembership() {
        if ("Member".equalsIgnoreCase(currentMembership)) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "You are already a member!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Upgrade to Membership for $9.99/month?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Upgrade Membership");
        Optional<ButtonType> res = confirm.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.YES) {
            currentMembership = "Member";
            updateUserInfo(currentUser, currentEmail, currentPassword);
            membershipLabel.setText("Member");
            showAlert(Alert.AlertType.INFORMATION, "Success", "Membership upgraded successfully!");
        }
    }

    private void updateUserInfo(String newUsername, String newEmail, String newPassword) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] f = line.split(",");
                if (f.length >= 5 && f[0].equals(currentUser)) {
                    String updatedPwd = newPassword.isEmpty() ? currentPassword : newPassword;
                    line = newUsername + "," + updatedPwd + "," + newEmail + "," + f[3] + "," + currentMembership;
                    currentUser = newUsername;
                    currentEmail = newEmail;
                    currentPassword = updatedPwd;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        usernameLabel.setText(currentUser);
        emailLabel.setText(currentEmail);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
