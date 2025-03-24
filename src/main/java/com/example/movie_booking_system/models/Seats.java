package com.example.movie_booking_system.models;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seats {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;  // ✅ Renamed from seat_id

    @ManyToOne
    @JoinColumn(name = "showtime_id", referencedColumnName = "id", nullable = false)
    private ShowTime showtime;  // ✅ Fixed field name and JoinColumn reference

    private Long seatNumber;  // ✅ Renamed from seat_number
    private Boolean seatAvailable;  // ✅ Renamed from seat_available

    // ✅ Default constructor
    public Seats() {}

    // ✅ Constructor
    public Seats(Long id, ShowTime showtime, Long seatNumber, Boolean seatAvailable) {
        this.id = id;
        this.showtime = showtime;
        this.seatNumber = seatNumber;
        this.seatAvailable = seatAvailable;
    }

    // ✅ Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ShowTime getShowtime() {
        return showtime;
    }

    public void setShowtime(ShowTime showtime) {
        this.showtime = showtime;
    }

    public Long getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Long seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Boolean getSeatAvailable() {
        return seatAvailable;
    }

    public void setSeatAvailable(Boolean seatAvailable) {
        this.seatAvailable = seatAvailable;
    }


}
