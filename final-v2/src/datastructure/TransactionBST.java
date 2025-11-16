package datastructure;

import model.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction专用二叉搜索树
 * 按日期（LocalDate）排序Transaction对象
 * 
 * 时间复杂度：
 * - insert: O(log n) 平均，O(n) 最坏（退化为链表）
 * - contains: O(log n) 平均，O(n) 最坏
 * - inOrderTraversal: O(n)
 * - rangeQuery: O(log n + k) 其中k是结果数量
 * 
 * 应用场景：
 * - Billing模块：维护按日期排序的交易记录
 * - 范围查询：查询特定日期范围内的交易
 * - 自动排序：插入时自动维护有序性，避免每次排序
 */
public class TransactionBST {
    
    /**
     * 树节点
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
     * 插入Transaction
     * 时间复杂度：O(log n) 平均，O(n) 最坏
     */
    public void insert(Transaction transaction) {
        root = insertRecursive(root, transaction);
    }
    
    /**
     * 递归插入
     * 比较规则：按日期（LocalDate）比较
     * 如果日期相同，按transactionId比较（确保唯一性）
     */
    private TreeNode insertRecursive(TreeNode node, Transaction transaction) {
        // 空节点，创建新节点
        if (node == null) {
            size++;
            return new TreeNode(transaction);
        }
        
        // 比较日期
        int dateCompare = transaction.getDate().compareTo(node.transaction.getDate());
        
        if (dateCompare < 0) {
            // 插入左子树（日期更早）
            node.left = insertRecursive(node.left, transaction);
        } else if (dateCompare > 0) {
            // 插入右子树（日期更晚）
            node.right = insertRecursive(node.right, transaction);
        } else {
            // 日期相同，按transactionId比较（确保唯一性）
            int idCompare = transaction.getTransactionId().compareTo(node.transaction.getTransactionId());
            if (idCompare < 0) {
                node.left = insertRecursive(node.left, transaction);
            } else if (idCompare > 0) {
                node.right = insertRecursive(node.right, transaction);
            }
            // 如果transactionId也相同，不插入（已存在）
        }
        
        return node;
    }
    
    /**
     * 检查是否包含Transaction
     * 时间复杂度：O(log n) 平均，O(n) 最坏
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
            // 日期相同，比较transactionId
            return transaction.getTransactionId().equals(node.transaction.getTransactionId());
        }
    }
    
    /**
     * 中序遍历（返回有序序列）
     * 时间复杂度：O(n)
     * 结果：按日期从早到晚排序
     */
    public List<Transaction> inOrderTraversal() {
        List<Transaction> result = new ArrayList<>();
        inOrderRecursive(root, result);
        return result;
    }
    
    /**
     * 中序遍历：左子树 -> 根节点 -> 右子树
     * 结果：按日期从早到晚排序
     */
    private void inOrderRecursive(TreeNode node, List<Transaction> result) {
        if (node != null) {
            inOrderRecursive(node.left, result);  // 左子树
            result.add(node.transaction);         // 根节点
            inOrderRecursive(node.right, result); // 右子树
        }
    }
    
    /**
     * 范围查询：查找[min, max]范围内的所有Transaction
     * 时间复杂度：O(log n + k)，其中k是结果数量
     */
    public List<Transaction> rangeQuery(Transaction min, Transaction max) {
        List<Transaction> result = new ArrayList<>();
        rangeQueryRecursive(root, min, max, result);
        return result;
    }
    
    /**
     * 范围查询递归实现
     */
    private void rangeQueryRecursive(TreeNode node, Transaction min, Transaction max, List<Transaction> result) {
        if (node == null) {
            return;
        }
        
        // 如果当前节点在范围内，添加到结果
        if (isInRange(node.transaction, min, max)) {
            result.add(node.transaction);
        }
        
        // 如果min小于当前节点日期，搜索左子树
        if (min.getDate().compareTo(node.transaction.getDate()) <= 0) {
            rangeQueryRecursive(node.left, min, max, result);
        }
        
        // 如果max大于当前节点日期，搜索右子树
        if (max.getDate().compareTo(node.transaction.getDate()) >= 0) {
            rangeQueryRecursive(node.right, min, max, result);
        }
    }
    
    /**
     * 检查transaction是否在[min, max]范围内
     */
    private boolean isInRange(Transaction transaction, Transaction min, Transaction max) {
        int compareMin = transaction.getDate().compareTo(min.getDate());
        int compareMax = transaction.getDate().compareTo(max.getDate());
        return compareMin >= 0 && compareMax <= 0;
    }
    
    /**
     * 查找最小日期Transaction
     * 时间复杂度：O(log n) 平均，O(n) 最坏
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
     * 查找最大日期Transaction
     * 时间复杂度：O(log n) 平均，O(n) 最坏
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
     * 删除Transaction
     * 时间复杂度：O(log n) 平均，O(n) 最坏
     */
    public void remove(Transaction transaction) {
        root = removeRecursive(root, transaction);
    }
    
    /**
     * 删除节点（三种情况）
     * 1. 叶子节点：直接删除
     * 2. 只有一个子节点：用子节点替换
     * 3. 有两个子节点：用右子树的最小值替换，然后删除右子树的最小值
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
            // 找到要删除的节点
            if (transaction.getTransactionId().equals(node.transaction.getTransactionId())) {
                size--;
                
                // 情况1：叶子节点
                if (node.left == null && node.right == null) {
                    return null;
                }
                
                // 情况2：只有一个子节点
                if (node.left == null) {
                    return node.right;
                }
                if (node.right == null) {
                    return node.left;
                }
                
                // 情况3：有两个子节点
                // 找到右子树的最小值
                TreeNode minNode = findMinRecursive(node.right);
                node.transaction = minNode.transaction;
                node.right = removeRecursive(node.right, minNode.transaction);
            } else {
                // 日期相同但transactionId不同，继续搜索
                node.left = removeRecursive(node.left, transaction);
            }
        }
        
        return node;
    }
    
    /**
     * 获取树的大小
     */
    public int size() {
        return size;
    }
    
    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return size == 0;
    }
}
