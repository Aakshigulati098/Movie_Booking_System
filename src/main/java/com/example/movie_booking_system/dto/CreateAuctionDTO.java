package com.example.movie_booking_system.dto;

public class CreateAuctionDTO {
    private Long bookingId;
    private Long userId;
    private Long showtime;
    private Long minAmount;



    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShowtime() {
        return showtime;
    }

    public void setShowtime(Long showtime) {
        this.showtime = showtime;
    }

    public Long getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Long minAmount) {
        this.minAmount = minAmount;
    }

    public CreateAuctionDTO(Long bookingId, Long userId, Long showtime, Long minAmount) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.showtime = showtime;
        this.minAmount = minAmount;
    }
}
