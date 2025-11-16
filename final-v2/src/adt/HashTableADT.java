package adt;

/**
 * Hash Table ADT接口
 * 用于快速查找和存储键值对
 * 
 * 时间复杂度：
 * - put: O(1) 平均，O(n) 最坏
 * - get: O(1) 平均，O(n) 最坏
 * - remove: O(1) 平均，O(n) 最坏
 */
public interface HashTableADT<K, V> {
    /**
     * 插入键值对
     * @param key 键
     * @param value 值
     */
    void put(K key, V value);
    
    /**
     * 根据键获取值
     * @param key 键
     * @return 值，如果不存在返回null
     */
    V get(K key);
    
    /**
     * 检查是否包含键
     * @param key 键
     * @return 如果包含返回true
     */
    boolean containsKey(K key);
    
    /**
     * 删除键值对
     * @param key 键
     */
    void remove(K key);
    
    /**
     * 获取哈希表大小
     * @return 元素数量
     */
    int size();
    
    /**
     * 检查是否为空
     * @return 如果为空返回true
     */
    boolean isEmpty();
}

