package com.example.movie_booking_system.models;


import jakarta.persistence.*;

@Entity
@Table(name="theatre")
public class Theatre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String theatre_name;
    private String pinCode;

    public Theatre() {}
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTheatre_name() {
        return theatre_name;
    }

    public void setTheatre_name(String theatre_name) {
        this.theatre_name = theatre_name;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public Theatre(Long id, String theatre_name, String pinCode) {
        this.id = id;
        this.theatre_name = theatre_name;
        this.pinCode = pinCode;
    }
}
