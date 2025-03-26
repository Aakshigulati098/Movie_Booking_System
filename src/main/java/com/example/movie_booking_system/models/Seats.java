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
    private ShowTime showtime;

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSeatRow() {
        return Seatrow;
    }

    public void setRow(String Seatrow) {
        this.Seatrow = Seatrow;
    }

    private String section;
    private String Seatrow;

    private Long seatNumber;
    private Boolean seatAvailable;

    // ✅ Default constructor
    public Seats() {}

    // ✅ Constructor
    public Seats(Long id, ShowTime showtime, Long seatNumber, Boolean seatAvailable,String row,String section) {
        this.id = id;
        this.showtime = showtime;
        this.seatNumber = seatNumber;
        this.seatAvailable = seatAvailable;
        this.Seatrow=row;
        this.section=section;
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
