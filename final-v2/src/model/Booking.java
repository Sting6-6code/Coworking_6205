package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Booking {

    private String bookingId;       // 唯一标识
    private String userId;          // 哪个用户预定
    private String spaceId;         // 对应 Space.id
    private LocalDate date;         // 预订日期
    private LocalTime startTime;    // 开始时间
    private LocalTime endTime;      // 结束时间
    private String status;          // booked / released
    private int totalCredits;

    public Booking() {
        // 默认构造函数，保留空实现
    }
    public Booking(String userId, String spaceId, LocalDate date,
                   LocalTime startTime, LocalTime endTime, String status) {
        this.bookingId = UUID.randomUUID().toString(); // 自动生成唯一 ID
        this.userId = userId;
        this.spaceId = spaceId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = (status == null || status.isBlank()) ? "booked" : status;
    }

    public Booking(String bookingId, String userId, String spaceId, LocalDate date,
                   LocalTime startTime, LocalTime endTime, String status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.spaceId = spaceId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public String getSpaceId() { return spaceId; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStatus(String status) { this.status = status; }

    public boolean overlaps(LocalDate d, LocalTime start, LocalTime end) {
        if (!this.date.equals(d)) return false;
        return startTime.isBefore(end) && endTime.isAfter(start);
    }
}
