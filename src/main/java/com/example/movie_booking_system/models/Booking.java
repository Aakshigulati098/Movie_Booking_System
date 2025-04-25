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
    @JoinColumn(name = "userId", referencedColumnName = "id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "showtimeId", referencedColumnName = "id")
    private ShowTime showtime;

    @ManyToOne
    @JoinColumn(name="movie_id", referencedColumnName = "id")
    private Movie movie;

    private Long amount;

    private LocalDateTime booking_date;

    private String seatIds;

    private boolean reminderSent = false;

    private BookingEnum bookingStatus;

    public BookingEnum getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingEnum bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

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

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }



    public void setBookingDate(LocalDateTime bookingDate) {
        this.booking_date = bookingDate;
    }

    public LocalDateTime getBookingDate() {
        return booking_date;
    }

    public String getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(String seatIds) {
        this.seatIds = seatIds;
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public Booking(Long id, Users user, ShowTime showtime, Long amount, LocalDateTime bookingDate, String seat) {
        this.id = id;
        this.user = user;
        this.showtime = showtime;
        this.amount = amount;
        this.booking_date = bookingDate;
        this.seatIds = seat;
    }
}