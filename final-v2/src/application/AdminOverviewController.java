package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import model.viewmodel.UserTableModel;
import java.io.*;

public class AdminOverviewController {

    @FXML private TableView<UserTableModel> userTable;
    @FXML private TableColumn<UserTableModel, String> colUserId;
    @FXML private TableColumn<UserTableModel, String> colUsername;
    @FXML private TableColumn<UserTableModel, String> colPassword;
    @FXML private TableColumn<UserTableModel, String> colEmail;
    @FXML private TableColumn<UserTableModel, String> colType;
    @FXML private TableColumn<UserTableModel, String> colMembership;
    @FXML private TableColumn<UserTableModel, String> colEdit;

    private static final String DATA_FILE = "data/data.csv";
    private ObservableList<UserTableModel> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadDataFromCSV();
    }

    private void setupTable() {
    	colUserId.setCellValueFactory(data -> data.getValue().userIdProperty());
        colUsername.setCellValueFactory(data -> data.getValue().usernameProperty());
        colPassword.setCellValueFactory(data -> data.getValue().passwordProperty());
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colMembership.setCellValueFactory(data -> data.getValue().membershipProperty());

        // 编辑按钮列
        colEdit.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✎");

            {
                editBtn.setOnAction((ActionEvent event) -> {
                    UserTableModel user = getTableView().getItems().get(getIndex());
                    openEditDialog(user);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editBtn);
                }
            }
        });

        userTable.setItems(userList);
    }

    private void openEditDialog(UserTableModel user) {
        Dialog<UserTableModel> dialog = new Dialog<>();
        dialog.setTitle("Edit User");

        // 设置按钮
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 布局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField(user.getUsername());
        TextField passwordField = new TextField(user.getPassword());
        TextField emailField = new TextField(user.getEmail());
        TextField typeField = new TextField(user.getType());
        ChoiceBox<String> membershipChoice = new ChoiceBox<>();
        membershipChoice.getItems().addAll("Member", "Non-member");
        membershipChoice.setValue(user.getMembership());

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Type:"), 0, 3);
        grid.add(typeField, 1, 3);
        grid.add(new Label("Membership:"), 0, 4);
        grid.add(membershipChoice, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // 点击保存后更新
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setUsername(usernameField.getText());
                user.setPassword(passwordField.getText());
                user.setEmail(emailField.getText());
                user.setType(typeField.getText());
                user.setMembership(membershipChoice.getValue());
                return user;
            }
            return null;
        });

        dialog.showAndWait();
        userTable.refresh(); // 更新显示
    }

    private void loadDataFromCSV() {
        userList.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {

            br.readLine();  // ⭐⭐ 跳过标题行！！

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {

                    String[] data = line.split(",");

                    String userId = data.length > 0 ? data[0] : "";
                    String username = data.length > 1 ? data[1] : "";
                    String password = data.length > 2 ? data[2] : "";
                    String email = data.length > 3 ? data[3] : "";
                    String type = data.length > 4 ? data[4] : "User";
                    String membership = data.length > 5 ? data[5] : "Non-member";

                    userList.add(new UserTableModel(
                            userId, username, password, email, type, membership
                    ));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void addUser() {
        String newId = java.util.UUID.randomUUID().toString(); // 生成唯一 userId
        userList.add(new UserTableModel(
                newId,
                "new_user",
                "password",
                "email@example.com",
                "User",
                "Non-member"
        ));
    }


    @FXML
    private void saveChanges() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (UserTableModel u : userList) {
                bw.write( u.getUserId() + "," + u.getUsername() + "," + u.getPassword() + "," + u.getEmail() + "," + u.getType() + "," + u.getMembership());
                bw.newLine();
            }
            showAlert("Saved", "Changes saved successfully!");
        } catch (IOException e) {
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
