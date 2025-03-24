package com.example.movie_booking_system.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "showtime")
public class ShowTime {  // ✅ PascalCase for class name

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // ✅ Foreign key reference to Movie entity
    @ManyToOne
    @JoinColumn(name = "movie_id", referencedColumnName = "id", nullable = false)
    private Movie movie;

    private Long totalAmount;

    // ✅ Foreign key reference to Theatre entity
    @ManyToOne
    @JoinColumn(name = "theatre_id", referencedColumnName = "id", nullable = false)
    private Theatre theatre;

    private LocalDateTime showtime;

    // ✅ Default constructor
    public ShowTime() {}

    // ✅ Constructor
    public ShowTime(Long id, Movie movie, Long totalAmount, Theatre theatre, LocalDateTime showtime) {
        this.id = id;
        this.movie = movie;
        this.totalAmount = totalAmount;
        this.theatre = theatre;
        this.showtime = showtime;
    }

    // ✅ Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }

    public LocalDateTime getShowtime() {
        return showtime;
    }

    public void setShowtime(LocalDateTime showtime) {
        this.showtime = showtime;
    }
}
