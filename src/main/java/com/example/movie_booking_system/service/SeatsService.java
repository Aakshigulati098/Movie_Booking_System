package com.example.movie_booking_system.service;

import com.example.movie_booking_system.models.Seats;
import com.example.movie_booking_system.repository.SeatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SeatsService {

    private final SeatsRepository seatsRepository;
    @Autowired
    public SeatsService(SeatsRepository seatsRepository) {
        this.seatsRepository = seatsRepository;
    }
    public Seats saveSeat(Seats seat) {
        return seatsRepository.save(seat);  // Save or update seat
    }

    public List<Seats> getAllSeats() {
        return seatsRepository.findAll();  // Retrieve all seats from the database
    }

    public Optional<Seats> getSeatById(Long seat_Id) {
        return seatsRepository.findById(seat_Id);  // Find a seat by its ID
    }

    public List<Seats> getSeatsByShowtime(Long showtime_Id) {
        // Custom query can be added here if you need to find seats by showtime
        // For example, you could have a custom repository method or use JPQL/Criteria
        return seatsRepository.findAll().stream()
                .filter(seat -> seat.getShowtime().getShowTime_id().equals(showtime_Id))
                .toList();
    }
    public Seats updateSeatAvailability(Long seat_Id, Boolean seat_available) {
        Optional<Seats> seatOpt = seatsRepository.findById(seat_Id);
        if (seatOpt.isPresent()) {
            Seats seat = seatOpt.get();
            seat.setSeat_available(seat_available);  // Update the seat's availability
            return seatsRepository.save(seat);  // Save the updated seat back to the database
        }
        return null;
    }
    }
