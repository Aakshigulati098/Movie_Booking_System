package com.example.movie_booking_system.service;

import com.example.movie_booking_system.models.Seats;
import com.example.movie_booking_system.models.ShowTime;
import com.example.movie_booking_system.repository.SeatsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatsServiceTest {

    @Mock
    private SeatsRepository seatsRepository;

    @InjectMocks
    private SeatsService seatsService;

    // Remove the @BeforeEach method - it's redundant with @ExtendWith(MockitoExtension.class)

    @Test
    void saveSeat_ShouldSaveAndReturnSeat() {
        // Arrange
        Seats seat = new Seats();
        seat.setId(1L);
        seat.setSeatAvailable(true);
        seat.setSeatNumber(1L);
        // Need showtime for completeness but not critical for this test
        when(seatsRepository.save(any(Seats.class))).thenReturn(seat);

        // Act
        Seats savedSeat = seatsService.saveSeat(seat);

        // Assert
        assertNotNull(savedSeat);
        assertEquals(1L, savedSeat.getId());
        verify(seatsRepository, times(1)).save(any(Seats.class));
    }

    @Test
    void getAllSeats_ShouldReturnListOfSeats() {
        // Arrange
        Seats seat1 = new Seats();
        seat1.setId(1L);
        seat1.setSeatAvailable(true);

        Seats seat2 = new Seats();
        seat2.setId(2L);
        seat2.setSeatAvailable(false);

        List<Seats> seatsList = Arrays.asList(seat1, seat2);
        when(seatsRepository.findAll()).thenReturn(seatsList);

        // Act
        List<Seats> seats = seatsService.getAllSeats();

        // Assert
        assertNotNull(seats);
        assertEquals(2, seats.size());
        verify(seatsRepository, times(1)).findAll();
    }

    @Test
    void getSeatById_SeatExists_ShouldReturnSeat() {
        // Arrange
        Seats seat = new Seats();
        seat.setId(1L);
        seat.setSeatAvailable(true);

        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat));

        // Act
        Optional<Seats> result = seatsService.getSeatById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(seatsRepository, times(1)).findById(1L);
    }

    @Test
    void getSeatById_SeatDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        when(seatsRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Seats> result = seatsService.getSeatById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(seatsRepository, times(1)).findById(1L);
    }

    @Test
    void getSeatsByShowtime_ShouldReturnSeatsForGivenShowtime() {
        // Arrange
        ShowTime showtime = new ShowTime();
        showtime.setId(1L);

        Seats seat1 = new Seats();
        seat1.setId(1L);
        seat1.setShowtime(showtime);
        seat1.setSeatAvailable(true);

        Seats seat2 = new Seats();
        seat2.setId(2L);
        seat2.setShowtime(showtime);
        seat2.setSeatAvailable(true);

        // Add another seat with different showtime ID to verify filtering
        ShowTime showtime2 = new ShowTime();
        showtime2.setId(2L);

        Seats seat3 = new Seats();
        seat3.setId(3L);
        seat3.setShowtime(showtime2);
        seat3.setSeatAvailable(true);

        when(seatsRepository.findAll()).thenReturn(Arrays.asList(seat1, seat2, seat3));

        // Act
        List<Seats> seats = seatsService.getSeatsByShowtime(1L);

        // Assert
        assertNotNull(seats);
        assertEquals(2, seats.size());
        verify(seatsRepository, times(1)).findAll();
    }

    @Test
    void updateSeatAvailability_SeatExists_ShouldUpdateAndReturnSeat() {
        // Arrange
        Seats seat = new Seats();
        seat.setId(1L);
        seat.setSeatAvailable(false);
        seat.setSeatNumber(1L);

        when(seatsRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(seatsRepository.save(any(Seats.class))).thenReturn(seat);

        // Act
        Seats updatedSeat = seatsService.updateSeatAvailability(1L, true);

        // Assert
        assertNotNull(updatedSeat);
        assertTrue(updatedSeat.getSeatAvailable());
        verify(seatsRepository, times(1)).findById(1L);
        verify(seatsRepository, times(1)).save(any(Seats.class));
    }

    @Test
    void updateSeatAvailability_SeatDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(seatsRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Seats updatedSeat = seatsService.updateSeatAvailability(1L, true);

        // Assert
        assertNull(updatedSeat);
        verify(seatsRepository, times(1)).findById(1L);
        verify(seatsRepository, never()).save(any());
    }
}