package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Booking {

    private String bookingId;       // unique identifier
    private String userId;          // which user booked
    private String spaceId;         // corresponds to Space.id
    private LocalDate date;         // booking date
    private LocalTime startTime;    // start time
    private LocalTime endTime;      // end time
    private String status;          // booked / released
    private int totalCredits;

    public Booking() {
        // default constructor, keep empty implementation
    }
    public Booking(String userId, String spaceId, LocalDate date,
                   LocalTime startTime, LocalTime endTime, String status) {
        this.bookingId = UUID.randomUUID().toString(); // automatically generate unique ID
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
