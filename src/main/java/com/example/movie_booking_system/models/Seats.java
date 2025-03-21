package com.example.movie_booking_system.models;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seats {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long seat_id;

    @ManyToOne
    @JoinColumn(name = "showtime_id", referencedColumnName = "showtime_id", nullable = false)
    private ShowTime showtime;

    // Use only one column for seat number (either seatNumber or seat_number)
    @Column(name = "seat_number") // This tells JPA to map the seat_number column in DB
    private String seatNumber; // Logical property name in your code

    private Boolean seat_available;

    public Seats() {}

    public Seats(Long seat_id, ShowTime showtime, String seatNumber, Boolean seat_available) {
        this.seat_id = seat_id;
        this.showtime = showtime;
        this.seatNumber = seatNumber;
        this.seat_available = seat_available;
    }

    // Getters and setters
    public Long getSeat_id() {
        return seat_id;
    }

    public void setSeat_id(Long seat_id) {
        this.seat_id = seat_id;
    }

    public ShowTime getShowtime() {
        return showtime;
    }

    public void setShowtime(ShowTime showtime) {
        this.showtime = showtime;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Boolean getSeat_available() {
        return seat_available;
    }

    public void setSeat_available(Boolean seat_available) {
        this.seat_available = seat_available;
    }
}
