package com.example.movie_booking_system.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")  // Avoid using "user" as it's a reserved keyword in some databases
public class User {  // Class name should start with uppercase
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment ID
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)  // Ensure email is unique & not null
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 15)  // Phone number as String
    private String phone;

    @Column(length = 10)  // PinCode should be String to keep leading zeros
    private String pinCode;

    // ✅ Default Constructor
    public User() {}

    // ✅ Parameterized Constructor (without ID)
    public User(String name, String email, String password, String phone, String pinCode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.pinCode = pinCode;
    }

    // ✅ Getters & Setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }
}
