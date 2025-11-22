package datastructure;

import adt.SortingADT;
import java.util.Comparator;
import java.util.List;

/**
 * QuickSort Implementation
 * Efficient in-place sorting algorithm using divide-and-conquer
 * 
 * Time Complexity:
 * - Average: O(n log n)
 * - Best: O(n log n)
 * - Worst: O(n^2) - when pivot is always smallest/largest
 * 
 * Space Complexity: O(log n) - recursion stack
 * 
 * Algorithm:
 * 1. Choose a pivot element
 * 2. Partition: rearrange elements so all < pivot are left, all > pivot are right
 * 3. Recursively sort left and right subarrays
 * 
 * Optimization: Uses median-of-three pivot selection to avoid worst case
 */
public class QuickSort<T> implements SortingADT<T> {
    
    /**
     * Sort a list using natural ordering
     * Requires T implements Comparable<T>
     * @param list List to sort (will be modified)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void sort(List<T> list) {
        if (list == null || list.size() <= 1) {
            return;
        }
        // Check if elements are Comparable
        if (!(list.get(0) instanceof Comparable)) {
            throw new IllegalArgumentException("Elements must implement Comparable<T> for natural ordering");
        }
        quickSort(list, 0, list.size() - 1, null);
    }
    
    /**
     * Sort a list using a custom comparator
     * @param list List to sort (will be modified)
     * @param comparator Custom comparator for sorting
     */
    @Override
    public void sort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() <= 1) {
            return;
        }
        quickSort(list, 0, list.size() - 1, comparator);
    }
    
    /**
     * Sort an array using natural ordering
     * Requires T implements Comparable<T>
     * @param array Array to sort (will be modified)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void sort(T[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        // Check if elements are Comparable
        if (array.length > 0 && !(array[0] instanceof Comparable)) {
            throw new IllegalArgumentException("Elements must implement Comparable<T> for natural ordering");
        }
        quickSort(array, 0, array.length - 1, null);
    }
    
    /**
     * Sort an array using a custom comparator
     * @param array Array to sort (will be modified)
     * @param comparator Custom comparator for sorting
     */
    @Override
    public void sort(T[] array, Comparator<T> comparator) {
        if (array == null || array.length <= 1) {
            return;
        }
        quickSort(array, 0, array.length - 1, comparator);
    }
    
    /**
     * Recursive QuickSort for List
     * @param list List to sort
     * @param low Starting index
     * @param high Ending index
     * @param comparator Optional comparator (null for natural ordering)
     */
    private void quickSort(List<T> list, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            // Partition and get pivot index
            int pivotIndex = partition(list, low, high, comparator);
            
            // Recursively sort elements before and after partition
            quickSort(list, low, pivotIndex - 1, comparator);
            quickSort(list, pivotIndex + 1, high, comparator);
        }
    }
    
    /**
     * Recursive QuickSort for Array
     * @param array Array to sort
     * @param low Starting index
     * @param high Ending index
     * @param comparator Optional comparator (null for natural ordering)
     */
    private void quickSort(T[] array, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            // Partition and get pivot index
            int pivotIndex = partition(array, low, high, comparator);
            
            // Recursively sort elements before and after partition
            quickSort(array, low, pivotIndex - 1, comparator);
            quickSort(array, pivotIndex + 1, high, comparator);
        }
    }
    
    /**
     * Partition function for List
     * Rearranges elements so all < pivot are left, all > pivot are right
     * @param list List to partition
     * @param low Starting index
     * @param high Ending index
     * @param comparator Optional comparator
     * @return Final position of pivot
     */
    private int partition(List<T> list, int low, int high, Comparator<T> comparator) {
        // Use median-of-three for better pivot selection
        T pivot = medianOfThree(list, low, high, comparator);
        
        int i = low - 1; // Index of smaller element
        
        for (int j = low; j < high; j++) {
            // If current element is smaller than or equal to pivot
            if (compare(list.get(j), pivot, comparator) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        
        // Place pivot in correct position
        swap(list, i + 1, high);
        return i + 1;
    }
    
    /**
     * Partition function for Array
     * @param array Array to partition
     * @param low Starting index
     * @param high Ending index
     * @param comparator Optional comparator
     * @return Final position of pivot
     */
    private int partition(T[] array, int low, int high, Comparator<T> comparator) {
        // Use median-of-three for better pivot selection
        T pivot = medianOfThree(array, low, high, comparator);
        
        int i = low - 1; // Index of smaller element
        
        for (int j = low; j < high; j++) {
            // If current element is smaller than or equal to pivot
            if (compare(array[j], pivot, comparator) <= 0) {
                i++;
                swap(array, i, j);
            }
        }
        
        // Place pivot in correct position
        swap(array, i + 1, high);
        return i + 1;
    }
    
    /**
     * Median-of-three pivot selection (optimization to avoid worst case)
     * @param list List
     * @param low Starting index
     * @param high Ending index
     * @param comparator Optional comparator
     * @return Median value as pivot
     */
    private T medianOfThree(List<T> list, int low, int high, Comparator<T> comparator) {
        int mid = low + (high - low) / 2;
        
        // Sort low, mid, high and use mid as pivot
        if (compare(list.get(low), list.get(mid), comparator) > 0) {
            swap(list, low, mid);
        }
        if (compare(list.get(low), list.get(high), comparator) > 0) {
            swap(list, low, high);
        }
        if (compare(list.get(mid), list.get(high), comparator) > 0) {
            swap(list, mid, high);
        }
        
        // Move pivot to end
        swap(list, mid, high);
        return list.get(high);
    }
    
    /**
     * Median-of-three for Array
     */
    private T medianOfThree(T[] array, int low, int high, Comparator<T> comparator) {
        int mid = low + (high - low) / 2;
        
        // Sort low, mid, high and use mid as pivot
        if (compare(array[low], array[mid], comparator) > 0) {
            swap(array, low, mid);
        }
        if (compare(array[low], array[high], comparator) > 0) {
            swap(array, low, high);
        }
        if (compare(array[mid], array[high], comparator) > 0) {
            swap(array, mid, high);
        }
        
        // Move pivot to end
        swap(array, mid, high);
        return array[high];
    }
    
    /**
     * Compare two elements using comparator or natural ordering
     */
    @SuppressWarnings("unchecked")
    private int compare(T a, T b, Comparator<T> comparator) {
        if (comparator != null) {
            return comparator.compare(a, b);
        } else {
            // Use Comparable if available
            if (a instanceof Comparable) {
                return ((Comparable<T>) a).compareTo(b);
            } else {
                throw new IllegalArgumentException("Cannot compare: elements must implement Comparable<T> or provide Comparator");
            }
        }
    }
    
    /**
     * Swap two elements in List
     */
    private void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
    
    /**
     * Swap two elements in Array
     */
    private void swap(T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}

