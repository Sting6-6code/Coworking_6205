package datastructure;

import adt.HashTableADT;
import java.util.LinkedList;

/**
 * 链式哈希表实现
 * 使用链表处理冲突（Chaining）
 * 
 * 时间复杂度：
 * - put: O(1) 平均，O(n) 最坏（所有元素哈希到同一位置）
 * - get: O(1) 平均，O(n) 最坏
 * - remove: O(1) 平均，O(n) 最坏
 * 
 * 空间复杂度：O(n)
 * 
 * 哈希函数：使用除法哈希法 h(k) = k mod m
 * 冲突处理：链式法（Chaining）- 使用LinkedList存储冲突的元素
 */
public class ChainingHashTable<K, V> implements HashTableADT<K, V> {
    
    // 哈希表容量（使用质数可以减少冲突）
    private static final int DEFAULT_CAPACITY = 17;
    private static final double LOAD_FACTOR = 0.75;
    
    // 使用链表数组存储数据（每个位置是一个链表，存储冲突的元素）
    private LinkedList<Entry<K, V>>[] table;
    private int size;
    private int capacity;
    
    // 内部类：存储键值对
    private static class Entry<K, V> {
        K key;
        V value;
        
        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    /**
     * 默认构造函数
     */
    @SuppressWarnings("unchecked")
    public ChainingHashTable() {
        this(DEFAULT_CAPACITY);
    }
    
    /**
     * 指定容量的构造函数
     * @param capacity 初始容量
     */
    @SuppressWarnings("unchecked")
    public ChainingHashTable(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.table = new LinkedList[capacity];
        // 初始化每个位置的链表
        for (int i = 0; i < capacity; i++) {
            table[i] = new LinkedList<>();
        }
    }
    
    /**
    
     * 
     * @param key 键
     * @return 哈希值（数组索引）
     */
    private int hashFunction(K key) {
        int hashCode = key.hashCode();
        // 确保返回正数（使用位运算去除符号位）
        return (hashCode & 0x7FFFFFFF) % capacity;
    }
    
    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        // 检查是否需要扩容（负载因子超过阈值）
        if ((double) size / capacity >= LOAD_FACTOR) {
            resize();
        }
        
        int index = hashFunction(key);
        LinkedList<Entry<K, V>> bucket = table[index];
        
        // 检查是否已存在该键（更新值）
        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                entry.value = value; // 更新值
                return;
            }
        }
        
        // 添加新条目（处理冲突：添加到链表中）
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
        
        // 在链表中查找
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
            size--; // 如果删除了元素，减少size
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
    
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = capacity * 2;
        LinkedList<Entry<K, V>>[] newTable = new LinkedList[newCapacity];
        
        // 初始化新表
        for (int i = 0; i < newCapacity; i++) {
            newTable[i] = new LinkedList<>();
        }
        
        // 重新哈希所有元素
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

