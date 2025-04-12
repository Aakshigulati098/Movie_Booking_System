package com.example.movie_booking_system.dto;

public class PendingAuctionDTO {
    private Long id;
    private String status;         // e.g., "pending", "accepted", "rejected"
    private String movieTitle;
    private String date;           // e.g., "April 15, 2025"
    private String time;           // e.g., "7:30 PM"
    private String seats;          // e.g., "G5, G6"
    private String theater;
    private Long originalPrice;  // e.g., "$28.00"
    private Long bidAmount;      // e.g., "$42.50"
    private String seller;
    private String expiresIn;      // e.g., "12:45" (minutes:seconds)
    private String timeLeft;       // e.g., "plenty", "warning", "critical"

    // Default constructor
    public PendingAuctionDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getTheater() {
        return theater;
    }

    public void setTheater(String theater) {
        this.theater = theater;
    }

    public Long getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Long originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Long getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(Long bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(String timeLeft) {
        this.timeLeft = timeLeft;
    }
// All-args constructor


    public PendingAuctionDTO(Long id, String status, String movieTitle, String date, String time, String seats, String theater, Long originalPrice, Long bidAmount, String seller, String expiresIn, String timeLeft) {
        this.id = id;
        this.status = status;
        this.movieTitle = movieTitle;
        this.date = date;
        this.time = time;
        this.seats = seats;
        this.theater = theater;
        this.originalPrice = originalPrice;
        this.bidAmount = bidAmount;
        this.seller = seller;
        this.expiresIn = expiresIn;
        this.timeLeft = timeLeft;
    }
}
