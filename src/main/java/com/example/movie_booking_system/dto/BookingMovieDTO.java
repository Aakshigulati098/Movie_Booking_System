package com.example.movie_booking_system.dto;

import java.util.List;

public class BookingMovieDTO {
    private List<Long> seatIds;

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }

    public BookingMovieDTO(List<Long> seatIds) {
        this.seatIds = seatIds;
    }
    public BookingMovieDTO() {}
}
