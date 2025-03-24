package com.example.movie_booking_system.service;
import com.example.movie_booking_system.models.ShowTime;
import com.example.movie_booking_system.repository.ShowTimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@Service
public class ShowTimeService {
   private final ShowTimeRepository showTimeRepository;

    public ShowTimeService(ShowTimeRepository showTimeRepository) {
        this.showTimeRepository = showTimeRepository;
    }

    // get all shows
    public List<ShowTime> getAllShows() {
        List<ShowTime> showTimes = showTimeRepository.findAll();
        if (showTimes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No shows found in the database");
        }
        return showTimes;
    }

    // get showtime by id
    public ShowTime getShowTimeById(Long showTime_id) {
        return showTimeRepository.findById(showTime_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ShowTime not found with ID: " + showTime_id));
    }
}
