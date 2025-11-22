package model.viewmodel;

import javafx.beans.property.SimpleStringProperty;

/**
 * TableView ViewModel for User Data
 * Used to display user information in JavaFX TableView
 * Contains: username, password, email, type, membership status
 */
public class UserTableModel {
	private final SimpleStringProperty userId;
	private final SimpleStringProperty username;
	private final SimpleStringProperty password;
	private final SimpleStringProperty email;
	private final SimpleStringProperty type;
	private final SimpleStringProperty membership;

	public UserTableModel(String userId, String username, String password, String email, String type,
			String membership) {
		this.userId = new SimpleStringProperty(userId);
		this.username = new SimpleStringProperty(username);
		this.password = new SimpleStringProperty(password);
		this.email = new SimpleStringProperty(email);
		this.type = new SimpleStringProperty(type);
		this.membership = new SimpleStringProperty(membership);
	}

	public String getUserId() {
		return userId.get();
	}

	public void setUserId(String value) {
		userId.set(value);
	}

	public SimpleStringProperty userIdProperty() {
		return userId;
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
