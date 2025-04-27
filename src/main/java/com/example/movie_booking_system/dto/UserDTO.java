package com.example.movie_booking_system.dto;

public class UserDTO {
    private String name;

    private String phone;

    // Constructors
    public UserDTO() {}

    public UserDTO(String name,  String phoneNumber) {
        this.name = name;

        this.phone = phoneNumber;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}