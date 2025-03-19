package com.example.movie_booking_system.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "showtime")
public class showTime {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long showTime_id;

    @ManyToOne
    @JoinColumn(name = "movie_id",referencedColumnName = "id")
    private movie movie;

    private Long theatre_id;
    private LocalDateTime showtime;

    public showTime() {

    }

    public Long getShowTime_id() {
        return showTime_id;
    }

    public void setShowTime_id(Long showTime_id) {
        this.showTime_id = showTime_id;
    }

    public movie getMovie() {
        return movie;
    }

    public void setMovie(movie movie) {
        this.movie = movie;
    }

    public Long getTheatre_id() {
        return theatre_id;
    }

    public void setTheatre_id(Long theatre_id) {
        this.theatre_id = theatre_id;
    }

    public LocalDateTime getShowtime() {
        return showtime;
    }

    public void setShowtime(LocalDateTime showtime) {
        this.showtime = showtime;
    }

    public showTime(Long showTime_id, movie movie, Long theatre_id, LocalDateTime showtime) {
        this.showTime_id = showTime_id;
        this.movie = movie;
        this.theatre_id = theatre_id;
        this.showtime = showtime;
    }
}
