package adt;

/**
 * Hash Table ADT Interface
 * Used for fast lookup and storage of key-value pairs
 * 
 * Time Complexity:
 * - put: O(1) average, O(n) worst
 * - get: O(1) average, O(n) worst
 * - remove: O(1) average, O(n) worst
 */
public interface HashTableADT<K, V> {
    /**
     * Insert key-value pair
     * @param key Key
     * @param value Value
     */
    void put(K key, V value);
    
    /**
     * Get value by key
     * @param key Key
     * @return Value, or null if not found
     */
    V get(K key);
    
    /**
     * Check if key exists
     * @param key Key
     * @return true if exists
     */
    boolean containsKey(K key);
    
    /**
     * Remove key-value pair
     * @param key Key
     */
    void remove(K key);
    
    /**
     * Get hash table size
     * @return Number of elements
     */
    int size();
    
    /**
     * Check if empty
     * @return true if empty
     */
    boolean isEmpty();
}

