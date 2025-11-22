package datastructure;

import model.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Binary Search Tree dedicated to Transaction
 * Sorts Transaction objects by date (LocalDate)
 * 
 * Time Complexity:
 * - insert: O(log n) average, O(n) worst (degenerate to linked list)
 * - contains: O(log n) average, O(n) worst
 * - inOrderTraversal: O(n)
 * - rangeQuery: O(log n + k) where k is the number of results
 * 
 * Application Scenarios:
 * - Billing Module: Maintains transaction records sorted by date
 * - Range Query: Query transactions within a specific date range
 * - Automatic Sorting: Automatically maintains order upon insertion, avoiding sorting each time
 */
public class TransactionBST {
    
    /**
     * Tree Node
     */
    private static class TreeNode {
        Transaction transaction;
        TreeNode left;
        TreeNode right;
        
        TreeNode(Transaction transaction) {
            this.transaction = transaction;
            this.left = null;
            this.right = null;
        }
    }
    
    private TreeNode root;
    private int size;
    
    public TransactionBST() {
        this.root = null;
        this.size = 0;
    }
    
    /**
     * Insert Transaction
     * Time Complexity: O(log n) average, O(n) worst
     */
    public void insert(Transaction transaction) {
        root = insertRecursive(root, transaction);
    }
    
    /**
     * Recursive Insertion
     * Comparison Rule: Compare by date (LocalDate)
     * If dates are equal, compare by transactionId (ensure uniqueness)
     */
    private TreeNode insertRecursive(TreeNode node, Transaction transaction) {
        // Empty node, create new node
        if (node == null) {
            size++;
            return new TreeNode(transaction);
        }
        
        // Compare dates
        int dateCompare = transaction.getDate().compareTo(node.transaction.getDate());
        
        if (dateCompare < 0) {
            // Insert into left subtree (earlier date)
            node.left = insertRecursive(node.left, transaction);
        } else if (dateCompare > 0) {
            // Insert into right subtree (later date)
            node.right = insertRecursive(node.right, transaction);
        } else {
            // Dates are equal, compare by transactionId (ensure uniqueness)
            int idCompare = transaction.getTransactionId().compareTo(node.transaction.getTransactionId());
            if (idCompare < 0) {
                node.left = insertRecursive(node.left, transaction);
            } else if (idCompare > 0) {
                node.right = insertRecursive(node.right, transaction);
            }
            // If transactionId is also equal, do not insert (already exists)
        }
        
        return node;
    }
    
    /**
     * Check if contains Transaction
     * Time Complexity: O(log n) average, O(n) worst
     */
    public boolean contains(Transaction transaction) {
        return containsRecursive(root, transaction);
    }
    
    private boolean containsRecursive(TreeNode node, Transaction transaction) {
        if (node == null) {
            return false;
        }
        
        int dateCompare = transaction.getDate().compareTo(node.transaction.getDate());
        if (dateCompare < 0) {
            return containsRecursive(node.left, transaction);
        } else if (dateCompare > 0) {
            return containsRecursive(node.right, transaction);
        } else {
            // Dates are equal, compare transactionId
            return transaction.getTransactionId().equals(node.transaction.getTransactionId());
        }
    }
    
    /**
     * In-order Traversal (return sorted sequence)
     * Time Complexity: O(n)
     * Result: Sorted by date from earliest to latest
     */
    public List<Transaction> inOrderTraversal() {
        List<Transaction> result = new ArrayList<>();
        inOrderRecursive(root, result);
        return result;
    }
    
    /**
     * In-order Traversal: Left Subtree -> Root Node -> Right Subtree
     * Result: Sorted by date from earliest to latest
     */
    private void inOrderRecursive(TreeNode node, List<Transaction> result) {
        if (node != null) {
            inOrderRecursive(node.left, result);  // Left Subtree
            result.add(node.transaction);         // Root Node
            inOrderRecursive(node.right, result); // Right Subtree
        }
    }
    
    /**
     * Range Query: Find all Transactions within [min, max] range
     * Time Complexity: O(log n + k), where k is the number of results
     */
    public List<Transaction> rangeQuery(Transaction min, Transaction max) {
        List<Transaction> result = new ArrayList<>();
        rangeQueryRecursive(root, min, max, result);
        return result;
    }
    
    /**
     * Range Query Recursive Implementation
     */
    private void rangeQueryRecursive(TreeNode node, Transaction min, Transaction max, List<Transaction> result) {
        if (node == null) {
            return;
        }
        
        // If current node is in range, add to result
        if (isInRange(node.transaction, min, max)) {
            result.add(node.transaction);
        }
        
        // If min is less than current node date, search left subtree
        if (min.getDate().compareTo(node.transaction.getDate()) <= 0) {
            rangeQueryRecursive(node.left, min, max, result);
        }
        
        // If max is greater than current node date, search right subtree
        if (max.getDate().compareTo(node.transaction.getDate()) >= 0) {
            rangeQueryRecursive(node.right, min, max, result);
        }
    }
    
    /**
     * Check if transaction is in [min, max] range
     */
    private boolean isInRange(Transaction transaction, Transaction min, Transaction max) {
        int compareMin = transaction.getDate().compareTo(min.getDate());
        int compareMax = transaction.getDate().compareTo(max.getDate());
        return compareMin >= 0 && compareMax <= 0;
    }
    
    /**
     * Find Minimum Date Transaction
     * Time Complexity: O(log n) average, O(n) worst
     */
    public Transaction findMin() {
        if (root == null) {
            return null;
        }
        TreeNode minNode = findMinRecursive(root);
        return minNode.transaction;
    }
    
    private TreeNode findMinRecursive(TreeNode node) {
        if (node.left == null) {
            return node;
        }
        return findMinRecursive(node.left);
    }
    
    /**
     * Find Maximum Date Transaction
     * Time Complexity: O(log n) average, O(n) worst
     */
    public Transaction findMax() {
        if (root == null) {
            return null;
        }
        TreeNode maxNode = findMaxRecursive(root);
        return maxNode.transaction;
    }
    
    private TreeNode findMaxRecursive(TreeNode node) {
        if (node.right == null) {
            return node;
        }
        return findMaxRecursive(node.right);
    }
    
    /**
     * Remove Transaction
     * Time Complexity: O(log n) average, O(n) worst
     */
    public void remove(Transaction transaction) {
        root = removeRecursive(root, transaction);
    }
    
    /**
     * Remove Node (Three Cases)
     * 1. Leaf Node: Delete directly
     * 2. One Child Node: Replace with child node
     * 3. Two Child Nodes: Replace with minimum of right subtree, then delete minimum of right subtree
     */
    private TreeNode removeRecursive(TreeNode node, Transaction transaction) {
        if (node == null) {
            return null;
        }
        
        int dateCompare = transaction.getDate().compareTo(node.transaction.getDate());
        if (dateCompare < 0) {
            node.left = removeRecursive(node.left, transaction);
        } else if (dateCompare > 0) {
            node.right = removeRecursive(node.right, transaction);
        } else {
            // Found node to delete
            if (transaction.getTransactionId().equals(node.transaction.getTransactionId())) {
                size--;
                
                // Case 1: Leaf Node
                if (node.left == null && node.right == null) {
                    return null;
                }
                
                // Case 2: One Child Node
                if (node.left == null) {
                    return node.right;
                }
                if (node.right == null) {
                    return node.left;
                }
                
                // Case 3: Two Child Nodes
                // Find minimum of right subtree
                TreeNode minNode = findMinRecursive(node.right);
                node.transaction = minNode.transaction;
                node.right = removeRecursive(node.right, minNode.transaction);
            } else {
                // Dates match but transactionId differs, continue searching
                node.left = removeRecursive(node.left, transaction);
            }
        }
        
        return node;
    }
    
    /**
     * Get tree size
     */
    public int size() {
        return size;
    }
    
    /**
     * Check if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }
}
