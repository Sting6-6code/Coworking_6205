package service;

import datastructure.QuickSort;
import java.util.*;

/**
 * SortingService
 * Uses custom QuickSort implementation for sorting operations
 * 
 * Demonstrates Sorting ADT application:
 * - Custom QuickSort algorithm (O(n log n) average)
 * - Supports various sorting criteria
 * - Replaces Java Collections.sort() with custom implementation
 */
public class SortingService {

    public enum SortMode {
        DEFAULT, BOOKINGS, REVENUE
    }

    // ========== Custom QuickSort instance ==========
    private static final QuickSort<String> quickSort = new QuickSort<>();
    // =================================================

    /**
     * Sort buildings using custom QuickSort algorithm
     * @param mode Sort mode (DEFAULT, BOOKINGS, REVENUE)
     * @param bookingCounts Map of building to booking count
     * @param revenues Map of building to revenue
     * @return Sorted list of buildings
     */
    public static List<String> sortBuildings(
            SortMode mode,
            Map<String, Long> bookingCounts,
            Map<String, Double> revenues
    ) {
        
        Set<String> allBuildings = new HashSet<>();
        allBuildings.addAll(bookingCounts.keySet());
        allBuildings.addAll(revenues.keySet());

        List<String> buildings = new ArrayList<>(allBuildings);

        // ========== Use custom QuickSort instead of Collections.sort() ==========
        switch (mode) {
            case BOOKINGS -> {
                // Sort by booking count (descending) using QuickSort
                Comparator<String> bookingComparator = 
                    Comparator.comparingLong((String b) -> bookingCounts.getOrDefault(b, 0L)).reversed();
                quickSort.sort(buildings, bookingComparator);
            }
            case REVENUE -> {
                // Sort by revenue (descending) using QuickSort
                Comparator<String> revenueComparator = 
                    Comparator.comparingDouble((String b) -> revenues.getOrDefault(b, 0.0)).reversed();
                quickSort.sort(buildings, revenueComparator);
            }
            default -> {
                // Default: alphabetical sort using QuickSort
                quickSort.sort(buildings);
            }
        }
        // =========================================================================

        return buildings;
    }
    
    /**
     * Sort spaces using custom QuickSort
     * @param spaces List of spaces to sort
     * @param comparator Comparator for sorting criteria
     */
    public static <T extends Comparable<T>> void sortSpaces(List<T> spaces, Comparator<T> comparator) {
        QuickSort<T> sorter = new QuickSort<>();
        if (comparator != null) {
            sorter.sort(spaces, comparator);
        } else {
            sorter.sort(spaces);
        }
    }
}
