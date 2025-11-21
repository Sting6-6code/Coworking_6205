package service;

import java.util.*;

public class SortingService {

    public enum SortMode {
        DEFAULT, BOOKINGS, REVENUE
    }

    public static List<String> sortBuildings(
            SortMode mode,
            Map<String, Long> bookingCounts,
            Map<String, Double> revenues
    ) {
        
        Set<String> allBuildings = new HashSet<>();
        allBuildings.addAll(bookingCounts.keySet());
        allBuildings.addAll(revenues.keySet());

        List<String> buildings = new ArrayList<>(allBuildings);

        switch (mode) {
            case BOOKINGS -> buildings.sort(
                    Comparator.comparingLong(b -> bookingCounts.getOrDefault(b, 0L)).reversed()
            );
            case REVENUE -> buildings.sort(
                    Comparator.comparingDouble(b -> revenues.getOrDefault(b, 0.0)).reversed()
            );
            default -> Collections.sort(buildings);
        }

        return buildings;
    }
}
