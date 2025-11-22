package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Transaction;
import util.CurrentUser;
import util.TransactionDataUtil;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class UsersController {

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label membershipLabel;
    @FXML private Button editButton;
    @FXML private Button upgradeButton;

    private static final String DATA_FILE = "data/data.csv";
    
    // Static field for sharing username across controllers
    private static String currentUser;

    private String currentEmail;
    private String currentMembership;
    private String currentPassword;

    // Provide static setter method (called by LoginController)
    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    @FXML
    public void initialize() {
        // Get username from CurrentUser
        if (CurrentUser.get() != null) {
            currentUser = CurrentUser.get().getUsername();
        }
        
        if (currentUser == null || currentUser.isEmpty()) {
            System.err.println("⚠️ currentUser is null! Make sure user is logged in.");
            return;
        }
        loadUserData();
    }

    private void loadUserData() {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] f = line.split(",");
                // CSV format: userId,username,password,email,type,membership
                if (f.length >= 6) {
                    String userId = f[0];
                    String username = f[1];
                    // Match username (because currentUser is username)
                    if (username.equals(currentUser)) {
                        currentPassword = f[2];
                        currentEmail = f[3];
                        currentMembership = f[5] != null && !f[5].isEmpty() && f[5].equalsIgnoreCase("Member") ? "Member" : "Non-member";

                        usernameLabel.setText(currentUser);
                        emailLabel.setText(currentEmail);
                        membershipLabel.setText(currentMembership);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR loading user data: " + e.getMessage());
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
            
            if (CurrentUser.get() != null) {
                CurrentUser.get().setMembership("Member");
            }
            // Create MEMBERSHIP transaction record
            try {
                String userId = CurrentUser.get() != null ? CurrentUser.get().getUserId() : null;
                if (userId == null) {
                    // Try to find userId from username
                    userId = getUserIdFromUsername(currentUser);
                }
                
                if (userId != null) {
                    double membershipPrice = 9.99;
                    String description = "Membership Upgrade - Monthly Subscription";
                    String membershipId = UUID.randomUUID().toString();
                    
                    Transaction transaction = new Transaction(
                            userId,
                            Transaction.TransactionType.MEMBERSHIP,
                            membershipPrice,
                            LocalDate.now(),
                            description,
                            membershipId
                    );
                    
                    TransactionDataUtil.addTransaction(transaction);
                    System.out.println("Membership transaction created: " + transaction.getTransactionId());
                } else {
                    System.err.println("ERROR: Cannot get userId for membership transaction!");
                }
            } catch (Exception e) {
                System.err.println("ERROR creating membership transaction: " + e.getMessage());
                e.printStackTrace();
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Membership upgraded successfully!");
        }
    }

    private void updateUserInfo(String newUsername, String newEmail, String newPassword) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] f = line.split(",");
                // CSV format: userId,username,password,email,type,membership
                if (f.length >= 6) {
                    String username = f[1];
                    // Match username
                    if (username.equals(currentUser)) {
                        String updatedPwd = newPassword.isEmpty() ? currentPassword : newPassword;
                        // Rebuild row: userId,newUsername,updatedPwd,newEmail,type,currentMembership
                        line = f[0] + "," + newUsername + "," + updatedPwd + "," + newEmail + "," + f[4] + "," + currentMembership;
                        currentUser = newUsername;
                        currentEmail = newEmail;
                        currentPassword = updatedPwd;
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("ERROR updating user info: " + e.getMessage());
            e.printStackTrace();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("ERROR saving user info: " + e.getMessage());
            e.printStackTrace();
        }

        usernameLabel.setText(currentUser);
        emailLabel.setText(currentEmail);
    }

    /**
     * Get userId from username by looking up in data.csv
     */
    private String getUserIdFromUsername(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                // Format: userId,username,password,email,type,membership
                if (parts.length >= 2 && parts[1].equals(username)) {
                    return parts[0]; // Return userId (first field)
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading data.csv: " + e.getMessage());
        }
        return null;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
