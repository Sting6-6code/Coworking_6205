package adt;

import java.util.List;

/**
 * Binary Search Tree ADT Interface
 * Used for maintaining sorted data, supports fast lookup, insertion, and range queries
 * 
 * Time Complexity:
 * - insert: O(log n) average, O(n) worst (degenerate to linked list)
 * - contains: O(log n) average, O(n) worst
 * - inOrderTraversal: O(n)
 * - rangeQuery: O(log n + k) where k is the number of results
 */
public interface BinarySearchTreeADT<T extends Comparable<T>> {
    /**
     * Insert item
     * @param item Item to insert
     */
    void insert(T item);
    
    /**
     * Check if item exists
     * @param item Item to search
     * @return true if exists
     */
    boolean contains(T item);
    
    /**
     * Remove item
     * @param item Item to remove
     */
    void remove(T item);
    
    /**
     * In-order traversal (returns sorted sequence)
     * @return Sorted list of items
     */
    List<T> inOrderTraversal();
    
    /**
     * Range query (returns all items within [min, max])
     * @param min Minimum value
     * @param max Maximum value
     * @return List of items in range
     */
    List<T> rangeQuery(T min, T max);
    
    /**
     * Find minimum value
     * @return Minimum item
     */
    T findMin();
    
    /**
     * Find maximum value
     * @return Maximum item
     */
    T findMax();
    
    /**
     * Get tree size
     * @return Number of elements
     */
    int size();
    
    /**
     * Check if empty
     * @return true if empty
     */
    boolean isEmpty();
}

