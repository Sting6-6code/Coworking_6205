package model.viewmodel;

import javafx.beans.property.SimpleStringProperty;

/**
 * 用户数据的TableView视图模型
 * 用于在JavaFX TableView中展示用户信息
 * 包含：用户名、密码、邮箱、类型、会员状态
 */
public class UserTableModel {
    private final SimpleStringProperty username;
    private final SimpleStringProperty password;
    private final SimpleStringProperty email;
    private final SimpleStringProperty type;
    private final SimpleStringProperty membership;

    public UserTableModel(String username, String password, String email, String type, String membership) {
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.email = new SimpleStringProperty(email);
        this.type = new SimpleStringProperty(type);
        this.membership = new SimpleStringProperty(membership);
    }

    // Username property
    public String getUsername() {
        return username.get();
    }

    public void setUsername(String value) {
        username.set(value);
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    // Password property
    public String getPassword() {
        return password.get();
    }

    public void setPassword(String value) {
        password.set(value);
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    // Email property
    public String getEmail() {
        return email.get();
    }

    public void setEmail(String value) {
        email.set(value);
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    // Type property
    public String getType() {
        return type.get();
    }

    public void setType(String value) {
        type.set(value);
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    // Membership property
    public String getMembership() {
        return membership.get();
    }

    public void setMembership(String value) {
        membership.set(value);
    }

    public SimpleStringProperty membershipProperty() {
        return membership;
    }
}

