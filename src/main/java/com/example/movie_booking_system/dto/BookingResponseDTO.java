package com.example.movie_booking_system.dto;

import java.time.LocalDateTime;

public class BookingResponseDTO {
    private Long bookingId;

    private String theatreName;
    private String seats;
//    private double totalPrice;
    private String showtime;

    // Constructors
    public BookingResponseDTO() {}

    public BookingResponseDTO(Long bookingId,  String theatreName, String seats,  String showtime) {
        this.bookingId = bookingId;

        this.theatreName = theatreName;
        this.seats = seats;
//        this.totalPrice = totalPrice;
        this.showtime = showtime;
    }



    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getTheatreName() { return theatreName; }
    public void setTheatreName(String theatreName) { this.theatreName = theatreName; }
    public String getSeats() { return seats; }
    public void setSeats(String seats) { this.seats = seats; }

    public String getShowtime() { return showtime; }
    public void setShowtime(String showtime) { this.showtime = showtime; }
}