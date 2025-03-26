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


    private Long price;

    // ✅ Foreign key reference to Theatre entity
    @ManyToOne
    @JoinColumn(name = "theatre_id", referencedColumnName = "id", nullable = false)
    private Theatre theatre;

    private String time;

    // ✅ Default constructor
    public ShowTime() {}


    public ShowTime(Long id, Long price, Theatre theatre, String time) {
        this.id = id;

        this.price = price;

        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
