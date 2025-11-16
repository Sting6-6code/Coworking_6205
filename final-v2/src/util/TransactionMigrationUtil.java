package util;

import model.Booking;
import model.Space;
import model.Transaction;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;

/**
 * Utility class to migrate historical booking data to transactions
 */
public class TransactionMigrationUtil {

    private static final String BOOKINGS_FILE = "data/bookings.csv";
    private static final String SPACES_FILE = "data/spaces.csv";

    /**
     * Migrate all bookings from bookings.csv to transactions.csv
     * Only migrates bookings with status "booked"
     */
    public static void migrateBookingsToTransactions() {
        Map<String, Space> spaces = loadSpaces();
        List<Booking> bookings = loadBookings();
        
        int migratedCount = 0;
        int skippedCount = 0;

        for (Booking booking : bookings) {
            // Only migrate booked (not released) bookings
            if (!"booked".equals(booking.getStatus())) {
                skippedCount++;
                continue;
            }

            // Check if transaction already exists for this booking
            Transaction existing = TransactionDataUtil.getTransactionByRelatedId(booking.getBookingId());
            if (existing != null) {
                skippedCount++;
                continue; // Already migrated
            }

            // Find the space to get creditsPerHour
            Space space = spaces.get(booking.getSpaceId());
            if (space == null) {
                System.err.println("Warning: Space not found for booking " + booking.getBookingId() + 
                                 ", spaceId: " + booking.getSpaceId());
                skippedCount++;
                continue;
            }

            // Calculate amount based on duration and creditsPerHour
            long minutes = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
            double amount = (minutes / 60.0) * space.getCreditsPerHour();

            // Create description
            String description = String.format("Booking: %s (%s) - %s %s-%s",
                    space.getName(), space.getType(), booking.getDate(), 
                    booking.getStartTime(), booking.getEndTime());

            // Create and save transaction
            Transaction transaction = new Transaction(
                    booking.getUserId(),
                    Transaction.TransactionType.BOOKING,
                    amount,
                    booking.getDate(),
                    description,
                    booking.getBookingId()
            );

            TransactionDataUtil.addTransaction(transaction);
            migratedCount++;
        }

        System.out.println("Migration completed:");
        System.out.println("  Migrated: " + migratedCount + " transactions");
        System.out.println("  Skipped: " + skippedCount + " bookings (already migrated or released)");
    }

    /**
     * Load all spaces from spaces.csv
     */
    private static Map<String, Space> loadSpaces() {
        Map<String, Space> spaces = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(SPACES_FILE))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 9) {
                        Space space = new Space(
                                parts[0], parts[1], parts[2], parts[3], parts[4], parts[5],
                                Integer.parseInt(parts[6]), parts[7], Double.parseDouble(parts[8])
                        );
                        spaces.put(space.getSpaceId(), space);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing space: " + line);
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Spaces file not found: " + SPACES_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return spaces;
    }

    /**
     * Load all bookings from bookings.csv
     */
    private static List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 7) {
                        Booking booking = new Booking(
                                parts[0], parts[1], parts[2],
                                LocalDate.parse(parts[3]),
                                LocalTime.parse(parts[4]),
                                LocalTime.parse(parts[5]),
                                parts[6]
                        );
                        bookings.add(booking);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing booking: " + line);
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Bookings file not found: " + BOOKINGS_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bookings;
    }

    /**
     * Main method for testing migration (can be called from application startup or manually)
     */
    public static void main(String[] args) {
        System.out.println("Starting migration of bookings to transactions...");
        migrateBookingsToTransactions();
        System.out.println("Migration finished.");
    }
}

