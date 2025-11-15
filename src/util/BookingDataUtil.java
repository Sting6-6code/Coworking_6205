package util;

import model.Booking;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class BookingDataUtil {

    private static final String BOOKING_FILE = "data/bookings.csv";

    /** 内存缓存 */
    private static final List<Booking> bookings = new ArrayList<>();

    static {
        loadBookings();
    }

    /** 加载 CSV */
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

    /** 保存 CSV */
    private static void saveBookings() {
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /** 添加 booking */
    public static void addBooking(Booking b) {
        bookings.add(b);
        saveBookings();
    }

    /** 释放 booking */
    public static void releaseBooking(String bookingId) {
        for (Booking b : bookings) {
            if (b.getBookingId().equals(bookingId)) {
                b.setStatus("released");
                break;
            }
        }
        saveBookings();
    }

    /** 获取全部 booking */
    public static List<Booking> getAllBookings() {
        return Collections.unmodifiableList(bookings);
    }

    /** 获取某空间某天的 bookings */
    public static List<Booking> getBookingsBySpaceIdAndDate(String spaceId, LocalDate date) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : bookings) {
            // ❗ FIXED: 原版错误对比字符串 → 现在直接比较 LocalDate
            if (b.getSpaceId().equals(spaceId)
                    && b.getDate().equals(date)
                    && !"released".equalsIgnoreCase(b.getStatus())) {
                result.add(b);
            }
        }
        return result;
    }

    /** 时间是否被占用 */
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
    //  ✅ NEW: 通用的更新 booking 状态方法
    // ============================================================
    public static void updateBookingStatus(String bookingId, String newStatus) {
        // ⭐ FIX: 替换 allBookings → 使用 bookings 字段
        for (Booking b : bookings) {
            if (b.getBookingId().equals(bookingId)) {
                b.setStatus(newStatus);
                break;
            }
        }

        // ⭐ FIX: saveAllBookings → 使用 saveBookings()
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

        // 找到覆盖该时间段的 booking
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

        // 删除原 booking
        bookings.remove(target);

        // 情况 1：slot 在开头
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
        // 情况 2：slot 在结尾
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
        // 情况 3：slot 在中间 → 分裂成两段
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


}
