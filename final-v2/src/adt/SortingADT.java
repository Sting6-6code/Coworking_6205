package adt;

import java.util.Comparator;
import java.util.List;

/**
 * Sorting ADT Interface
 * Defines the contract for sorting algorithms
 * 
 * Time Complexity:
 * - QuickSort: O(n log n) average, O(n^2) worst
 * - MergeSort: O(n log n) guaranteed
 * 
 * Application Scenarios:
 * - Space sorting by various criteria
 * - Transaction sorting
 * - Building sorting by revenue/bookings
 */
public interface SortingADT<T> {
    
    /**
     * Sort a list using natural ordering (requires T implements Comparable)
     * @param list List to sort (will be modified)
     */
    void sort(List<T> list);
    
    /**
     * Sort a list using a custom comparator
     * @param list List to sort (will be modified)
     * @param comparator Custom comparator for sorting
     */
    void sort(List<T> list, Comparator<T> comparator);
    
    /**
     * Sort an array using natural ordering
     * @param array Array to sort (will be modified)
     */
    void sort(T[] array);
    
    /**
     * Sort an array using a custom comparator
     * @param array Array to sort (will be modified)
     * @param comparator Custom comparator for sorting
     */
    void sort(T[] array, Comparator<T> comparator);
}

