package util;

import model.Transaction;
import adt.HashTableADT;
import datastructure.ChainingHashTable;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TransactionDataUtil {

    private static final String TRANSACTION_FILE = "data/transactions.csv";

    /** 内存缓存 */
    private static final List<Transaction> transactions = new ArrayList<>();
    
    // ========== HashTable索引 - 优化查找性能 O(1) ==========
    /** 按userId索引的哈希表 - O(1)查找 */
    private static HashTableADT<String, List<Transaction>> userIdIndex = new ChainingHashTable<>();
    
    /** 按type索引的哈希表 - O(1)查找 */
    private static HashTableADT<Transaction.TransactionType, List<Transaction>> typeIndex = new ChainingHashTable<>();
    
    /** 按relatedId索引的哈希表 - O(1)查找 */
    private static HashTableADT<String, Transaction> relatedIdIndex = new ChainingHashTable<>();
    // ========================================================

    static {
        loadTransactions();
        buildIndexes(); // 构建索引
    }

    /** 加载 CSV */
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
        
        // 加载完成后构建索引
        buildIndexes();
    }
    
    /**
     * 构建所有索引（在加载数据后调用）
     * 时间复杂度：O(n)，但后续查找变为O(1)
     * 
     * 展示HashTable的应用：
     * - 使用哈希表建立多个索引
     * - 支持快速查找（O(1)）
     * - 展示哈希表的实际应用场景
     */
    private static void buildIndexes() {
        // 重新创建索引
        userIdIndex = new ChainingHashTable<>();
        typeIndex = new ChainingHashTable<>();
        relatedIdIndex = new ChainingHashTable<>();
        
        // 构建索引：遍历所有transactions并建立索引
        for (Transaction t : transactions) {
            // 按userId索引
            List<Transaction> userList = userIdIndex.get(t.getUserId());
            if (userList == null) {
                userList = new ArrayList<>();
                userIdIndex.put(t.getUserId(), userList);
            }
            userList.add(t);
            
            // 按type索引
            List<Transaction> typeList = typeIndex.get(t.getType());
            if (typeList == null) {
                typeList = new ArrayList<>();
                typeIndex.put(t.getType(), typeList);
            }
            typeList.add(t);
            
            // 按relatedId索引
            if (t.getRelatedId() != null && !t.getRelatedId().isEmpty()) {
                relatedIdIndex.put(t.getRelatedId(), t);
            }
        }
        
        System.out.println("HashTable indexes built: userIdIndex=" + userIdIndex.size() + 
                          ", typeIndex=" + typeIndex.size() + 
                          ", relatedIdIndex=" + relatedIdIndex.size());
    }

    /** 保存 CSV */
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

    /** 添加 transaction */
    public static void addTransaction(Transaction t) {
        if (t == null) {
            System.err.println("ERROR: Cannot add null transaction!");
            return;
        }
        System.out.println("Adding transaction: " + t.getTransactionId() + ", userId: " + t.getUserId() + ", amount: " + t.getAmount());
        transactions.add(t);
        saveTransactions();
        
        // ========== 更新HashTable索引 ==========
        // 更新userId索引
        List<Transaction> userList = userIdIndex.get(t.getUserId());
        if (userList == null) {
            userList = new ArrayList<>();
            userIdIndex.put(t.getUserId(), userList);
        }
        userList.add(t);
        
        // 更新type索引
        List<Transaction> typeList = typeIndex.get(t.getType());
        if (typeList == null) {
            typeList = new ArrayList<>();
            typeIndex.put(t.getType(), typeList);
        }
        typeList.add(t);
        
        // 更新relatedId索引
        if (t.getRelatedId() != null && !t.getRelatedId().isEmpty()) {
            relatedIdIndex.put(t.getRelatedId(), t);
        }
        // ======================================
        
        System.out.println("Transaction saved. Total transactions: " + transactions.size());
    }

    /** 获取全部 transaction */
    public static List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /** 
     * 获取某用户的 transactions
     * 使用HashTable索引优化：O(1)查找
     * 优化前：O(n) 线性搜索
     * 优化后：O(1) 哈希表查找
     */
    public static List<Transaction> getTransactionsByUserId(String userId) {
        List<Transaction> result = userIdIndex.get(userId);
        return result != null ? new ArrayList<>(result) : new ArrayList<>();
    }

    /** 
     * 根据类型获取 transactions
     * 使用HashTable索引优化：O(1)查找
     * 优化前：O(n) 线性搜索
     * 优化后：O(1) 哈希表查找
     */
    public static List<Transaction> getTransactionsByType(Transaction.TransactionType type) {
        List<Transaction> result = typeIndex.get(type);
        return result != null ? new ArrayList<>(result) : new ArrayList<>();
    }

    /** 
     * 根据 relatedId 获取 transaction
     * 使用HashTable索引优化：O(1)查找
     * 优化前：O(n) 线性搜索
     * 优化后：O(1) 哈希表查找
     */
    public static Transaction getTransactionByRelatedId(String relatedId) {
        if (relatedId == null || relatedId.isEmpty()) {
            return null;
        }
        return relatedIdIndex.get(relatedId);
    }

    /** 重新加载数据（用于外部修改CSV后刷新） */
    public static void reload() {
        loadTransactions();
        buildIndexes(); // 重新构建索引
    }
}

