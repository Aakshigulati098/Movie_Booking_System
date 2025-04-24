package com.example.movie_booking_system.dto;



public class BookingResponseDTO {
    private Long bookingId;

    private String movieName;
    private String movieImage;

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

    public BookingResponseDTO(Long bookingId,  String theatreName, String seats,  String showtime, String movieName, String movieImage) {
        this.bookingId = bookingId;
        this.movieName = movieName;
        this.movieImage = movieImage;

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