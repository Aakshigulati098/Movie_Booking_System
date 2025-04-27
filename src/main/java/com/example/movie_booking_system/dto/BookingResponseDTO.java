package com.example.movie_booking_system.dto;


import com.example.movie_booking_system.models.BookingEnum;

public class BookingResponseDTO {
    private Long bookingId;

    private String movieName;
    private String movieImage;

    public BookingEnum getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingEnum bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    private BookingEnum bookingStatus;

    public String getMovieImage() {
        return movieImage;
    }

    public void setMovieImage(String movieImage) {
        this.movieImage = movieImage;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    private String theatreName;
    private String seats;

    private String showtime;

    // Constructors
    public BookingResponseDTO() {}

    public BookingResponseDTO(Long bookingId,  String theatreName, String seats,  String showtime, String movieName, String movieImage,BookingEnum bookingStatus) {
        this.bookingId = bookingId;
        this.movieName = movieName;
        this.movieImage = movieImage;
        this.bookingStatus = bookingStatus;

        this.theatreName = theatreName;
        this.seats = seats;

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