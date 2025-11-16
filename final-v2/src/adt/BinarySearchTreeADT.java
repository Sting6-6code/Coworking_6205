package adt;

import java.util.List;

/**
 * Binary Search Tree ADT接口
 * 用于维护有序数据，支持快速查找、插入和范围查询
 * 
 * 时间复杂度：
 * - insert: O(log n) 平均，O(n) 最坏（退化为链表）
 * - contains: O(log n) 平均，O(n) 最坏
 * - inOrderTraversal: O(n)
 * - rangeQuery: O(log n + k) 其中k是结果数量
 */
public interface BinarySearchTreeADT<T extends Comparable<T>> {
    /**
     * 插入元素
     * @param item 要插入的元素
     */
    void insert(T item);
    
    /**
     * 检查是否包含元素
     * @param item 要查找的元素
     * @return 如果包含返回true
     */
    boolean contains(T item);
    
    /**
     * 删除元素
     * @param item 要删除的元素
     */
    void remove(T item);
    
    /**
     * 中序遍历（返回有序序列）
     * @return 有序的元素列表
     */
    List<T> inOrderTraversal();
    
    /**
     * 范围查询（返回[min, max]范围内的所有元素）
     * @param min 最小值
     * @param max 最大值
     * @return 范围内的元素列表
     */
    List<T> rangeQuery(T min, T max);
    
    /**
     * 查找最小值
     * @return 最小元素
     */
    T findMin();
    
    /**
     * 查找最大值
     * @return 最大元素
     */
    T findMax();
    
    /**
     * 获取树的大小
     * @return 元素数量
     */
    int size();
    
    /**
     * 检查是否为空
     * @return 如果为空返回true
     */
    boolean isEmpty();
}

