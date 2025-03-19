package com.example.movie_booking_system.models;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class seats {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long seat_id;

    @ManyToOne
    @JoinColumn(name = "showtime_id", referencedColumnName = "showtime_id", nullable = false)
    private showTime showtime;
    private String seatNumber;
    private Long seat_number;
    private Boolean seat_available;

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public seats() {

    }

    public Long getSeat_id() {
        return seat_id;
    }

    public void setSeat_id(Long seat_id) {
        this.seat_id = seat_id;
    }

    public showTime getShowtime() {
        return showtime;
    }

    public void setShowtime(showTime showtime) {
        this.showtime = showtime;
    }

    public Long getSeat_number() {
        return seat_number;
    }

    public void setSeat_number(Long seat_number) {
        this.seat_number = seat_number;
    }

    public Boolean getSeat_available() {
        return seat_available;
    }

    public void setSeat_available(Boolean seat_available) {
        this.seat_available = seat_available;
    }

    public seats(Long seat_id, showTime showtime, String seatNumber, Long seat_number, Boolean seat_available) {
        this.seat_id = seat_id;
        this.showtime = showtime;
        this.seatNumber = seatNumber;
        this.seat_number = seat_number;
        this.seat_available = seat_available;
    }
}
