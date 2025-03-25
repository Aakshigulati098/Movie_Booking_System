package com.example.movie_booking_system.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "showtimeId", referencedColumnName = "id", nullable = false)
    private ShowTime showtime;

    private Long amount;
    private LocalDateTime booking_date;

    @ManyToOne
    @JoinColumn(name = "seatId", referencedColumnName = "id", nullable = false)
    private Seats seat;

    public Booking() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public ShowTime getShowtime() {
        return showtime;
    }

    public void setShowtime(ShowTime showtime) {
        this.showtime = showtime;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public LocalDateTime getBooking_date() {
        return booking_date;
    }

    public void setBooking_date(LocalDateTime booking_date) {
        this.booking_date = booking_date;
    }

    public Seats getSeat() {
        return seat;
    }

    public void setSeat(Seats seat) {
        this.seat = seat;
    }

    public Booking(Long id, Users user, ShowTime showtime, Long amount, LocalDateTime booking_date, Seats seat) {
        this.id = id;
        this.user = user;
        this.showtime = showtime;
        this.amount = amount;
        this.booking_date = booking_date;
        this.seat = seat;
    }
}