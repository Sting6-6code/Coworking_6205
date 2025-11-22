package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Transaction model for billing records
 * Represents a financial transaction (booking payment or membership purchase)
 */
public class Transaction {
    
    public enum TransactionType {
        BOOKING,    // Space booking payment
        MEMBERSHIP  // Membership purchase
    }
    
    private String transactionId;
    private String userId;
    private TransactionType type;
    private double amount;
    private LocalDate date;
    private String description;
    private String relatedId;  // bookingId for BOOKING, or membershipId for MEMBERSHIP
    
    // Default constructor
    public Transaction() {}
    
    // Full constructor
    public Transaction(String transactionId, String userId, TransactionType type, 
                      double amount, LocalDate date, String description, String relatedId) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.relatedId = relatedId;
    }
    
    // Constructor without transactionId (auto-generates UUID)
    public Transaction(String userId, TransactionType type, double amount, 
                      LocalDate date, String description, String relatedId) {
        this.transactionId = UUID.randomUUID().toString();
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.relatedId = relatedId;
    }
    
    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getRelatedId() {
        return relatedId;
    }
    
    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }
    
    /**
     * Convert Transaction to CSV line
     * Format: transactionId,userId,type,amount,date,description,relatedId
     */
    public String toCSV() {
        return String.join(",",
            transactionId,
            userId,
            type.name(),
            String.valueOf(amount),
            date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            description != null ? description.replace(",", ";") : "",  // Replace commas in description
            relatedId != null ? relatedId : ""
        );
    }
    
    /**
     * Parse Transaction from CSV line
     * Format: transactionId,userId,type,amount,date,description,relatedId
     */
    public static Transaction fromCSV(String line) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid transaction CSV line: " + line);
        }
        
        try {
            String transactionId = parts[0];
            String userId = parts[1];
            TransactionType type = TransactionType.valueOf(parts[2]);
            double amount = Double.parseDouble(parts[3]);
            LocalDate date = LocalDate.parse(parts[4], DateTimeFormatter.ISO_LOCAL_DATE);
            String description = parts.length > 5 ? parts[5].replace(";", ",") : "";
            String relatedId = parts.length > 6 ? parts[6] : "";
            
            return new Transaction(transactionId, userId, type, amount, date, description, relatedId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing transaction CSV line: " + line, e);
        }
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", userId='" + userId + '\'' +
                ", type=" + type +
                ", amount=" + amount +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", relatedId='" + relatedId + '\'' +
                '}';
    }
}

