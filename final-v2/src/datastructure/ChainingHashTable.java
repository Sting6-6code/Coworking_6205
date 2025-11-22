package datastructure;

import adt.HashTableADT;
import java.util.LinkedList;

/**
 * Chaining Hash Table Implementation
 * Uses linked lists to handle collisions (Chaining)
 * 
 * Time Complexity:
 * - put: O(1) average, O(n) worst (all elements hashed to the same location)
 * - get: O(1) average, O(n) worst
 * - remove: O(1) average, O(n) worst
 * 
 * Space Complexity: O(n)
 * 
 * Hash Function: Uses division hashing h(k) = k mod m
 * Collision Resolution: Chaining - Uses LinkedList to store conflicting elements
 */
public class ChainingHashTable<K, V> implements HashTableADT<K, V> {
    
    // Hash table capacity (using prime numbers can reduce collisions)
    private static final int DEFAULT_CAPACITY = 17;
    private static final double LOAD_FACTOR = 0.75;
    
    // Use linked list array to store data (each position is a linked list storing conflicting elements)
    private LinkedList<Entry<K, V>>[] table;
    private int size;
    private int capacity;
    
    // Inner class: Stores key-value pairs
    private static class Entry<K, V> {
        K key;
        V value;
        
        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    /**
     * Default Constructor
     */
    @SuppressWarnings("unchecked")
    public ChainingHashTable() {
        this(DEFAULT_CAPACITY);
    }
    
    /**
     * Constructor with specified capacity
     * @param capacity Initial capacity
     */
    @SuppressWarnings("unchecked")
    public ChainingHashTable(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.table = new LinkedList[capacity];
        // Initialize linked list for each position
        for (int i = 0; i < capacity; i++) {
            table[i] = new LinkedList<>();
        }
    }
    
    /**
     * 
     * @param key Key
     * @return Hash value (array index)
     */
    private int hashFunction(K key) {
        int hashCode = key.hashCode();
        // Ensure positive number (use bitwise operation to remove sign bit)
        return (hashCode & 0x7FFFFFFF) % capacity;
    }
    
    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        // Check if resizing is needed (load factor exceeds threshold)
        if ((double) size / capacity >= LOAD_FACTOR) {
            resize();
        }
        
        int index = hashFunction(key);
        LinkedList<Entry<K, V>> bucket = table[index];
        
        // Check if key already exists (update value)
        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                entry.value = value; // Update value
                return;
            }
        }
        
        // Add new entry (handle collision: add to linked list)
        bucket.add(new Entry<>(key, value));
        size++;
    }
    
    @Override
    public V get(K key) {
        if (key == null) {
            return null;
        }
        
        int index = hashFunction(key);
        LinkedList<Entry<K, V>> bucket = table[index];
        
        // Search in linked list
        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        
        return null;
    }
    
    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }
    
    @Override
    public void remove(K key) {
        if (key == null) {
            return;
        }
        
        int index = hashFunction(key);
        LinkedList<Entry<K, V>> bucket = table[index];
        
        bucket.removeIf(entry -> entry.key.equals(key));
        if (bucket.size() < table[index].size()) {
            size--; // If element removed, decrease size
        }
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = capacity * 2;
        LinkedList<Entry<K, V>>[] newTable = new LinkedList[newCapacity];
        
        // Initialize new table
        for (int i = 0; i < newCapacity; i++) {
            newTable[i] = new LinkedList<>();
        }
        
        // Re-hash all elements
        for (LinkedList<Entry<K, V>> bucket : table) {
            for (Entry<K, V> entry : bucket) {
                int newIndex = (entry.key.hashCode() & 0x7FFFFFFF) % newCapacity;
                newTable[newIndex].add(entry);
            }
        }
        
        this.table = newTable;
        this.capacity = newCapacity;
    }
}

