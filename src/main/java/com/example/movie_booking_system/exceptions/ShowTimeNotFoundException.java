package com.example.movie_booking_system.exceptions;

public class ShowTimeNotFoundException extends RuntimeException {
    public ShowTimeNotFoundException(String message) {
        super(message);
    }
}
