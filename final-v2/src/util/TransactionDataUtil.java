package util;

import model.Transaction;
import adt.HashTableADT;
import datastructure.ChainingHashTable;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TransactionDataUtil {

    private static final String TRANSACTION_FILE = "data/transactions.csv";

    /** Memory Cache */
    private static final List<Transaction> transactions = new ArrayList<>();
    
    // ========== HashTable Index - Optimize Search Performance O(1) ==========
    /** HashTable indexed by userId - O(1) search */
    private static HashTableADT<String, List<Transaction>> userIdIndex = new ChainingHashTable<>();
    
    /** HashTable indexed by type - O(1) search */
    private static HashTableADT<Transaction.TransactionType, List<Transaction>> typeIndex = new ChainingHashTable<>();
    
    /** HashTable indexed by relatedId - O(1) search */
    private static HashTableADT<String, Transaction> relatedIdIndex = new ChainingHashTable<>();
    // ========================================================

    static {
        loadTransactions();
        buildIndexes(); // Build indexes
    }

    /** Load CSV */
    public static void loadTransactions() {
        transactions.clear();

        try {
            Path path = Paths.get(TRANSACTION_FILE);

            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);

                try (PrintWriter pw = new PrintWriter(new FileWriter(TRANSACTION_FILE))) {
                    pw.println("transactionId,userId,type,amount,date,description,relatedId");
                }
                return;
            }

            List<String> lines = Files.readAllLines(path);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                if (i == 0 && line.contains("transactionId")) continue;
                if (line.isBlank()) continue;

                try {
                    Transaction transaction = Transaction.fromCSV(line);
                    transactions.add(transaction);
                } catch (Exception e) {
                    System.err.println("Error parsing transaction line: " + line);
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Build indexes after loading
        buildIndexes();
    }
    
    /**
     * Build all indexes (called after loading data)
     * Time Complexity: O(n), but subsequent searches become O(1)
     * 
     * Demonstrate HashTable Application:
     * - Use HashTables to build multiple indexes
     * - Support fast lookup (O(1))
     * - Demonstrate practical application scenarios of HashTables
     */
    private static void buildIndexes() {
        // Re-create indexes
        userIdIndex = new ChainingHashTable<>();
        typeIndex = new ChainingHashTable<>();
        relatedIdIndex = new ChainingHashTable<>();
        
        // Build indexes: iterate through all transactions and build indexes
        for (Transaction t : transactions) {
            // Index by userId
            List<Transaction> userList = userIdIndex.get(t.getUserId());
            if (userList == null) {
                userList = new ArrayList<>();
                userIdIndex.put(t.getUserId(), userList);
            }
            userList.add(t);
            
            // Index by type
            List<Transaction> typeList = typeIndex.get(t.getType());
            if (typeList == null) {
                typeList = new ArrayList<>();
                typeIndex.put(t.getType(), typeList);
            }
            typeList.add(t);
            
            // Index by relatedId
            if (t.getRelatedId() != null && !t.getRelatedId().isEmpty()) {
                relatedIdIndex.put(t.getRelatedId(), t);
            }
        }
        
        System.out.println("HashTable indexes built: userIdIndex=" + userIdIndex.size() + 
                          ", typeIndex=" + typeIndex.size() + 
                          ", relatedIdIndex=" + relatedIdIndex.size());
    }

    /** Save CSV */
    private static void saveTransactions() {
        try {
            Path path = Paths.get(TRANSACTION_FILE);
            Files.createDirectories(path.getParent());
            
            try (PrintWriter pw = new PrintWriter(new FileWriter(TRANSACTION_FILE))) {
                pw.println("transactionId,userId,type,amount,date,description,relatedId");

                for (Transaction t : transactions) {
                    pw.println(t.toCSV());
                }
            }
            System.out.println("Transactions saved to: " + TRANSACTION_FILE + " (count: " + transactions.size() + ")");
        } catch (IOException e) {
            System.err.println("ERROR saving transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Add transaction */
    public static void addTransaction(Transaction t) {
        if (t == null) {
            System.err.println("ERROR: Cannot add null transaction!");
            return;
        }
        System.out.println("Adding transaction: " + t.getTransactionId() + ", userId: " + t.getUserId() + ", amount: " + t.getAmount());
        transactions.add(t);
        saveTransactions();
        
        // ========== Update HashTable Indexes ==========
        // Update userId index
        List<Transaction> userList = userIdIndex.get(t.getUserId());
        if (userList == null) {
            userList = new ArrayList<>();
            userIdIndex.put(t.getUserId(), userList);
        }
        userList.add(t);
        
        // Update type index
        List<Transaction> typeList = typeIndex.get(t.getType());
        if (typeList == null) {
            typeList = new ArrayList<>();
            typeIndex.put(t.getType(), typeList);
        }
        typeList.add(t);
        
        // Update relatedId index
        if (t.getRelatedId() != null && !t.getRelatedId().isEmpty()) {
            relatedIdIndex.put(t.getRelatedId(), t);
        }
        // ======================================
        
        System.out.println("Transaction saved. Total transactions: " + transactions.size());
    }

    /** Get all transactions */
    public static List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /** 
     * Get transactions for a specific user
     * Optimized using HashTable index: O(1) search
     * Before optimization: O(n) linear search
     * After optimization: O(1) HashTable search
     */
    public static List<Transaction> getTransactionsByUserId(String userId) {
        List<Transaction> result = userIdIndex.get(userId);
        return result != null ? new ArrayList<>(result) : new ArrayList<>();
    }

    /** 
     * Get transactions by type
     * Optimized using HashTable index: O(1) search
     * Before optimization: O(n) linear search
     * After optimization: O(1) HashTable search
     */
    public static List<Transaction> getTransactionsByType(Transaction.TransactionType type) {
        List<Transaction> result = typeIndex.get(type);
        return result != null ? new ArrayList<>(result) : new ArrayList<>();
    }

    /** 
     * Get transaction by relatedId
     * Optimized using HashTable index: O(1) search
     * Before optimization: O(n) linear search
     * After optimization: O(1) HashTable search
     */
    public static Transaction getTransactionByRelatedId(String relatedId) {
        if (relatedId == null || relatedId.isEmpty()) {
            return null;
        }
        return relatedIdIndex.get(relatedId);
    }

    /** Reload data (refresh after external CSV modification) */
    public static void reload() {
        loadTransactions();
        buildIndexes(); // Rebuild indexes
    }
}

