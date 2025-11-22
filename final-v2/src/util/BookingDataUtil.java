package util;

import model.Booking;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class BookingDataUtil {

    private static final String BOOKING_FILE = "data/bookings.csv";

    /** Memory Cache */
    private static final List<Booking> bookings = new ArrayList<>();

    static {
        loadBookings();
    }

    /** Load CSV */
    public static void loadBookings() {
        bookings.clear();

        try {
            Path path = Paths.get(BOOKING_FILE);

            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);

                try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKING_FILE))) {
                    pw.println("bookingId,userId,spaceId,date,startTime,endTime,status");
                }
                return;
            }

            List<String> lines = Files.readAllLines(path);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                if (i == 0 && line.contains("bookingId")) continue;
                if (line.isBlank()) continue;

                String[] arr = line.split(",");
                if (arr.length < 7) continue;

                Booking b = new Booking(
                        arr[0],
                        arr[1],
                        arr[2],
                        LocalDate.parse(arr[3]),
                        LocalTime.parse(arr[4]),
                        LocalTime.parse(arr[5]),
                        arr[6]
                );

                bookings.add(b);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Save CSV */
    private static void saveBookings() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(BOOKING_FILE);
            java.nio.file.Files.createDirectories(path.getParent());
            
            try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKING_FILE))) {
                pw.println("bookingId,userId,spaceId,date,startTime,endTime,status");

                for (Booking b : bookings) {
                    pw.println(String.join(",",
                            b.getBookingId(),
                            b.getUserId(),
                            b.getSpaceId(),
                            b.getDate().toString(),
                            b.getStartTime().toString(),
                            b.getEndTime().toString(),
                            b.getStatus()
                    ));
                }
            }
            System.out.println("Bookings saved to: " + BOOKING_FILE + " (count: " + bookings.size() + ")");
        } catch (IOException e) {
            System.err.println("ERROR saving bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /** Add booking */
    public static void addBooking(Booking b) {
        if (b == null) {
            System.err.println("ERROR: Cannot add null booking!");
            return;
        }
        System.out.println("Adding booking: " + b.getBookingId() + ", userId: " + b.getUserId() + ", spaceId: " + b.getSpaceId());
        bookings.add(b);
        saveBookings();
        System.out.println("Booking saved. Total bookings: " + bookings.size());
    }

    /** Release booking */
    public static void releaseBooking(String bookingId) {
        for (Booking b : bookings) {
            if (b.getBookingId().equals(bookingId)) {
                b.setStatus("released");
                break;
            }
        }
        saveBookings();
    }

    /** Get all bookings */
    public static List<Booking> getAllBookings() {
        return Collections.unmodifiableList(bookings);
    }

    /** Get bookings for a space on a specific date */
    public static List<Booking> getBookingsBySpaceIdAndDate(String spaceId, LocalDate date) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : bookings) {
            // ❗ FIXED: Original string comparison error -> now comparing LocalDate directly
            if (b.getSpaceId().equals(spaceId)
                    && b.getDate().equals(date)
                    && !"released".equalsIgnoreCase(b.getStatus())) {
                result.add(b);
            }
        }
        return result;
    }

    /** Check if time is occupied */
    public static boolean isOccupied(String spaceId, LocalDate date, LocalTime start, LocalTime end) {
        for (Booking b : bookings) {
            if (b.getSpaceId().equals(spaceId)
                    && b.getDate().equals(date)
                    && !"released".equalsIgnoreCase(b.getStatus())
                    && b.overlaps(date, start, end)) {
                return true;
            }
        }
        return false;
    }


    // ============================================================
    //  ✅ NEW: Universal booking status update method
    // ============================================================
    public static void updateBookingStatus(String bookingId, String newStatus) {
        // ⭐ FIX: Replace allBookings -> use bookings field
        for (Booking b : bookings) {
            if (b.getBookingId().equals(bookingId)) {
                b.setStatus(newStatus);
                break;
            }
        }

        // ⭐ FIX: saveAllBookings -> use saveBookings()
        saveBookings();
    }
    
    public static void releaseBookingBySpaceTime(String spaceId, LocalDate date, LocalTime time) {
        for (Booking b : bookings) {
            if (b.getSpaceId().equals(spaceId)
                    && b.getDate().equals(date)
                    && !b.getStatus().equalsIgnoreCase("released")
                    && !time.isBefore(b.getStartTime())
                    && time.isBefore(b.getEndTime())) {
                b.setStatus("released");
                break;
            }
        }
        saveBookings();
    }
 // BookingDataUtil.java
    public static Booking getBookingBySpaceTime(String spaceId, LocalDate date, LocalTime time) {
        return bookings.stream()
            .filter(b -> b.getSpaceId().equals(spaceId)
                      && b.getDate().equals(date)
                      && b.getStartTime().equals(time)
                      && b.getStatus().equals("booked"))
            .findFirst()
            .orElse(null);
    }
    
    
    public static Booking getBookingCoveringSlot(String spaceId, LocalDate date, LocalTime slotStart) {
        for (Booking b : bookings) {
            if (b.getSpaceId().equals(spaceId)
                    && b.getDate().equals(date)
                    && !"released".equalsIgnoreCase(b.getStatus())
                    && !slotStart.isBefore(b.getStartTime())
                    && slotStart.isBefore(b.getEndTime())) {
                return b;
            }
        }
        return null;
    }

    public static void releaseSingleSlot(String spaceId, LocalDate date, LocalTime slotStart) {

        Booking target = null;

        // Find booking covering this time slot
        for (Booking b : bookings) {
            if (b.getSpaceId().equals(spaceId)
                    && b.getDate().equals(date)
                    && !slotStart.isBefore(b.getStartTime())
                    && slotStart.isBefore(b.getEndTime())
                    && b.getStatus().equals("booked")) {

                target = b;
                break;
            }
        }

        if (target == null) return;

        LocalTime bStart = target.getStartTime();
        LocalTime bEnd = target.getEndTime();
        LocalTime slotEnd = slotStart.plusMinutes(30);

        // Remove original booking
        bookings.remove(target);

        // Case 1: slot is at the beginning
        if (slotStart.equals(bStart)) {
            if (!slotEnd.equals(bEnd)) {
                bookings.add(new Booking(
                        UUID.randomUUID().toString(),
                        target.getUserId(),
                        target.getSpaceId(),
                        target.getDate(),
                        slotEnd,
                        bEnd,
                        "booked"
                ));
            }
        }
        // Case 2: slot is at the end
        else if (slotEnd.equals(bEnd)) {
            bookings.add(new Booking(
                    UUID.randomUUID().toString(),
                    target.getUserId(),
                    target.getSpaceId(),
                    target.getDate(),
                    bStart,
                    slotStart,
                    "booked"
            ));
        }
        // Case 3: slot is in the middle -> split into two parts
        else {
            bookings.add(new Booking(
                    UUID.randomUUID().toString(),
                    target.getUserId(),
                    target.getSpaceId(),
                    target.getDate(),
                    bStart,
                    slotStart,
                    "booked"
            ));

            bookings.add(new Booking(
                    UUID.randomUUID().toString(),
                    target.getUserId(),
                    target.getSpaceId(),
                    target.getDate(),
                    slotEnd,
                    bEnd,
                    "booked"
            ));
        }

        saveBookings();
    }

    
    /** 
     * ✅ Get all bookings for a space (regardless of date)
     * Used for booking statistics / calculating total revenue, etc.
     */
    public static List<Booking> getBookingsBySpaceId(String spaceId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getSpaceId().equals(spaceId)
                    && !"released".equalsIgnoreCase(b.getStatus())) {
                result.add(b);
            }
        }
        return result;
    }


}
