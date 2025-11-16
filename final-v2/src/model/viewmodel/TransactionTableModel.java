package model.viewmodel;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Transaction table view model for JavaFX TableView
 * Used to display transaction records in billing interfaces
 */
public class TransactionTableModel {
    private final SimpleStringProperty date;
    private final SimpleStringProperty type;
    private final SimpleStringProperty description;
    private final SimpleDoubleProperty amount;
    private final SimpleStringProperty userId;      // For admin view
    private final SimpleStringProperty username;    // For admin view
    private final SimpleStringProperty transactionId; // For admin view (optional)
    
    // Constructor for user view (without userId/username)
    public TransactionTableModel(String date, String type, String description, double amount) {
        this.date = new SimpleStringProperty(date);
        this.type = new SimpleStringProperty(type);
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
        this.userId = new SimpleStringProperty("");
        this.username = new SimpleStringProperty("");
        this.transactionId = new SimpleStringProperty("");
    }
    
    // Constructor for admin view (with userId/username)
    public TransactionTableModel(String transactionId, String userId, String username, 
                                String date, String type, String description, double amount) {
        this.transactionId = new SimpleStringProperty(transactionId);
        this.userId = new SimpleStringProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.date = new SimpleStringProperty(date);
        this.type = new SimpleStringProperty(type);
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
    }
    
    // Date property
    public String getDate() {
        return date.get();
    }
    
    public void setDate(String value) {
        date.set(value);
    }
    
    public SimpleStringProperty dateProperty() {
        return date;
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
    
    // Description property
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String value) {
        description.set(value);
    }
    
    public SimpleStringProperty descriptionProperty() {
        return description;
    }
    
    // Amount property
    public double getAmount() {
        return amount.get();
    }
    
    public void setAmount(double value) {
        amount.set(value);
    }
    
    public SimpleDoubleProperty amountProperty() {
        return amount;
    }
    
    // UserId property (for admin view)
    public String getUserId() {
        return userId.get();
    }
    
    public void setUserId(String value) {
        userId.set(value);
    }
    
    public SimpleStringProperty userIdProperty() {
        return userId;
    }
    
    // Username property (for admin view)
    public String getUsername() {
        return username.get();
    }
    
    public void setUsername(String value) {
        username.set(value);
    }
    
    public SimpleStringProperty usernameProperty() {
        return username;
    }
    
    // TransactionId property (for admin view)
    public String getTransactionId() {
        return transactionId.get();
    }
    
    public void setTransactionId(String value) {
        transactionId.set(value);
    }
    
    public SimpleStringProperty transactionIdProperty() {
        return transactionId;
    }
}

